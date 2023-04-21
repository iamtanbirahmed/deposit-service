package com.tryvalut.api.deposit.exception;

public class MultipleRequestException extends Exception {
    private String message;
    public MultipleRequestException( ExceptionMessage exceptionMessage) {
        super(String.format("Multiple request received for load id: %s customer_id: %s", exceptionMessage.depositId(), exceptionMessage.customerId()));
    }
}
