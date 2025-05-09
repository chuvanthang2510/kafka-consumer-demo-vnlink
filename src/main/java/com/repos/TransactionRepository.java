package com.repos;

import com.entitys.TransactionDemo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<TransactionDemo, String> {
}