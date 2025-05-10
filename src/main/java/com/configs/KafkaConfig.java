package com.configs;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.consumer.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${spring.kafka.consumer.auto-offset-reset}")
    private String autoOffsetReset;

    /**
     * Cấu hình ConsumerFactory với các thông số tối ưu
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        
        // Tối ưu performance
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500); // Số record tối đa mỗi lần poll
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1); // Số bytes tối thiểu để fetch
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500); // Thời gian chờ tối đa để fetch
        
        // Tối ưu memory
        props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, 1048576); // 1MB
        props.put(ConsumerConfig.RECEIVE_BUFFER_CONFIG, 32768); // 32KB
        
        // Tối ưu reliability
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // Disable auto commit
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed"); // Chỉ đọc message đã commit
        
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * Cấu hình KafkaListenerContainerFactory với các tính năng nâng cao
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        
        // Cấu hình số lượng consumer threads
        factory.setConcurrency(3);
        
        // Cấu hình batch listener
        factory.setBatchListener(true);
        
        // Cấu hình acknowledgment mode
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        
        // Cấu hình error handler
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
            (record, exception) -> {
                // Custom error handling logic
                System.err.println("Error in kafka listener: " + exception.getMessage());
            },
            new FixedBackOff(1000L, 3) // Retry 3 times with 1 second delay
        );
        factory.setCommonErrorHandler(errorHandler);
        
        return factory;
    }
} 