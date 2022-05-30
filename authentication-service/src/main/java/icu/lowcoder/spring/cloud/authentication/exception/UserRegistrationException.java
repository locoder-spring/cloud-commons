package icu.lowcoder.spring.cloud.authentication.exception;

public class UserRegistrationException extends RuntimeException {
    public UserRegistrationException(String msg) {
        super(msg);
    }

    public UserRegistrationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
