package com.tryvalut.api.deposit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tryvalut.api.deposit.dtos.DepositDTO;
import com.tryvalut.api.deposit.dtos.DepositOutputDTO;
import com.tryvalut.api.deposit.exception.*;
import com.tryvalut.api.deposit.model.Deposit;
import com.tryvalut.api.deposit.repository.DepositRepository;
import com.tryvalut.api.deposit.util.DateUtil;
import com.tryvalut.api.deposit.validator.ValidationResult;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.time.LocalDateTime;
import java.util.function.Function;


@Service
@AllArgsConstructor
public class DepositService {


    private final DepositRepository depositRepository;
    private final ObjectMapper objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(DepositService.class);

    private interface Validator extends Function<Tuple3<Integer, LocalDateTime, Double>, ValidationResult> {

        default Validator and(Validator __other) {
            return tuple -> {
                ValidationResult results = apply(tuple);
                return results.equals(ValidationResult.SUCCESS) ? __other.apply(tuple) : results;
            };
        }
    }

    public void save(DepositDTO depositDTO) throws Exception {

        if (depositRepository.countDepositByDepositIdAndCustomerId(depositDTO.depositId(), depositDTO.customerId()) > 0) {
            throw new MultipleRequestException(new ExceptionMessage(depositDTO.depositId(), depositDTO.customerId(), ValidationResult.MULTIPLE_DEPOSIT_REQUEST));
        }

        int customerId = depositDTO.customerId();
        LocalDateTime requestTime = depositDTO.requestTime();
        Double requestAmount = depositDTO.loadAmount();


        ValidationResult result = weeklyDepositSumIsValid()
                .and(dailyDepositSumIsValid())
                .and(dailyDepositCountIsValid())
                .apply(Tuples.of(customerId, requestTime, requestAmount));

        if (result != ValidationResult.SUCCESS) {
            throw new DepositException(new ExceptionMessage(depositDTO.depositId(), depositDTO.customerId(), result));
        }

//        if (dailyCount > 3) {

//            throw new DailyDepositCountLimitExceed(new ExceptionMessage(depositDTO.depositId(), depositDTO.customerId()));
//        } else if (dailySum + depositDTO.loadAmount() > 5000) {
//            logger.debug("Daily sum: {} {}", dailySum + depositDTO.loadAmount(), ValidationResult.DAILY_DEPOSIT_AMOUNT_LIMIT_EXCEED);
//            throw new DailyDepositAmountExceedException(new ExceptionMessage(depositDTO.depositId(), depositDTO.customerId()));
//        } else if (weeklySum + depositDTO.loadAmount() > 20000) {
//            logger.debug("weekly deposit: {} {}", weeklySum + depositDTO.loadAmount(), ValidationResult.WEEKLY_DEPOSIT_AMOUNT_LIMIT_EXCEED);
//            throw new WeeklyDepositAmountLimitException(new ExceptionMessage(depositDTO.depositId(), depositDTO.customerId()));
//        }

        Deposit deposit = new Deposit();
        deposit.setDepositId(depositDTO.depositId());
        deposit.setCustomerId(depositDTO.customerId());
        deposit.setLoadAmount(depositDTO.loadAmount());
        deposit.setRequestTime(depositDTO.requestTime());
        depositRepository.save(deposit);

        logger.info(objectMapper.writeValueAsString(new DepositOutputDTO(deposit.getDepositId(), deposit.getCustomerId(), true)));
    }

    Double getDepositSumForCustomerWithinPeriod(Integer customerId, LocalDateTime start, LocalDateTime end) {
        return depositRepository.findDepositByCustomerIdAndRequestTimeGreaterThanEqualAndRequestTimeLessThanEqual(customerId, start, end).stream().mapToDouble(Deposit::getLoadAmount).sum();
    }

    Integer getDepositCountForCustomerWithinPeriod(Integer customerId, LocalDateTime start, LocalDateTime end) {
        return depositRepository.findDepositByCustomerIdAndRequestTimeGreaterThanEqualAndRequestTimeLessThanEqual(customerId, start, end).size();
    }


    Validator dailyDepositCountIsValid() {
        return tuple -> {
            Integer customerId = tuple.getT1();
            LocalDateTime requestTime = tuple.getT2();
            Pair<LocalDateTime, LocalDateTime> startAndEndOfDay = DateUtil.getStartAndEndOfDay(requestTime);
            int dailyCount = getDepositCountForCustomerWithinPeriod(customerId, startAndEndOfDay.getFirst(), startAndEndOfDay.getSecond());
            logger.debug("Customer Id: {} -> Daily Count {}", customerId, dailyCount);
            if (dailyCount >= 3) {
                logger.debug("Daily count: {} {}", dailyCount, ValidationResult.DAILY_DEPOSIT_COUNT_LIMIT_EXCEED);
            }
            return dailyCount < 3 ? ValidationResult.SUCCESS : ValidationResult.DAILY_DEPOSIT_COUNT_LIMIT_EXCEED;
        };

    }

    Validator dailyDepositSumIsValid() {
        return tuple -> {
            Integer customerId = tuple.getT1();
            Double requestAmount = tuple.getT3();
            LocalDateTime requestTime = tuple.getT2();
            Pair<LocalDateTime, LocalDateTime> startAndEndOfDay = DateUtil.getStartAndEndOfDay(requestTime);
            Double dailySum = getDepositSumForCustomerWithinPeriod(customerId, startAndEndOfDay.getFirst(), startAndEndOfDay.getSecond());
            logger.debug("Customer Id: {} -> Daily Sum: {}", customerId, dailySum+requestAmount);
            if (requestAmount + dailySum > 5000.0) {
                logger.debug("Daily sum: {} {}", dailySum + requestAmount, ValidationResult.DAILY_DEPOSIT_AMOUNT_LIMIT_EXCEED);
            }
            return requestAmount + dailySum <= 5000.0 ? ValidationResult.SUCCESS : ValidationResult.DAILY_DEPOSIT_AMOUNT_LIMIT_EXCEED;
        };
    }


    Validator weeklyDepositSumIsValid() {

        return tuple -> {
            Integer customerId = tuple.getT1();
            Double requestAmount = tuple.getT3();
            LocalDateTime requestTime = tuple.getT2();
            Pair<LocalDateTime, LocalDateTime> startAndEndOfWeek = DateUtil.getStartAndEndOfWeek(requestTime);
            Double weeklySum = getDepositSumForCustomerWithinPeriod(customerId, startAndEndOfWeek.getFirst(), startAndEndOfWeek.getSecond());
            logger.debug("Customer Id: {} -> Weekly total: {}", customerId, weeklySum);
            if(weeklySum + requestAmount > 20000){
                logger.debug("weekly deposit: {} {}", weeklySum + requestAmount, ValidationResult.WEEKLY_DEPOSIT_AMOUNT_LIMIT_EXCEED);
            }
            return weeklySum + requestAmount <= 20000 ? ValidationResult.SUCCESS : ValidationResult.WEEKLY_DEPOSIT_AMOUNT_LIMIT_EXCEED;
        };

    }


}
