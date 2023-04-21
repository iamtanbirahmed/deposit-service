package com.tryvalut.api.deposit.exception;

public class DailyDepositCountLimitExceed extends Exception {
    public DailyDepositCountLimitExceed(ExceptionMessage exceptionMessage) {
        super(String.format("Maximum limit for a daily deposit has exceeded  id: %s customer_id: %s", exceptionMessage.depositId(), exceptionMessage.customerId()));
    }
}
