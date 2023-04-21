package com.tryvalut.api.deposit.validator;

public enum ValidationResult {

    SUCCESS,
    DAILY_DEPOSIT_AMOUNT_LIMIT_EXCEED,
    DAILY_DEPOSIT_COUNT_LIMIT_EXCEED,
    WEEKLY_DEPOSIT_AMOUNT_LIMIT_EXCEED,
    MULTIPLE_DEPOSIT_REQUEST

}
