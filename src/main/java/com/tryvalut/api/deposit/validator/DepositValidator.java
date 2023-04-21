package com.tryvalut.api.deposit.validator;

import com.tryvalut.api.deposit.dtos.DepositDTO;
import com.tryvalut.api.deposit.model.Deposit;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

//public interface DepositValidator extends Function<Pair<DepositDTO, List<Deposit>>, ValidationResult> {
//
//
//    static DepositValidator isDailyDepositAmountValid() {
//        return (depositPair) -> depositPair.getFirst().loadAmount() + depositPair.getSecond().stream().mapToDouble(Deposit::getLoadAmount).sum() <= 5000 ? ValidationResult.SUCCESS : ValidationResult.DAILY_DEPOSIT_AMOUNT_LIMIT_EXCEED;
//    }
//
//    static DepositValidator isDailyDepositCountValid() {
//        return (depositPair) -> depositPair.getSecond().stream().count() > 3 ? ValidationResult.SUCCESS : ValidationResult.DAILY_DEPOSIT_COUNT_LIMIT_EXCEED;
//    }
//
//    static DepositValidator isWeeklyDepositAmountValid() {
//        return (depositPair) -> depositPair.getFirst().loadAmount() + depositPair.getSecond().stream().mapToDouble(Deposit::getLoadAmount).sum() <= 20000 ? ValidationResult.SUCCESS : ValidationResult.WEEKLY_DEPOSIT_AMOUNT_LIMIT_EXCEED;
//    }
//
//    default DepositValidator and(DepositValidator __other) {
//        return customer -> {
//            ValidationResult results = this.apply(customer);
//            return results.equals(ValidationResult.SUCCESS) ? __other.apply(customer) : results;
//        };
//    }
//
//
//}
