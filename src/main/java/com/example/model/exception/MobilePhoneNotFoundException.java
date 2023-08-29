package com.example.model.exception;

public class MobilePhoneNotFoundException extends RuntimeException {

    public MobilePhoneNotFoundException(String message) {
        super(message);
    }

}