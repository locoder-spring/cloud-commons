package icu.lowcoder.spring.cloud.file.manager;

public class FilesManagerException extends RuntimeException {
    public FilesManagerException() {
        super();
    }

    public FilesManagerException(String message) {
        super(message);
    }

    public FilesManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public FilesManagerException(Throwable cause) {
        super(cause);
    }

    protected FilesManagerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
