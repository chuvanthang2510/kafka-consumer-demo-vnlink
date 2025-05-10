package com.service;

import com.entitys.TransactionErrorLog;
import com.entitys.TransactionMessage;
import com.entitys.TransactionDemo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.repos.TransactionErrorLogRepository;
import com.repos.TransactionDemoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionProcessorService {

    private TransactionErrorLogRepository errorLogRepository;

    private JdbcTemplate jdbcTemplate; // hoặc JPA repository nếu bạn dùng ORM

    private KafkaTemplate<String, String> kafkaTemplate;

    private static final BlockingQueue<TransactionMessage> queue = new LinkedBlockingQueue<>(10000); // Giới hạn 10000 giao dịch

    private final ThreadPoolConfigService threadPoolConfigService;

    private final TransactionDemoRepository transactionDemoRepository;

    @Transactional
    public void processTransaction(String message) {
        try {
            // Parse message and create transaction
            TransactionDemo transaction = parseTransaction(message);
            
            // Save transaction
            transactionDemoRepository.save(transaction);
            
            log.info("Successfully processed transaction: {}", transaction.getId());
        } catch (Exception e) {
            log.error("Error processing transaction: {}", e.getMessage());
            throw new RuntimeException("Failed to process transaction", e);
        }
    }

    private TransactionDemo parseTransaction(String message) {
        // Implement message parsing logic
        // This is a placeholder for actual implementation
        TransactionDemo transaction = new TransactionDemo();
        // Set transaction properties from message
        return transaction;
    }

    private void processAndSaveTransaction(TransactionMessage transactionMessage) {
        try {
            // Parse JSON
            JsonNode node = new ObjectMapper().readTree(transactionMessage.getMessage());
            String id = node.get("transactionId").asText();
            double amount = node.get("amount").asDouble();
            Instant timestamp = Instant.parse(node.get("timestamp").asText());

            // Save vào DB thật
            jdbcTemplate.update(
                    "INSERT INTO transactions (id, amount, timestamp) VALUES (?, ?, ?)",
                    id, amount, timestamp
            );
        } catch (Exception e) {
            // Ghi vào bảng log
            errorLogRepository.save(new TransactionErrorLog(
                    UUID.randomUUID().toString(),
                    extractTransactionId(transactionMessage.getMessage()),
                    transactionMessage.getMessage(),
                    Instant.now()
            ));
            // Throw để KafkaListener biết là lỗi
            throw new RuntimeException(e);
        }
    }

    private String extractTransactionId(String message) {
        try {
            return new ObjectMapper().readTree(message).get("transactionId").asText();
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }
}


