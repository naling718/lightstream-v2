package com.naling.lightstream.exception;

public class ClaimNotFoundException extends RuntimeException{

    public ClaimNotFoundException(String code){
        super("No share found for code " + code);
    }
}
