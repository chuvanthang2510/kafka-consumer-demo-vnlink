package com.entitys;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transactions")
@Data
public class TransactionDemo {
    @Id
    private String id;
    private Instant timestamp;
    private String userId;
    private BigDecimal amount;
}
