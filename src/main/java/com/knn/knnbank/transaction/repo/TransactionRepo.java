package com.knn.knnbank.transaction.repo;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.knn.knnbank.transaction.entity.Transaction;

public interface TransactionRepo extends JpaRepository<Transaction, Long> {
    
    @Query("SELECT t FROM Transaction t WHERE t.account.accountNumber = :accountNumber " +
            "OR (t.transactionType = 'TRANSFER' AND t.destinationAccount = :accountNumber) " +
            "ORDER BY t.transactionDate DESC")
    Page<Transaction> findByAccount_AccountNumber(String accountNumber, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.account.accountNumber = :accountNumber " +
            "OR (t.transactionType = 'TRANSFER' AND t.destinationAccount = :accountNumber) " +
            "ORDER BY t.transactionDate DESC")
    List<Transaction> findByAccount_AccountNumber(String accountNumber);
    
}
