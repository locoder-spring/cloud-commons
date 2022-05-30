package icu.lowcoder.spring.cloud.authentication.exception;

import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;

public class InvalidSmsGrantException extends InvalidGrantException {
    public InvalidSmsGrantException(String msg) {
        super(msg);
    }

    public InvalidSmsGrantException(String msg, Throwable t) {
        super(msg, t);
    }
}
