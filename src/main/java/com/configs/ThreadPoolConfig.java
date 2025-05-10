package com.configs;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.ThreadPoolExecutor;
@Configuration
public class ThreadPoolConfig {

    private final MeterRegistry meterRegistry;

    public ThreadPoolConfig(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("transaction-processor-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        // 👉 Phải initialize trước khi dùng getThreadPoolExecutor
        executor.initialize();

        // Đăng ký metric sau khi executor đã được khởi tạo
        meterRegistry.gauge("threadpool.active.threads", executor,
                ThreadPoolTaskExecutor::getActiveCount);
        meterRegistry.gauge("threadpool.queue.size", executor.getThreadPoolExecutor().getQueue(),
                queue -> queue.size());

        return executor;
    }
}
