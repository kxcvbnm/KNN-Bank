package com.knn.knnbank.transaction.service;

import java.util.List;

import com.knn.knnbank.response.Response;
import com.knn.knnbank.transaction.dtos.TransactionDTO;
import com.knn.knnbank.transaction.dtos.TransactionRequest;

public interface TransactionService {
    
    Response<?> createTransaction(TransactionRequest transactionRequest);

    Response<List<TransactionDTO>> getTransactionsForMyAccount(String accountNumber, int page, int size);
}
