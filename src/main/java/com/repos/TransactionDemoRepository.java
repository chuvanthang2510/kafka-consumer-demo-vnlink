package com.repos;

import com.entitys.TransactionDemo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionDemoRepository extends JpaRepository<TransactionDemo, String> {
} 