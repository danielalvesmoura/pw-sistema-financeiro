package com.ifpr.backend.exception;
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) { 
        super(message); 
    }
}
