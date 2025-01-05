package pl.michal5520pl.wszib;

import java.util.List;

import org.apache.commons.validator.routines.RegexValidator;

class UsernameValidatorHandler extends GenericHandler {
    UsernameValidatorHandler(final DatabaseProxy dbProxy, final User user, final List<String> nextHandlers){
        super(dbProxy, user, nextHandlers);
    }

    public Boolean validateUsername(User user){
        setMessage("Username is not correct!");
        return (new RegexValidator("[A-Za-z0-9]*")).isValid(user.getUsername());
    }
}
