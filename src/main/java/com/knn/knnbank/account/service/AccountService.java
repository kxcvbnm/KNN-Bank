package com.knn.knnbank.account.service;

import java.util.List;

import com.knn.knnbank.account.dtos.AccountDTO;
import com.knn.knnbank.account.entity.Account;
import com.knn.knnbank.auth_users.entity.User;
import com.knn.knnbank.enums.AccountType;
import com.knn.knnbank.response.Response;

public interface AccountService {
    
    Account createAccount(AccountType accountType, User user);
    
    Response<List<AccountDTO>> getMyAccounts();
    
    Response<?> closeAccount(String accountNumber);
}
