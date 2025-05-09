package com.repos;

import com.entitys.TransactionErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransactionErrorLogRepository extends JpaRepository<TransactionErrorLog, UUID> {
}
