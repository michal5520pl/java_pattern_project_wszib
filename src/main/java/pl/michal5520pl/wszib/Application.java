package pl.michal5520pl.wszib;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

public class Application {
    private static final Logger logger = LogManager.getLogger();
    private static Options options = new Options();
    private static CommandLine cmd;
    private static Properties properties = new Properties();
    private static boolean correctConfig = false;

    private final static String ANOTHER_USER = "Do you want to create another user? (y/n) ";
    private final static String USERNAME = "Username: ";
    private final static String EMAIL = "User's email: ";
    private final static String PASSWORD = "Password: ";

    public static void main(String args[]){
        addOptions();

        parseCmdArgs(args);

        if(cmd.hasOption("d")){
            Configurator.setAllLevels(LogManager.ROOT_LOGGER_NAME, Level.DEBUG);
        }

        if(cmd.hasOption("c")){
            if(! loadConfig(cmd.getOptionValue("c"))){
                System.exit(1);
                return;
            }
        }

        if(cmd.hasOption("j")){
            if(properties.containsKey("h2.jdbcURL")){
                logger.warn("jdbcURL is defined both in config file and command line arguments. Using the one from CMD arguments.");
            }

            properties.setProperty("h2.jdbcURL", cmd.getOptionValue("j"));
        }

        List<User> userList;

        if(cmd.hasOption('i')){
            userList = (new JSONParser(cmd.getOptionValue('i'))).getUsers();
        }
        else {
            userList = askForUsers();
        }

        List<String> handlersList = null;

        if(properties.containsKey("internals.handlers")){
            handlersList = new ArrayList<>();
            Collections.addAll(handlersList, properties.getProperty("internals.handlers").replace(" ", "").split(","));
        }

        DatabaseProxy db = new DatabaseProxy(
            properties.getProperty("h2.jdbcURL"),
            properties.getProperty("h2.user"),
            properties.getProperty("h2.password")
        );

        ExecutorService pool = Executors.newFixedThreadPool(10);

        for(final User user: userList){
            var thread = new UserRegisterThread(db, user, handlersList);
            pool.execute(thread);
        }

        pool.shutdown();

        try {
            if(! pool.awaitTermination(2, TimeUnit.MINUTES)){
                throw new ExecutionException("The task didn't finish in proper time!", null);
            }
        }
        catch(InterruptedException e){
            logger.error(String.format("The task was interrupted!"));
            logger.debug(e.getMessage());
            System.out.println("Seems like user registration didn't go well...");
        }
        catch(ExecutionException e){
            logger.error(e.getMessage());
            System.out.println("Seems like user registration didn't go well...");
        }
    }

    private static void addOptions(){
        options.addOption("i", "inputfile", true, "Specify input file");
        options.addOption("j", "jdbcURL", true, "JDBC URL for H2 DB");
        options.addOption("c", "config", true, "Config file");
        options.addOption("d", "debug", false, "Debug logs");
    }

    private static void parseCmdArgs(String args[]){
        try {
            cmd = (new DefaultParser()).parse(options, args);
        }
        catch(ParseException e){
            logger.error("There was a parse exception during CMD args processing!");
            logger.debug(e.getMessage());
            System.exit(1);
        }
    }

    private static boolean loadConfig(final String filename){
        try(FileInputStream fis = new FileInputStream(filename)){
            logger.debug("Loading config file...");
            properties.load(fis);
        }
        catch(FileNotFoundException e){
            logger.error("Config file not found!");
            return false;
        }
        catch(IOException e){
            logger.error("Some IO error!");
            logger.debug(e.getMessage());
            return false;
        }

        return checkConfig();
    }

    private static boolean checkConfig(){
        correctConfig = true;

        Arrays.asList(new String[]{"prompts.ANOTHER_USER", "prompts.USERNAME", "prompts.EMAIL", "prompts.PASSWORD"}).forEach(prompt -> {
            if(properties.getProperty(prompt) == null){
                logger.error(String.format("Config doesn't contain required %s", prompt));
                correctConfig = false;
            }
        });

        return correctConfig;
    }

    private static List<User> askForUsers(){
        Scanner scanner = new Scanner(System.in);
        ArrayList<User> array = new ArrayList<>();
        String[] userData;
        Predicate<List<String>> nullUserData = (list) -> {
            if(list.size() == 0){ return true; };

            Iterator<String> iter = list.iterator();

            while(iter.hasNext()){ if(iter.next() == null){ return true; } }

            return false;
        };
        Function<String, String> prompter = (prompt) -> {
            boolean toEnd = false;
            String data;

            do {
                System.out.println(prompt);
                data = scanner.nextLine();
                
                if(array.size() > 0 && data.isBlank()){
                    System.out.println(correctConfig ? properties.getProperty("prompts.ANOTHER_USER") : ANOTHER_USER);

                    String choice = "";
                        try {
                            choice = scanner.next("[YynN]");
                        }
                        catch(InputMismatchException e){
                            scanner.nextLine();
                            System.out.println("Try again!");
                        }

                    if(choice.toLowerCase().matches("n")){
                        data = null;
                        toEnd = true;
                    }
                }
                else if(! data.isBlank()) {
                    toEnd = true;
                }
                
            } while(!toEnd);

            return data;
        };

        do {
            List<String> prompts;

            if(correctConfig){
                prompts = Arrays.asList(properties.getProperty("prompts.USERNAME"), properties.getProperty("prompts.EMAIL"), properties.getProperty("prompts.PASSWORD"));
            }
            else {
                prompts = Arrays.asList(USERNAME, EMAIL, PASSWORD);
            };

            userData = prompts.stream().map(prompter).toArray(String[]::new);
        } while(! nullUserData.test(Arrays.asList(userData)) && array.add(new User(userData[0], userData[1], userData[2])));

        scanner.close();
        return array;
    }
}
