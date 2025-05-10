package com.service;

import com.entitys.ThreadConfig;
import com.repos.ThreadConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class ThreadPoolConfigService {

    private final ThreadConfigRepository threadConfigRepository;
    private final ThreadPoolTaskExecutor taskExecutor;
    private ThreadPoolExecutor executor;
    private final AtomicInteger currentThreadCount = new AtomicInteger();

    /**
     * Lấy số lượng thread từ cấu hình database
     */
    private int fetchThreadCountFromDB() {
        return threadConfigRepository.findById("consumer-thread-pool")
                .map(ThreadConfig::getValue)
                .orElse(5); // Default to 5 threads if not configured
    }

    /**
     * Khởi tạo thread pool khi ứng dụng start
     */
    @PostConstruct
    public void initializeThreadPool() {
        int initialSize = fetchThreadCountFromDB();
        this.executor = new ThreadPoolExecutor(
                initialSize, initialSize,
                60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(10000),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        this.currentThreadCount.set(initialSize);
        log.info("Thread pool initialized with {} threads", initialSize);
    }

    /**
     * Điều chỉnh kích thước thread pool dựa trên tải hệ thống
     */
    public void adjustPoolSizeIfChanged() {
        int dbSize = fetchThreadCountFromDB();
        if (dbSize != currentThreadCount.get()) {
            executor.setCorePoolSize(dbSize);
            executor.setMaximumPoolSize(dbSize);
            currentThreadCount.set(dbSize);
            log.info("Updated thread pool size to: {}", dbSize);
        }
    }

    /**
     * Submit task vào thread pool
     */
    public void submitTask(Runnable task) {
        taskExecutor.execute(task);
    }

    /**
     * Lấy task executor để sử dụng với CompletableFuture
     */
    public ThreadPoolTaskExecutor getTaskExecutor() {
        return taskExecutor;
    }

    /**
     * Graceful shutdown thread pool
     */
    public void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            log.info("Thread pool shut down successfully");
        }
    }
}
