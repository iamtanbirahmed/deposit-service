package com.tryvalut.api.deposit.exception;

public class WeeklyDepositAmountLimitException extends Exception{
    public WeeklyDepositAmountLimitException(ExceptionMessage exceptionMessage){
        super(String.format("Maximum limit for a weekly deposit has exceeded  id: %s customer_id: %s", exceptionMessage.depositId(), exceptionMessage.customerId()));
    }
}
