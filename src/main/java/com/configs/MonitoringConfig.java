package com.configs;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MonitoringConfig {

    private final MeterRegistry meterRegistry;

    public MonitoringConfig(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Bean
    public Timer transactionProcessingTimer() {
        return Timer.builder("transaction.processing.time")
                .description("Time taken to process a transaction")
                .register(meterRegistry);
    }

    @Bean
    public io.micrometer.core.instrument.Counter successCounter() {
        return io.micrometer.core.instrument.Counter.builder("transaction.success")
                .description("Number of successful transactions")
                .register(meterRegistry);
    }

    @Bean
    public io.micrometer.core.instrument.Counter errorCounter() {
        return io.micrometer.core.instrument.Counter.builder("transaction.error")
                .description("Number of failed transactions")
                .register(meterRegistry);
    }
} 