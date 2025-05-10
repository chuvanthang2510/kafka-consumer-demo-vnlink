# Transaction Consumer Service - Business Flow Diagrams

## 1. Tổng Quan Luồng Xử Lý

```mermaid
graph TD
    A[Kafka Topic: transaction_logs] --> B[Transaction Consumer]
    B --> C{Validation}
    C -->|Valid| D[Processing]
    C -->|Invalid| E[Dead Letter Queue]
    D --> F{Circuit Breaker}
    F -->|Closed| G[Process Transaction]
    F -->|Open| H[Fallback Processing]
    G --> I{Success?}
    I -->|Yes| J[Commit Offset]
    I -->|No| K{Retryable?}
    K -->|Yes| L[Retry Topic]
    K -->|No| E
    L --> B
```

## 2. Chi Tiết Xử Lý Message

```mermaid
sequenceDiagram
    participant K as Kafka
    participant C as Consumer
    participant V as Validator
    participant P as Processor
    participant CB as Circuit Breaker
    participant R as Retry Handler
    participant DLQ as Dead Letter Queue

    K->>C: Batch Messages
    C->>V: Validate Messages
    V-->>C: Validation Result
    
    loop For Each Message
        C->>P: Process Message
        P->>CB: Check Circuit
        CB-->>P: Circuit Status
        
        alt Circuit Closed
            P->>P: Process Transaction
            alt Success
                P-->>C: Success
            else Failure
                P->>R: Check Retry
                alt Retryable
                    R->>K: Send to Retry Topic
                else Non-Retryable
                    R->>DLQ: Send to DLQ
                end
            end
        else Circuit Open
            P->>P: Fallback Processing
        end
    end
    
    C->>K: Commit Offset
```

## 3. Thread Pool Management

```mermaid
graph TD
    A[Thread Pool Config] --> B{Check Load}
    B -->|High Load| C[Increase Threads]
    B -->|Low Load| D[Decrease Threads]
    B -->|Normal| E[Maintain Current]
    
    C --> F[Update DB Config]
    D --> F
    E --> F
    
    F --> G[Thread Pool Executor]
    G --> H[Process Messages]
    H --> I[Complete Processing]
    I --> B
```

## 4. Error Handling Flow

```mermaid
graph TD
    A[Error Occurs] --> B{Error Type}
    B -->|Retryable| C[Retry Handler]
    B -->|Non-Retryable| D[Error Logger]
    
    C --> E{Retry Count}
    E -->|Under Limit| F[Exponential Backoff]
    E -->|Over Limit| G[Dead Letter Queue]
    
    F --> H[Retry Processing]
    H --> I{Success?}
    I -->|Yes| J[Complete]
    I -->|No| E
    
    D --> K[Log to Database]
    K --> L[Alert if Critical]
    L --> M[Monitoring Dashboard]
```

## 5. Monitoring và Metrics Flow

```mermaid
graph TD
    A[Service] --> B[Business Metrics]
    A --> C[System Metrics]
    A --> D[Health Checks]
    
    B --> E[Success Rate]
    B --> F[Error Rate]
    B --> G[Processing Time]
    
    C --> H[Thread Usage]
    C --> I[Memory Usage]
    C --> J[Network Stats]
    
    D --> K[Service Health]
    D --> L[Kafka Health]
    D --> M[DB Health]
    
    E --> N[Prometheus]
    F --> N
    G --> N
    H --> N
    I --> N
    J --> N
    K --> N
    L --> N
    M --> N
    
    N --> O[Grafana Dashboard]
    N --> P[Alert Manager]
```

## 6. Circuit Breaker States

```mermaid
stateDiagram-v2
    [*] --> Closed
    Closed --> Open: Failure Rate > Threshold
    Open --> HalfOpen: Wait Duration
    HalfOpen --> Closed: Success
    HalfOpen --> Open: Failure
    Closed --> [*]
    Open --> [*]
    HalfOpen --> [*]
```

## 7. Batch Processing Flow

```mermaid
graph TD
    A[Kafka Topic] --> B[Batch Consumer]
    B --> C[Message Buffer]
    C --> D{Buffer Full?}
    D -->|Yes| E[Process Batch]
    D -->|No| F[Wait for More]
    F --> C
    
    E --> G[Thread Pool]
    G --> H[Parallel Processing]
    H --> I[Batch Complete]
    I --> J[Commit Offset]
    J --> K[Clear Buffer]
    K --> C
```

## 8. Resource Management

```mermaid
graph TD
    A[Resource Monitor] --> B{Check Resources}
    B -->|High Memory| C[Reduce Batch Size]
    B -->|High CPU| D[Adjust Threads]
    B -->|High Network| E[Optimize Fetch]
    
    C --> F[Update Config]
    D --> F
    E --> F
    
    F --> G[Apply Changes]
    G --> H[Monitor Impact]
    H --> A
```

## Chú Thích

1. **Tổng Quan Luồng Xử Lý**:
   - Hiển thị luồng chính của service từ khi nhận message đến khi xử lý xong
   - Bao gồm các điểm quyết định chính và xử lý lỗi

2. **Chi Tiết Xử Lý Message**:
   - Sequence diagram chi tiết về cách xử lý từng message
   - Hiển thị tương tác giữa các component

3. **Thread Pool Management**:
   - Flow của việc quản lý thread pool
   - Cách điều chỉnh số lượng thread dựa trên tải

4. **Error Handling Flow**:
   - Chi tiết về cách xử lý các loại lỗi khác nhau
   - Flow của retry mechanism và dead letter queue

5. **Monitoring và Metrics**:
   - Các loại metrics được thu thập
   - Flow của dữ liệu monitoring

6. **Circuit Breaker States**:
   - Các trạng thái của circuit breaker
   - Điều kiện chuyển đổi giữa các trạng thái

7. **Batch Processing Flow**:
   - Chi tiết về cách xử lý batch
   - Flow của buffer và commit

8. **Resource Management**:
   - Cách quản lý và tối ưu tài nguyên
   - Flow của việc điều chỉnh cấu hình 