package com.tryvalut.api.deposit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tryvalut.api.deposit.dtos.DepositDTO;
import com.tryvalut.api.deposit.exception.DailyDepositCountLimitExceed;
import com.tryvalut.api.deposit.exception.DepositException;
import com.tryvalut.api.deposit.exception.MultipleRequestException;
import com.tryvalut.api.deposit.model.Deposit;
import com.tryvalut.api.deposit.repository.DepositRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
//@MockitoSettings(strictness = Strictness.LENIENT)
class DepositServiceTest {

    @Mock
    private DepositRepository depositRepository;

    @InjectMocks
    private DepositService depositService;
    private DepositDTO depositDTO;
    private Deposit deposit;
    @Mock
    private ObjectMapper mapper;


    @BeforeEach
    void setUp() {
        depositDTO = new DepositDTO(12345, 6789, 500.0, LocalDateTime.now());
        deposit = new Deposit();
        deposit.setDepositId(12345);
        deposit.setCustomerId(6789);
        deposit.setLoadAmount(500.0);
        deposit.setRequestTime(LocalDateTime.now());
    }

    @Test
    void save_shouldSaveDeposit_whenValidDepositDTO() throws Exception {
        when(depositRepository.countDepositByDepositIdAndCustomerId(anyInt(), anyInt())).thenReturn(0);
        when(depositRepository.save(any(Deposit.class))).thenReturn(deposit);

        depositService.save(depositDTO);

        verify(depositRepository, times(1)).save(any(Deposit.class));
    }
    @Test
    void save_shouldThrowMultipleRequestException_whenDuplicateDepositDTO() throws Exception {
        when(depositRepository.countDepositByDepositIdAndCustomerId(anyInt(), anyInt())).thenReturn(1);

        assertThrows(MultipleRequestException.class, () -> {
            depositService.save(depositDTO);
        });
    }

