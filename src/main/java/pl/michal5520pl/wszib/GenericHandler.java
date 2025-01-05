package pl.michal5520pl.wszib;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class GenericHandler {
    private final DatabaseProxy dbProxy;
    private final List<String> nextHandlers;
    private final User user;
    private String errorMessage = null;
    protected final static Logger logger = LogManager.getLogger();

    GenericHandler(final DatabaseProxy dbProxy, final User user, final List<String> nextHandlers){
        this.dbProxy = dbProxy;
        this.user = user;
        this.nextHandlers = nextHandlers;
    }

    protected final User getUser(){
        return this.user;
    }

    protected final List<String> getHandlers(){
        return this.nextHandlers;
    }

    protected final String getMessage(){
        return this.errorMessage;
    }

    protected final void setMessage(final String message){
        this.errorMessage = message;
    }

    final void validate(Function<User, Boolean> function){
        logger.debug(String.format("Starting %s handler", getClass().getName()));

        if(! function.apply(user)){
            dbProxy.failedRegister(getClass(), errorMessage, user);
        }
        else {
            if(nextHandlers.size() == 0){
                if(this.dbProxy.registerUser(user)){
                    logger.info(String.format("User %s has been registered!", user.getUsername()));
                    System.out.println(String.format("User %s has been registered!", user.getUsername()));
                }
                else {
                    logger.error(String.format("User %s has not been registered!", user.getUsername()));
                    System.out.println(String.format("User %s has not been registered!", user.getUsername()));
                }
                
                return;
            }

            String handlerName = nextHandlers.remove(0);
            String[] handlerNames = handlerName.split("::", 2); // First thing should be class name and second method to invoke

            try {
                GenericHandler handler = 
                    (GenericHandler) Class
                        .forName(handlerNames[0].contains(".") ? handlerNames[0] : "pl.michal5520pl.wszib.".concat(handlerNames[0])) // Class.forName requires full name including package name. If there is no package name, add the default one
                        .getDeclaredConstructor(DatabaseProxy.class, User.class, List.class)
                        .newInstance(dbProxy, user, nextHandlers);

                Function<User, Boolean> func = (User user) -> {
                    try {
                        return (Boolean) handler.getClass().getMethod(handlerNames[1], User.class).invoke(handler, user);
                    }
                    catch(IllegalArgumentException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e){
                        if(e.getCause() == null){
                            handler.setMessage(String.format("Failed to invoke handler function %s!", handlerNames[1]));
                        }
                        else {
                            handler.setMessage(String.format("Method %s has thrown an exception! Exception: %s", handlerNames[1], e.getCause().getMessage()));
                        }
                        dbProxy.failedRegister(handler.getClass(), handler.getMessage(), user);
                    }

                    return false;
                };

                handler.validate(func);
            }
            catch(ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e){
                dbProxy.failedRegister(getClass(), String.format("Can't access class %s, exiting!", handlerNames[0]), user);
            }
        }
    }
}
