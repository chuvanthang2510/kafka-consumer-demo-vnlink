package com.service;

import com.entitys.TransactionErrorLog;
import com.entitys.TransactionMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.repos.TransactionErrorLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
@RequiredArgsConstructor
public class TransactionProcessorService {

    private TransactionErrorLogRepository errorLogRepository;

    private JdbcTemplate jdbcTemplate; // hoặc JPA repository nếu bạn dùng ORM

    private KafkaTemplate<String, String> kafkaTemplate;

    private static final BlockingQueue<TransactionMessage> queue = new LinkedBlockingQueue<>(10000); // Giới hạn 10000 giao dịch

    private final ThreadPoolConfigService threadPoolConfigService;

    public void processTransaction(String message) {
        // Đảm bảo rằng thread pool được điều chỉnh nếu cấu hình DB thay đổi
        threadPoolConfigService.adjustPoolSizeIfChanged();

        // Chuyển giao dịch vào Queue để đợi xử lý
        try {
            queue.put(new TransactionMessage(message));  // Sử dụng BlockingQueue để đẩy giao dịch vào queue
        } catch (InterruptedException e) {
            // Log lỗi nếu không thể thêm vào queue
            System.err.println("Error adding transaction to queue: " + e.getMessage());
        }

        // Submit task vào thread pool
        threadPoolConfigService.submitTask(() -> {
            try {
                // Lấy giao dịch từ queue và xử lý
                TransactionMessage transactionMessage = queue.take();  // Lấy giao dịch từ queue
                // Xử lý giao dịch
                processAndSaveTransaction(transactionMessage);
            } catch (Exception e) {
                // Gửi message lỗi sang DLT
                kafkaTemplate.send("transaction_error", message);
                // Không ack, để offset không commit (tuỳ config, hoặc commit thủ công)
            }
        });
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
                    UUID.randomUUID(),
                    extractTransactionId(transactionMessage.getMessage()),
                    e.getMessage(),
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


