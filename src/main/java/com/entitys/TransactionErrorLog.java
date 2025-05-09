package com.entitys;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transaction_error_log")
@Data
public class TransactionErrorLog {

    @Id
    private UUID id;

    private String transactionId;

    @Lob
    private String errorMessage;

    @Lob
    private String rawData;

    private Instant createdAt;

    public TransactionErrorLog(UUID randomUUID, String extractTransactionId, String message, String message1, Instant now) {
    }
}
