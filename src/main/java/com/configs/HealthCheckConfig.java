package com.configs;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
public class HealthCheckConfig implements HealthIndicator {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ThreadPoolTaskExecutor taskExecutor;

    public HealthCheckConfig(KafkaTemplate<String, String> kafkaTemplate, ThreadPoolTaskExecutor taskExecutor) {
        this.kafkaTemplate = kafkaTemplate;
        this.taskExecutor = taskExecutor;
    }

    @Override
    public Health health() {
        try {
            // Check Kafka connection
            kafkaTemplate.getDefaultTopic();
            
            // Check thread pool status
            int activeThreads = taskExecutor.getActiveCount();
            int poolSize = taskExecutor.getPoolSize();
            int queueSize = taskExecutor.getThreadPoolExecutor().getQueue().size();
            
            return Health.up()
                    .withDetail("kafka", "connected")
                    .withDetail("threadPool.activeThreads", activeThreads)
                    .withDetail("threadPool.poolSize", poolSize)
                    .withDetail("threadPool.queueSize", queueSize)
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withException(e)
                    .build();
        }
    }
} 