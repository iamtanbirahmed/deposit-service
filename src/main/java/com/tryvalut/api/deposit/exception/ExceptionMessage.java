package com.tryvalut.api.deposit.exception;


import com.tryvalut.api.deposit.validator.ValidationResult;

public record ExceptionMessage (Integer depositId, Integer customerId, ValidationResult result){

}
