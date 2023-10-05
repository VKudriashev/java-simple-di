package com.simpledi.exception;

public class ConstructorAmbiguityException extends RuntimeException {

    public ConstructorAmbiguityException(String message) {
        super(message);
    }
}