    @Test
    void save_shouldThrowDailyDepositLimitExceed_whenDailyCountExceedsLimit() throws Exception {
        when(depositRepository.countDepositByDepositIdAndCustomerId(anyInt(), anyInt())).thenReturn(0);
        when(depositRepository.findDepositByCustomerIdAndRequestTimeGreaterThanEqualAndRequestTimeLessThanEqual(anyInt(), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(List.of(

                        new Deposit(UUID.randomUUID(), 1, 123, 100.0, LocalDateTime.parse("2023-01-01T12:00:00")),
                        new Deposit(UUID.randomUUID(), 2, 123, 200.0, LocalDateTime.parse("2023-01-01T13:00:00")),
                        new Deposit(UUID.randomUUID(), 3, 123, 300.0, LocalDateTime.parse("2023-01-01T14:00:00"))
        ));
        assertThrows(DepositException.class, ()->{
            depositService.save(depositDTO);
        });

    }

    @Test
    void save_shouldThrowDepositException_whenDailyDepositSumExceeds() throws Exception {
        when(depositRepository.countDepositByDepositIdAndCustomerId(anyInt(), anyInt())).thenReturn(0);
        when(depositRepository.findDepositByCustomerIdAndRequestTimeGreaterThanEqualAndRequestTimeLessThanEqual(anyInt(), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(List.of(

                new Deposit(UUID.randomUUID(), 1, 123, 3000.0, LocalDateTime.parse("2023-01-01T12:00:00")),
                new Deposit(UUID.randomUUID(), 2, 123, 2000.0, LocalDateTime.parse("2023-01-01T13:00:00"))
        ));
        assertThrows(DepositException.class, ()->{
            depositService.save(depositDTO);
        });
    }

    @Test
    void save_shouldThrowDepositException_whenWeeklyDepositSumExceeds() throws Exception {
        when(depositRepository.countDepositByDepositIdAndCustomerId(anyInt(), anyInt())).thenReturn(0);
        when(depositRepository.findDepositByCustomerIdAndRequestTimeGreaterThanEqualAndRequestTimeLessThanEqual(anyInt(), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(List.of(

                new Deposit(UUID.randomUUID(), 1, 123, 300.0, LocalDateTime.parse("2023-01-01T12:00:00")),
                new Deposit(UUID.randomUUID(), 1, 123, 300.0, LocalDateTime.parse("2023-01-02T12:00:00")),
                new Deposit(UUID.randomUUID(), 1, 123, 3000.0, LocalDateTime.parse("2023-01-03T12:00:00")),
                new Deposit(UUID.randomUUID(), 2, 123, 2000.0, LocalDateTime.parse("2023-01-04T13:00:00")),
                new Deposit(UUID.randomUUID(), 2, 123, 2000.0, LocalDateTime.parse("2023-01-05T13:00:00"))
        ));
        assertThrows(DepositException.class, ()->{
            depositService.save(depositDTO);
        });
    }

    @Test
    void testGetDepositSumForCustomerWithinPeriod() {
        // Setup
        Integer customerId = 123;

        LocalDateTime start = LocalDateTime.parse("2023-01-01T00:00:00");
        LocalDateTime end = LocalDateTime.parse("2023-01-31T23:59:59");

        List<Deposit> deposits = Arrays.asList(
                new Deposit(UUID.randomUUID(), 1, customerId, 100.0, LocalDateTime.parse("2023-01-01T12:00:00")),
                new Deposit(UUID.randomUUID(), 2, customerId, 200.0, LocalDateTime.parse("2023-01-15T12:00:00")),
                new Deposit(UUID.randomUUID(), 3, customerId, 300.0, LocalDateTime.parse("2023-02-01T12:00:00"))
        );

        when(depositRepository.findDepositByCustomerIdAndRequestTimeGreaterThanEqualAndRequestTimeLessThanEqual(customerId, start, end)).thenReturn(deposits);

        // Execution
        Double result = depositService.getDepositSumForCustomerWithinPeriod(customerId, start, end);

        // Verification
        assertEquals(600, result, 0.001);
    }

    @Test
    void testGetDepositSumForCustomerWithinPeriodWithNoDeposits() {
        // Setup
        Integer customerId = 123;
        LocalDateTime start = LocalDateTime.parse("2023-01-01T00:00:00");
        LocalDateTime end = LocalDateTime.parse("2023-01-31T23:59:59");

        when(depositRepository.findDepositByCustomerIdAndRequestTimeGreaterThanEqualAndRequestTimeLessThanEqual(customerId, start, end)).thenReturn(Collections.emptyList());

        // Execution
        Double result = depositService.getDepositSumForCustomerWithinPeriod(customerId, start, end);

        // Verification
        assertEquals(0.0, result, 0.001);
    }



    @Test
    void testGetDepositSumForCustomerWithinPeriodWithOneDeposit() {
        // Setup
        Integer customerId = 123;
        LocalDateTime start = LocalDateTime.parse("2023-01-01T00:00:00");
        LocalDateTime end = LocalDateTime.parse("2023-01-31T23:59:59");

        List<Deposit> deposits = Collections.singletonList(
                new Deposit(UUID.randomUUID(), 1, customerId, 100.0, LocalDateTime.parse("2023-01-01T12:00:00"))
        );

        when(depositRepository.findDepositByCustomerIdAndRequestTimeGreaterThanEqualAndRequestTimeLessThanEqual(customerId, start, end)).thenReturn(deposits);

        // Execution
        Double result = depositService.getDepositSumForCustomerWithinPeriod(customerId, start, end);

        // Verification
        assertEquals(100.0, result, 0.001);
    }

    @Test
    void testGetDepositCountForCustomerWithinPeriod(){
        Integer customerId = 123;
        LocalDateTime start = LocalDateTime.parse("2023-01-01T00:00:00");
        LocalDateTime end = LocalDateTime.parse("2023-01-31T23:59:59");

        List<Deposit> deposits = Arrays.asList(
                new Deposit(UUID.randomUUID(), 1, customerId, 100.0, LocalDateTime.parse("2023-01-01T12:00:00")),
                new Deposit(UUID.randomUUID(), 2, customerId, 200.0, LocalDateTime.parse("2023-01-15T12:00:00")),
                new Deposit(UUID.randomUUID(), 3, customerId, 300.0, LocalDateTime.parse("2023-02-01T12:00:00"))
        );

        when(depositRepository.findDepositByCustomerIdAndRequestTimeGreaterThanEqualAndRequestTimeLessThanEqual(customerId, start, end)).thenReturn(deposits);

        // Execution
        Integer result = depositService.getDepositCountForCustomerWithinPeriod(customerId, start, end);

        // Verification
        assertEquals(3, result);
    }

    @Test
    void testGetDepositCountForCustomerWithiNoDeposits() {
        // Setup
        Integer customerId = 123;
        LocalDateTime start = LocalDateTime.parse("2023-01-01T00:00:00");
        LocalDateTime end = LocalDateTime.parse("2023-01-31T23:59:59");

        when(depositRepository.findDepositByCustomerIdAndRequestTimeGreaterThanEqualAndRequestTimeLessThanEqual(customerId, start, end)).thenReturn(Collections.emptyList());

        // Execution
        Integer result = depositService.getDepositCountForCustomerWithinPeriod(customerId, start, end);

        // Verification
        assertEquals(0.0, result, 0.001);
    }



}