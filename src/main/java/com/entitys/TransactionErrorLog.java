package com.entitys;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "transaction_error_logs")
@Data
@NoArgsConstructor
public class TransactionErrorLog {
    @Id
    private String id;
    private String message;
    private String errorMessage;
    private Instant timestamp = Instant.now();

    public TransactionErrorLog(String id, String message, String errorMessage, Instant timestamp) {
        this.id = id;
        this.message = message;
        this.errorMessage = errorMessage;
        this.timestamp = timestamp;
    }
}
