package com.listeners;

import com.entitys.TransactionErrorLog;
import com.repos.TransactionErrorLogRepository;
import com.service.ThreadPoolConfigService;
import com.service.TransactionProcessorService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@Validated
public class TransactionListener {

    private final TransactionProcessorService transactionService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ThreadPoolConfigService threadPoolConfigService;
    private final TransactionErrorLogRepository transactionErrorLogRepository;
    private final Timer transactionProcessingTimer;
    private final Counter successCounter;
    private final Counter errorCounter;

    /**
     * Batch listener để xử lý nhiều message cùng lúc
     * @param records Danh sách các message từ Kafka
     * @param ack Acknowledgment để commit offset
     */
    @KafkaListener(topics = "${kafka.topic.transaction}", groupId = "${kafka.consumer.group-id}")
    public void listen(List<ConsumerRecord<String, String>> records, Acknowledgment ack) {
        log.info("Received batch of {} messages", records.size());
        
        // Đảm bảo thread pool được điều chỉnh
        threadPoolConfigService.adjustPoolSizeIfChanged();

        // Xử lý batch messages bất đồng bộ
        List<CompletableFuture<Void>> futures = records.stream()
            .map(record -> processMessageAsync(record, ack))
            .collect(Collectors.toList());

        // Đợi tất cả các message được xử lý
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenRun(() -> {
                log.info("Batch processing completed");
                ack.acknowledge();
            })
            .exceptionally(throwable -> {
                log.error("Error in batch processing", throwable);
                return null;
            });
    }

    /**
     * Xử lý một message bất đồng bộ
     */
    private CompletableFuture<Void> processMessageAsync(ConsumerRecord<String, String> record, Acknowledgment ack) {
        return CompletableFuture.runAsync(() -> {
            String messageId = UUID.randomUUID().toString();
            log.info("Processing message ID: {}", messageId);

            Timer.Sample sample = Timer.start();
            try {
                // Validate message
                validateMessage(record.value());

                // Process transaction with circuit breaker and retry
                processTransactionWithResilience(record.value());

                // Record success metrics
                sample.stop(transactionProcessingTimer);
                successCounter.increment();
                
                log.info("Successfully processed message ID: {}", messageId);
            } catch (Exception e) {
                // Record error metrics
                errorCounter.increment();
                log.error("Error processing message ID: {}", messageId, e);

                // Handle error based on type
                handleError(record.value(), e, messageId);
            }
        }, threadPoolConfigService.getTaskExecutor());
    }

    /**
     * Circuit breaker và retry mechanism
     */
    @CircuitBreaker(name = "transactionProcessor", fallbackMethod = "processFallback")
    @Retry(name = "transactionProcessor", fallbackMethod = "processFallback")
    private void processTransactionWithResilience(String message) {
        transactionService.processTransaction(message);
    }

    /**
     * Fallback method khi circuit breaker mở hoặc retry thất bại
     */
    private void processFallback(String message, Exception e) {
        log.warn("Fallback processing for message due to: {}", e.getMessage());
        // Implement fallback logic here
    }

    /**
     * Validate message format và content
     */
    private void validateMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be empty");
        }
        // Add more validation as needed
    }

    /**
     * Xử lý lỗi dựa trên loại lỗi
     */
    private void handleError(String message, Exception e, String messageId) {
        if (isRetryable(e)) {
            // Send to retry topic with exponential backoff
            kafkaTemplate.send("${spring.kafka.topic.retry}", messageId, message);
            log.info("Message ID: {} sent to retry topic", messageId);
        } else {
            // Send to dead letter topic
            kafkaTemplate.send("${kafka.topic.dead-letter}", messageId, message);
            // Log error to database
            transactionErrorLogRepository.save(createErrorLog(message, e, messageId));
            log.error("Message ID: {} sent to dead letter topic", messageId);
        }
    }

    /**
     * Kiểm tra xem lỗi có thể retry được không
     */
    private boolean isRetryable(Exception e) {
        return e instanceof RuntimeException && !(e instanceof IllegalArgumentException);
    }

    /**
     * Tạo error log entry
     */
    private TransactionErrorLog createErrorLog(String message, Exception e, String messageId) {
        return new TransactionErrorLog(messageId, message, e.getMessage(), Instant.now());
    }
}
