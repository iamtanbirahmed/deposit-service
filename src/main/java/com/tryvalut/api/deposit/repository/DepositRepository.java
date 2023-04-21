package com.tryvalut.api.deposit.repository;

import com.tryvalut.api.deposit.model.Deposit;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface DepositRepository extends CrudRepository<Deposit, UUID> {

    Iterable<Deposit> findDepositByLoadAmountGreaterThan(Double amount);

    Iterable<Deposit> findDepositByCustomerId(Integer customerId);

    @Query("SELECT SUM(d.loadAmount) FROM Deposit d WHERE d.customerId = :customerId")
    Double findTotalLoadAmountForCustomer(Integer customerId);

    @Query("SELECT DISTINCT d.customerId FROM Deposit d")
    List<String> findUniqueCustomerIds();

    long count();

    @Query("SELECT d.customerId, d.loadAmount " +
            "FROM Deposit d " +
            "WHERE d.requestTime >= :startDate AND + d.requestTime <= :endDate AND d.customerId=:customerId"

    )
    List<Object[]> findTransactionCountByCustomerIdByDay(Integer customerId, LocalDateTime startDate, LocalDateTime endDate);


    Integer countDepositByDepositIdAndCustomerId(Integer depositId, Integer customerId);

    List<Deposit> findDepositByCustomerIdAndRequestTimeGreaterThanEqualAndRequestTimeLessThanEqual(Integer customerId, LocalDateTime startDate, LocalDateTime endDate);

}
