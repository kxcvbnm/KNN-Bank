package com.knn.knnbank.transaction.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.knn.knnbank.response.Response;
import com.knn.knnbank.transaction.dtos.TransactionRequest;
import com.knn.knnbank.transaction.service.TransactionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transactions")
public class TransactionController {
    
    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<Response<?>> createTransaction(@RequestBody @Valid TransactionRequest transactionRequest) {

        return ResponseEntity.ok(transactionService.createTransaction(transactionRequest));
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<Response<?>> getTransactionsForMyAccount(@PathVariable String accountNumber,
                                                                  @RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "50") int size) {

        return ResponseEntity.ok(transactionService.getTransactionsForMyAccount(accountNumber, page, size));
    }
}
