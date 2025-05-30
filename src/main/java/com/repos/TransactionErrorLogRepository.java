package com.repos;

import com.entitys.TransactionErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionErrorLogRepository extends JpaRepository<TransactionErrorLog, String> {
}
