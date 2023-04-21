package com.tryvalut.api.deposit.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "deposit")
public class Deposit {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "deposit_id")
    private Integer depositId;

    @Column(name = "customer_id")
    private Integer customerId;

    @Column(name = "load_amount")
    private Double loadAmount;

    @Column(name = "load_time")
    private LocalDateTime requestTime;

}
