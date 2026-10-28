package com.naling.lightstream.exception;

public class EmptyFileException extends RuntimeException{
    public EmptyFileException() {
        super("File must not be empty");
    }
}
