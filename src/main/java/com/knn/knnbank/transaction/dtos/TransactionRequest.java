package com.knn.knnbank.transaction.dtos;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.knn.knnbank.enums.TransactionType;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionRequest {
    
    private TransactionType transactionType;

    private BigDecimal amount;

    private String accountNumber;

    private String description;

    private String destinationAccountNumber;
}
