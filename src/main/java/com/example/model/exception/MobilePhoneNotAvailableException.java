package com.example.model.exception;

public class MobilePhoneNotAvailableException extends RuntimeException {

    public MobilePhoneNotAvailableException(String message) {
        super(message);
    }

}