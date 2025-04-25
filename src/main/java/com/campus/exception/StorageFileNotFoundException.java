package com.campus.exception;

/**
 * 文件未找到异常
 */
public class StorageFileNotFoundException extends StorageException {

    private static final long serialVersionUID = 1L;

    public StorageFileNotFoundException() {
        super();
    }

    public StorageFileNotFoundException(String message) {
        super(message);
    }

    public StorageFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public StorageFileNotFoundException(Throwable cause) {
        super(cause);
    }
} 