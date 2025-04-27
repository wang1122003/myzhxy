package com.campus.exception;

/**
 * 文件存储异常
 */
public class StorageException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public StorageException() {
        super();
    }

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }

    public StorageException(Throwable cause) {
        super(cause);
    }
} 