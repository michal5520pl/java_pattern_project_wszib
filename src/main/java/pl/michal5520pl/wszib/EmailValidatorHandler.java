package pl.michal5520pl.wszib;

import java.util.List;

import org.apache.commons.validator.routines.DomainValidator;
import org.apache.commons.validator.routines.EmailValidator;

class EmailValidatorHandler extends GenericHandler {
    EmailValidatorHandler(final DatabaseProxy dbProxy, User user, List<String> handlers){
        super(dbProxy, user, handlers);
    }

    public Boolean validateEmail(User user){
        setMessage("Email is not correct!");
        return (new EmailValidator(true, true, DomainValidator.getInstance(true))).isValid(user.getEmail());
    }
}
