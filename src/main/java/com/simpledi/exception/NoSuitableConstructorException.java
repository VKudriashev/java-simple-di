package com.simpledi.exception;

public class NoSuitableConstructorException extends RuntimeException {

    public NoSuitableConstructorException(String message) {
        super(message);
    }
}
