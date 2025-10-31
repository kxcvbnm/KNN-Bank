package com.knn.knnbank.account.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.knn.knnbank.account.dtos.AccountDTO;
import com.knn.knnbank.account.entity.Account;
import com.knn.knnbank.account.repo.AccountRepo;
import com.knn.knnbank.auth_users.entity.User;
import com.knn.knnbank.auth_users.service.UserService;
import com.knn.knnbank.enums.AccountStatus;
import com.knn.knnbank.enums.AccountType;
import com.knn.knnbank.enums.Currency;
import com.knn.knnbank.exceptions.BadRequestException;
import com.knn.knnbank.exceptions.NotFoundException;
import com.knn.knnbank.response.Response;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AccountServiceImpl implements AccountService {
    
    private final AccountRepo accountRepo;
    private final UserService userService;
    private final ModelMapper modelMapper;

    private final Random random = new Random();

    @Override
    public Account createAccount(AccountType accountType, User user) {
        
        log.info("Creating account for user: {}", user.getEmail());

        String accountNumber = generateAccountNumber();

        Account account = Account.builder()
            .accountNumber(accountNumber)
            .accountType(accountType)
            .currency(Currency.THB)
            .balance(BigDecimal.ZERO)
            .status(AccountStatus.ACTIVE)
            .user(user)
            .createdAt(LocalDateTime.now())
            .build();
        
        return accountRepo.save(account);
    }

    @Override
    public Response<List<AccountDTO>> getMyAccounts() {

        User user = userService.getCurrentLoggedInUser();

        List<AccountDTO> accounts = accountRepo.findByUserId(user.getId())
            .stream()
            .map(account -> modelMapper.map(account, AccountDTO.class))
            .toList();

        return Response.<List<AccountDTO>>builder()
            .statusCode(HttpStatus.OK.value())
            .message("Accounts retrieved successfully")
            .data(accounts)
            .build();
    }

    @Override
    public Response<?> closeAccount(String accountNumber) {
        
        User user = userService.getCurrentLoggedInUser();

        Account account = accountRepo.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new NotFoundException("Account not found"));

        if(!user.getAccounts().contains(account)) {
            throw new NotFoundException("Account is not yours");
        }

        if(account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new BadRequestException("Must withdraw all funds before closing account");
        }

        account.setStatus(AccountStatus.CLOSED);
        account.setClosedAt(LocalDateTime.now());
        accountRepo.save(account);

        return Response.builder()
            .statusCode(HttpStatus.OK.value())
            .message("Account closed successfully")
            .build();
    }

    private String generateAccountNumber() {

        String accountNumber;

        // Generate a random 8 digit number
        do{
            accountNumber = "66" + (random.nextInt(90000000) + 10000000);

        } while(accountRepo.findByAccountNumber(accountNumber).isPresent());

        log.info("Generated account number: {}", accountNumber);
        return accountNumber;
    }

}
