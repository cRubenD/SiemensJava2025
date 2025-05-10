package com.siemens.internship.validator;


import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class EmailValidator {

    /**
     * email pattern REGEX
     */
    private static final String EMAIL_PATTERN =
            "^[a-zA-Z0-9_.-]+@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$";
    private final Pattern pattern;
    public EmailValidator() {
        this.pattern = Pattern.compile(EMAIL_PATTERN);
    }

    public boolean validateEmail(String email) {
        if(email == null) {
            return false;
        }

        return pattern.matcher(email).matches();
    }
}
