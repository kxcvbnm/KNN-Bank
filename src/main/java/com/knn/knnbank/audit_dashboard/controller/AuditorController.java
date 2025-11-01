package com.knn.knnbank.audit_dashboard.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.knn.knnbank.account.dtos.AccountDTO;
import com.knn.knnbank.audit_dashboard.service.AuditorService;
import com.knn.knnbank.auth_users.dtos.UserDTO;
import com.knn.knnbank.transaction.dtos.TransactionDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/audit")
@PreAuthorize("hasAuthority('AUDITOR') or hasAuthority('ADMIN')")
public class AuditorController {

    private final AuditorService auditorService;

    @GetMapping("/totals")
    public ResponseEntity<Map<String, Long>> getSystemTotals() {
        
        return ResponseEntity.ok(auditorService.getSystemTotals());
    }

    @GetMapping("/users")
    public ResponseEntity<UserDTO> findUserByEmail(@RequestParam String email) {

        Optional<UserDTO> userDTO = auditorService.findUserByEmail(email);
        
        return userDTO.map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/accounts")
    public ResponseEntity<AccountDTO> findAccountDetailsByAccountNumber(@RequestParam String accountNumber) {

        Optional<AccountDTO> accountDTO = auditorService.findAccountDetailsByAccountNumber(accountNumber);
        
        return accountDTO.map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/transactions/by-account")
    public ResponseEntity<List<TransactionDTO>> findTransactionsByAccountNumber(@RequestParam String accountNumber) {

        List<TransactionDTO> transactionDTOList = auditorService.findTransactionsByAccountNumber(accountNumber);
        
        if(transactionDTOList.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(transactionDTOList);    
    }

    @GetMapping("/transactions/by-id")
    public ResponseEntity<TransactionDTO> findTransactionById(@RequestParam Long id) {

        Optional<TransactionDTO> transactionDTO = auditorService.findTransactionById(id);
        
        return transactionDTO.map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
