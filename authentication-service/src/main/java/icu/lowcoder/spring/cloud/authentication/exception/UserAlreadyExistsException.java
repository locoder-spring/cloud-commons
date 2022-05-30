package icu.lowcoder.spring.cloud.authentication.exception;

public class UserAlreadyExistsException extends UserRegistrationException {
    public UserAlreadyExistsException(String msg) {
        super(msg);
    }

    public UserAlreadyExistsException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
