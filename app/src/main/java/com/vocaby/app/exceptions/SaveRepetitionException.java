package com.vocaby.app.exceptions;

public class SaveRepetitionException extends Exception {
    public SaveRepetitionException() {
        super();
    }
    public SaveRepetitionException(String errorMessage) {
        super(errorMessage);
    }
}
