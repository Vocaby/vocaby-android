package com.vocaby.app.utils.exception;

public class IllegalFileException extends Exception {
    public static final int INVALID_FORMAT = 0;
    public static final int INVALID_FILE = 1;
    private final int code;

    public IllegalFileException(int code) {
        super();
        this.code = code;
    }

    public IllegalFileException(String message, int code) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
