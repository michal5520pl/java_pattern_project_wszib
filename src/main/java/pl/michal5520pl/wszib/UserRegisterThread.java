package pl.michal5520pl.wszib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class UserRegisterThread implements Runnable {
    private final DatabaseProxy dbProxy;
    private final User user;
    private List<String> handlers;

    UserRegisterThread(final DatabaseProxy dbProxy, final User user, final List<String> handlers){
        this.dbProxy = dbProxy;
        this.user = user;
        this.handlers = handlers;
    }

    @Override
    public void run() {
        if(handlers == null){
            handlers = new ArrayList<String>();
            Collections.addAll(handlers, "UsernameValidatorHandler::validateUsername", "EmailValidatorHandler::validateEmail", "PasswordValidatorHandler::validatePass");
        }
        else {
            handlers = new ArrayList<String>(handlers);  // Making a copy so all threads won't share the same array
        }

        dbProxy.startRegister(user, handlers);
    }
}
