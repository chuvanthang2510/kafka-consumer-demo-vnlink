package com.service;

import com.entitys.ThreadConfig;
import com.repos.ThreadConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
@Service
@RequiredArgsConstructor
public class ThreadPoolConfigService {

    private final ThreadConfigRepository threadConfigRepository;
    private ThreadPoolExecutor executor;
    private final AtomicInteger currentThreadCount = new AtomicInteger();

    // Phương thức lấy số luồng từ cơ sở dữ liệu
    private int fetchThreadCountFromDB() {
        // Lấy giá trị cấu hình từ DB
        return threadConfigRepository.findById("consumer-thread-pool")
                .map(ThreadConfig::getValue)
                .orElse(5); // Mặc định là 5 nếu không có cấu hình
    }

    @PostConstruct
    public void initializeThreadPool() {
        // Lấy số luồng từ DB và khởi tạo thread pool
        int initialSize = fetchThreadCountFromDB();
        this.executor = new ThreadPoolExecutor(
                initialSize, initialSize,
                60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(10000),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        this.currentThreadCount.set(initialSize);
        System.out.println("Thread pool initialized with " + initialSize + " threads.");
    }

    // Phương thức kiểm tra và thay đổi số luồng nếu cấu hình DB thay đổi
    public void adjustPoolSizeIfChanged() {
        int dbSize = fetchThreadCountFromDB();
        if (dbSize != currentThreadCount.get()) {
            // Điều chỉnh số luồng nếu cần thiết
            executor.setCorePoolSize(dbSize);
            executor.setMaximumPoolSize(dbSize);
            currentThreadCount.set(dbSize);
            System.out.println("🔁 Updated thread pool size to: " + dbSize);
        }
    }

    // Phương thức thêm task vào ThreadPool
    public void submitTask(Runnable task) {
        executor.submit(task);
    }

    // Đảm bảo việc đóng thread pool khi ứng dụng dừng
    public void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            System.out.println("Thread pool shut down.");
        }
    }
}
