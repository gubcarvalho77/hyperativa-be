package com.hyperativa.be.exceptions;

public class CreditCardException extends RuntimeException {

    public CreditCardException(String msg) {
        super(msg);
    }

    public CreditCardException(String msg, Throwable e) {
        super(msg, e);
    }
}
