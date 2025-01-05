package pl.michal5520pl.wszib;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class PasswordValidatorHandler extends GenericHandler {
    PasswordValidatorHandler(final DatabaseProxy dbProxy, User user, List<String> handlers){
        super(dbProxy, user, handlers);
    }

    public Boolean validatePass(User user){
        logger.debug("Validating against such regexes: \".{8,}\", \"[A-Z]{1,}\", \"[a-z]{1,}\", \"[!-/:-@\\\\[-`{-~-]{1,}\"");
        setMessage("Password didn't pass the validation!");
        final String[] patterns = new String[]{".{8,}", "[A-Z]{1,}", "[a-z]{1,}", "[!-/:-@\\[-`{-~-]{1,}"};
        return Arrays
            .stream(patterns)
            .map(Pattern::compile)
            .map((Pattern pattern) -> (pattern.matcher(user.getPassword())))
            .allMatch((Matcher matcher) -> (matcher.find()));
    }
}
