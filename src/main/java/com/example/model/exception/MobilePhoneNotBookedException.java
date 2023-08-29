package com.example.model.exception;

public class MobilePhoneNotBookedException extends RuntimeException {

    public MobilePhoneNotBookedException(String message) {
        super(message);
    }

}