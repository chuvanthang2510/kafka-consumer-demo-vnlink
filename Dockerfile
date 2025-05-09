# Sử dụng image OpenJDK 11 làm base image
FROM openjdk:11-jdk-slim

# Thiết lập thư mục làm việc trong container
WORKDIR /app

# Sao chép file JAR vào container (sửa đúng tên và thêm .jar)
COPY target/consumer-app-1.0-SNAPSHOT.jar /app/consumer-app-1.0-SNAPSHOT.jar

# Lệnh chạy ứng dụng khi container khởi động
CMD ["java", "-jar", "consumer-app-1.0-SNAPSHOT.jar"]
