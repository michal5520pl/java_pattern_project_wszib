package pl.michal5520pl.wszib;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DatabaseProxy {
    private static final Logger logger = LogManager.getLogger();
    private final String username, password, jdbcURL;

    DatabaseProxy(final String jdbcURL, final String username, final String password){
        this.jdbcURL = jdbcURL;
        this.username = username;
        this.password = password;
    }

    void startRegister(final User user, List<String> handlers){
        logger.debug("Starting initial handler...");

        GenericHandler handlerObject = new GenericHandler(this, user, handlers){};
        handlerObject.validate((dummy) -> { return true; }); // I just didn't want to copy the code from GenericHandler...
    }

    void failedRegister(final Class<? extends GenericHandler> classExecuting, final String message, final User user){
        try {
            ((Logger) classExecuting.getField("logger").get(null)).error(message);
        }
        catch(NoSuchFieldException | IllegalAccessException e){
            GenericHandler.logger.error(message);
        }

        System.out.println(
            message == null
                ? String.format("Validation check performed by %s for %s was not successful!", classExecuting.getName(), user.getUsername())
                : message
        );
    }

    public boolean containsUser(final User user){
        var db = new Database(jdbcURL, username, password);
        db.createTable();

        return db.containsUser(user);
    }

    boolean registerUser(final User user){
        var db = new Database(jdbcURL, username, password);
        db.createTable();
        
        if(db.containsUser(user)){
            return false;
        }

        return db.registerUser(user);
    }
}
