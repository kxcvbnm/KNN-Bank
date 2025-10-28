package com.knn.knnbank.transaction.repo;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.knn.knnbank.transaction.entity.Transaction;

public interface TransactionRepo extends JpaRepository<Transaction, Long> {
    
    Page<Transaction> findByAccount_AccountNumber(String accountNumber, Pageable pageable);

    List<Transaction> findByAccount_AccountNumber(String accountNumber);
    
}
