package com.tryvalut.api.deposit.exception;

public class DepositException extends Exception{

    public DepositException(ExceptionMessage exceptionMessage){
        super(String.format("Deposit validation failed id: %s customer_id: %s reason: %s", exceptionMessage.depositId(), exceptionMessage.customerId(), exceptionMessage.result()));
    }
}
