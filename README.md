# Transaction Consumer Service

## Tổng Quan
Đây là một service xử lý giao dịch (transaction) sử dụng Kafka Consumer, được thiết kế với các tính năng nâng cao như circuit breaker, retry mechanism, và monitoring. Service này được xây dựng trên Spring Boot và tuân thủ các best practices trong việc xử lý message.

## Công Nghệ Sử Dụng
- Java 8+
- Spring Boot
- Apache Kafka
- Resilience4j (Circuit Breaker, Retry)
- Micrometer (Metrics)
- Prometheus (Monitoring)
- Spring Data JPA
- Lombok

## Cấu Trúc Project
```
src/main/java/com/
├── configs/
│   └── KafkaConfig.java
├── entitys/
│   ├── TransactionErrorLog.java
│   └── ThreadConfig.java
├── listeners/
│   └── TransactionListener.java
├── repos/
│   ├── TransactionErrorLogRepository.java
│   └── ThreadConfigRepository.java
├── service/
│   ├── ThreadPoolConfigService.java
│   └── TransactionProcessorService.java
└── TransactionConsumerApplication.java
```

## Chi Tiết Các Component

### 1. KafkaConfig
Cấu hình Kafka Consumer với các tối ưu hóa:
- **Performance Optimization**:
  - `max.poll.records`: 500 records mỗi lần poll
  - `fetch.min.bytes`: 1 byte tối thiểu
  - `fetch.max.wait.ms`: 500ms thời gian chờ tối đa

- **Memory Optimization**:
  - `max.partition.fetch.bytes`: 1MB
  - `receive.buffer.bytes`: 32KB

- **Reliability**:
  - Disable auto commit
  - Isolation level: read_committed

### 2. TransactionListener
Xử lý message từ Kafka với các tính năng:
- Batch processing
- Asynchronous processing
- Circuit breaker
- Retry mechanism
- Error handling
- Metrics collection

#### Luồng Xử Lý Message:
1. Nhận batch messages
2. Validate message
3. Xử lý bất đồng bộ với thread pool
4. Circuit breaker và retry nếu cần
5. Ghi log lỗi nếu có
6. Commit offset sau khi xử lý thành công

### 3. ThreadPoolConfigService
Quản lý thread pool động:
- Tự động điều chỉnh số lượng thread
- Cấu hình thread pool từ database
- Graceful shutdown
- Task submission và execution

### 4. Cấu Hình (application.yml)
#### Kafka Consumer:
```yaml
spring.kafka.consumer:
  bootstrap-servers: localhost:9092
  group-id: transaction-group
  auto-offset-reset: earliest
```

#### Resilience4j:
- Circuit Breaker:
  - Failure rate threshold: 50%
  - Minimum calls: 10
  - Wait duration: 5s
  - Half-open state calls: 3

- Retry:
  - Max attempts: 3
  - Wait duration: 1s
  - Exponential backoff

#### Monitoring:
- Prometheus metrics
- Health checks
- Detailed logging

## Tính Năng Nổi Bật

### 1. Xử Lý Message Hiệu Quả
- Batch processing để tối ưu throughput
- Asynchronous processing với thread pool
- Tự động điều chỉnh thread pool size

### 2. Fault Tolerance
- Circuit breaker để ngăn chặn lỗi dây chuyền
- Retry mechanism với exponential backoff
- Dead letter queue cho message lỗi
- Error logging và monitoring

### 3. Monitoring và Metrics
- Prometheus metrics
- Health checks
- Detailed logging
- Performance metrics

### 4. Tối Ưu Hóa
- Memory usage
- Network efficiency
- Thread management
- Error handling

## Cách Sử Dụng

### 1. Cấu Hình
Chỉnh sửa `application.yml` với các thông số phù hợp:
```yaml
spring:
  kafka:
    consumer:
      bootstrap-servers: your-kafka-servers
      group-id: your-group-id
```

### 2. Chạy Service
```bash
./mvnw spring-boot:run
```

### 3. Monitoring
- Health check: `http://localhost:8080/actuator/health`
- Metrics: `http://localhost:8080/actuator/prometheus`

## Best Practices Đã Áp Dụng

1. **Performance**:
   - Batch processing
   - Asynchronous processing
   - Optimized Kafka configurations

2. **Reliability**:
   - Circuit breaker
   - Retry mechanism
   - Error handling
   - Dead letter queue

3. **Monitoring**:
   - Prometheus metrics
   - Health checks
   - Detailed logging

4. **Maintainability**:
   - Clean code structure
   - Comprehensive documentation
   - Configurable parameters

## Lưu Ý Khi Triển Khai

1. **Resource Requirements**:
   - Đủ memory cho batch processing
   - CPU cho thread pool
   - Network bandwidth cho Kafka

2. **Monitoring**:
   - Theo dõi metrics
   - Alert khi có vấn đề
   - Log rotation

3. **Scaling**:
   - Điều chỉnh thread pool size
   - Tối ưu batch size
   - Monitor performance

## Troubleshooting

### Common Issues:
1. **High Latency**:
   - Kiểm tra thread pool size
   - Điều chỉnh batch size
   - Monitor network latency

2. **Memory Issues**:
   - Kiểm tra batch size
   - Monitor heap usage
   - Adjust buffer sizes

3. **Connection Issues**:
   - Kiểm tra Kafka connectivity
   - Verify network settings
   - Monitor circuit breaker status 