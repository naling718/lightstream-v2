package com.naling.lightstream.exception;

public class RateLimitExceededException extends RuntimeException{
    public RateLimitExceededException() {
        super("Too many claim attempts");
    }
}
