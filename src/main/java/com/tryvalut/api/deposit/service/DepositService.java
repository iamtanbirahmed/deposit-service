package com.tryvalut.api.deposit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tryvalut.api.deposit.dtos.DepositDTO;
import com.tryvalut.api.deposit.dtos.DepositOutputDTO;
import com.tryvalut.api.deposit.exception.*;
import com.tryvalut.api.deposit.model.Deposit;
import com.tryvalut.api.deposit.repository.DepositRepository;
import com.tryvalut.api.deposit.util.DateUtil;
import com.tryvalut.api.deposit.validator.DepositValidator;
import com.tryvalut.api.deposit.validator.ValidationResult;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.w3c.dom.ls.LSOutput;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;


@Service
@AllArgsConstructor
public class DepositService {


    private final DepositRepository depositRepository;
    private final ObjectMapper objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(DepositService.class);

    public void save(DepositDTO depositDTO) throws Exception {

        if (depositRepository.countDepositByDepositIdAndCustomerId(depositDTO.depositId(), depositDTO.customerId()) > 0) {
            throw new MultipleRequestException(new ExceptionMessage(depositDTO.depositId(), depositDTO.customerId()));
        }

        Deposit deposit = new Deposit();
        deposit.setDepositId(depositDTO.depositId());
        deposit.setCustomerId(depositDTO.customerId());
        deposit.setLoadAmount(depositDTO.loadAmount());
        deposit.setRequestTime(depositDTO.requestTime());


/**
 *          Each customer is subject to three limits:
 *          A maximum of $5,000 can be loaded per day
 *          A maximum of $20,000 can be loaded per week
 *          A maximum of 3 loads can be performed per day, regardless of amount
 *
 */

        Integer customerId = depositDTO.customerId();

        Pair<LocalDateTime, LocalDateTime> startAndEndOfDay = DateUtil.getStartAndEndOfDay(deposit.getRequestTime());


        List<Deposit> responseForTheDay = depositRepository.findDepositByCustomerIdAndRequestTimeGreaterThanEqualAndRequestTimeLessThanEqual(customerId, startAndEndOfDay.getFirst(), startAndEndOfDay.getSecond());
        Double dailySum = responseForTheDay.stream().mapToDouble(Deposit::getLoadAmount).sum();
        int dailyCount = responseForTheDay.size();
        logger.debug("Customer Id: {} -> Daily Sum: {}, Daily Count {}", customerId, dailySum, dailyCount);

        Pair<LocalDateTime, LocalDateTime> startAndEndOfWeek = DateUtil.getStartAndEndOfWeek(deposit.getRequestTime());
        List<Deposit> responseForTheWeek = depositRepository.findDepositByCustomerIdAndRequestTimeGreaterThanEqualAndRequestTimeLessThanEqual(customerId, startAndEndOfWeek.getFirst(), startAndEndOfWeek.getSecond());
        Double weeklySum = responseForTheWeek.stream().mapToDouble(Deposit::getLoadAmount).sum();
        logger.debug("Customer Id: {} -> Weekly total: {}", customerId, weeklySum);

        if (dailyCount > 3) {
            logger.debug("Daily count: {} {}", dailyCount, ValidationResult.DAILY_DEPOSIT_COUNT_LIMIT_EXCEED);
            throw new DailyDepositCountLimitExceed(new ExceptionMessage(depositDTO.depositId(), depositDTO.customerId()));
        } else if (dailySum + deposit.getLoadAmount() > 5000) {
            logger.debug("Daily sum: {} {}", dailySum + deposit.getLoadAmount(), ValidationResult.DAILY_DEPOSIT_AMOUNT_LIMIT_EXCEED);
            throw new DailyDepositAmountExceedException(new ExceptionMessage(depositDTO.depositId(), depositDTO.customerId()));
        } else if (weeklySum + deposit.getLoadAmount() > 20000) {
            logger.debug("weekly deposit: {} {}", weeklySum + deposit.getLoadAmount(), ValidationResult.WEEKLY_DEPOSIT_AMOUNT_LIMIT_EXCEED);
            throw new WeeklyDepositAmountLimitException(new ExceptionMessage(depositDTO.depositId(), depositDTO.customerId()));
        }
        depositRepository.save(deposit);
        logger.info(objectMapper.writeValueAsString(new DepositOutputDTO(deposit.getDepositId(), deposit.getCustomerId(), true)));
    }

}
