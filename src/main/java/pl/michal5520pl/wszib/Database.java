package pl.michal5520pl.wszib;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class Database {
    private static final Logger logger = LogManager.getLogger();
    private String jdbcURL = "jdbc:h2:mem:users"; // Default
    private final String username, password;

    Database(final String jdbcURL, final String username, final String password){
        if(jdbcURL != null){
            this.jdbcURL = jdbcURL;
        }
        this.username = username;
        this.password = password;
    }

    private Connection getConnection(){
        try {
            Class.forName("org.h2.Driver");
            return DriverManager.getConnection(jdbcURL, username, password);
        }
        catch(ClassNotFoundException e){
            logger.error("Class org.h2.Driver not found!");
            System.exit(1);
            return null;
        }
        catch(SQLException e){
            logger.error("SQL Exception! Bad jdbcURL, user, password or connection!");
            logger.debug(e.getMessage());
            System.exit(1);
            return null;
        }
    }

    void createTable(){
        var connection = getConnection();
        try {
            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS users(username VARCHAR(64), email VARCHAR(64), password VARCHAR(256), PRIMARY KEY(username));");
        }
        catch(SQLException e){
            logger.error("SQL Exception! Failed table creation!");
            logger.debug(e.getMessage());
            System.exit(1);
        }
    }

    boolean containsUser(final User user){
        var connection = getConnection();
        try(var statement = connection.prepareStatement("SELECT username FROM users WHERE username=?;")){
            statement.setString(1, user.getUsername());
            return statement.executeQuery().first();
        }
        catch(SQLException e){
            logger.error("SQL Exception! Could not get info about user in DB.");
            logger.debug(e.getMessage());
        }
        return false;
    }

    boolean registerUser(final User user){
        var connection = getConnection();
        try(var statement = connection.prepareStatement("INSERT INTO USERS VALUES(?, ?, ?);")){
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPassword());
            return statement.executeUpdate() > 0;
        }
        catch(SQLException e){
            logger.error("SQL Exception! Registering user failed!");
            logger.debug(e.getMessage());
        }

        return false;
    }
}
