package com.knn.knnbank.audit_dashboard.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.knn.knnbank.account.dtos.AccountDTO;
import com.knn.knnbank.auth_users.dtos.UserDTO;
import com.knn.knnbank.transaction.dtos.TransactionDTO;

public interface AuditorService {
    
    Map<String, Long> getSystemTotals();

    Optional<UserDTO> findUserByEmail(String email);

    Optional<AccountDTO> findAccountDetailsByAccountNumber(String accountNumber);

    List<TransactionDTO> findTransactionsByAccountNumber(String accountNumber);

    Optional<TransactionDTO> findTransactionById(Long transactionId);
}
