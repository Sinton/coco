package com.github.coco.exception;

/**
 * @author Yan
 */
public class BaseException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public BaseException() {}

    public BaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public BaseException(String message) {
        super(message);
    }

    public BaseException(Throwable cause) {
        super(cause);
    }
}
