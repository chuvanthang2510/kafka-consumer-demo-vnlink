package com.listeners;

import com.repos.TransactionErrorLogRepository;
import com.service.ThreadPoolConfigService;
import com.service.TransactionProcessorService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
@Component
@RequiredArgsConstructor
public class TransactionListener { 

    private final TransactionProcessorService transactionService;

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ThreadPoolConfigService threadPoolConfigService;

    private final TransactionErrorLogRepository transactionErrorLogRepository;

    @KafkaListener(topics = "transaction_logs", groupId = "transaction-group")
    public void listen(String message, Acknowledgment ack) {
        // Đảm bảo rằng thread pool được điều chỉnh nếu cấu hình DB thay đổi
        threadPoolConfigService.adjustPoolSizeIfChanged();

        // Submit task vào thread pool để xử lý giao dịch không đồng bộ
        threadPoolConfigService.submitTask(() -> {
            try {
                // Xử lý giao dịch chu van thang
                transactionService.processTransaction(message);

                // Nếu xử lý thành công, commit offset
                ack.acknowledge();  // Chỉ gọi ack sau khi xử lý thành công
            } catch (Exception e) {
                // Gửi message lỗi sang DLT (Dead Letter Topic)
                kafkaTemplate.send("transaction_error", message);

                // Nếu có lỗi, không commit offset, giao dịch sẽ được thử lại
                // Kafka sẽ không đánh dấu là đã xử lý, giúp giao dịch bị lỗi được xử lý lại khi consumer tiếp theo đọc lại
                // Thêm logic ghi lỗi vào DB để lưu thông tin lỗi
            }
        });
    }

}
