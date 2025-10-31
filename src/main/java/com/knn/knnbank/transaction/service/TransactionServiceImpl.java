package com.knn.knnbank.transaction.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.knn.knnbank.account.entity.Account;
import com.knn.knnbank.account.repo.AccountRepo;
import com.knn.knnbank.auth_users.entity.User;
import com.knn.knnbank.auth_users.service.UserService;
import com.knn.knnbank.enums.TransactionStatus;
import com.knn.knnbank.enums.TransactionType;
import com.knn.knnbank.exceptions.BadRequestException;
import com.knn.knnbank.exceptions.InsufficientBalanceException;
import com.knn.knnbank.exceptions.InvalidTransactionException;
import com.knn.knnbank.exceptions.NotFoundException;
import com.knn.knnbank.notification.dtos.NotificationDTO;
import com.knn.knnbank.notification.service.NotificationService;
import com.knn.knnbank.response.Response;
import com.knn.knnbank.transaction.dtos.TransactionDTO;
import com.knn.knnbank.transaction.dtos.TransactionRequest;
import com.knn.knnbank.transaction.entity.Transaction;
import com.knn.knnbank.transaction.repo.TransactionRepo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {
    
    private final TransactionRepo transactionRepo;
    private final AccountRepo accountRepo;
    private final NotificationService notificationService;
    private final UserService userService;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public Response<?> createTransaction(TransactionRequest transactionRequest) {

        Transaction transaction = new Transaction();
        transaction.setTransactionType(transactionRequest.getTransactionType());
        transaction.setAmount(transactionRequest.getAmount());
        transaction.setDescription(transactionRequest.getDescription());

        switch(transactionRequest.getTransactionType()) {

            case DEPOSIT -> handleDeposit(transactionRequest, transaction);
            case WITHDRAWAL -> handleWithdrawal(transactionRequest, transaction);
            case TRANSFER -> handleTransfer(transactionRequest, transaction);
            default -> throw new InvalidTransactionException("Invalid transaction type");
        }

        transaction.setStatus(TransactionStatus.SUCCESS);
        Transaction savedTransaction = transactionRepo.save(transaction);

        sendTransactionNotification(savedTransaction);

        return Response.builder()
            .statusCode(200)
            .message("Transaction created successfully")
            .build();
    }

    @Override
    public Response<List<TransactionDTO>> getTransactionsForMyAccount(String accountNumber,
                                                                      int page,
                                                                      int size) {
        User user = userService.getCurrentLoggedInUser();

        Account account = accountRepo.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new NotFoundException("Account not found"));   

        if(!account.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Account does not belong to user");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
        Page<Transaction> transactions = transactionRepo.findByAccount_AccountNumber(accountNumber, pageable);

        List<TransactionDTO> transactionDTOs = transactions.getContent().stream()
            .map(transaction -> modelMapper.map(transaction, TransactionDTO.class))
            .toList();

        return Response.<List<TransactionDTO>>builder()
            .statusCode(HttpStatus.OK.value())
            .message("Transactions retrieved successfully")
            .data(transactionDTOs)
            .meta(Map.of(
                "currentPage", transactions.getNumber(),
                "totalItems", transactions.getTotalElements(),
                "totalPages", transactions.getTotalPages(),
                "pageSize", transactions.getSize()
            ))
            .build();
    }

    private void handleDeposit(TransactionRequest transactionRequest, Transaction transaction) {
        
        Account account = accountRepo.findByAccountNumber(transactionRequest.getAccountNumber())
            .orElseThrow(() -> new NotFoundException("Account not found"));

        account.setBalance(account.getBalance().add(transactionRequest.getAmount()));
        transaction.setAccount(account);

        accountRepo.save(account);
    }

    private void handleWithdrawal(TransactionRequest transactionRequest, Transaction transaction) {
        
        Account account = accountRepo.findByAccountNumber(transactionRequest.getAccountNumber())
            .orElseThrow(() -> new NotFoundException("Account not found"));

        if(account.getBalance().compareTo(transactionRequest.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        account.setBalance(account.getBalance().subtract(transactionRequest.getAmount()));
        transaction.setAccount(account);

        accountRepo.save(account);
    }

    private void handleTransfer(TransactionRequest transactionRequest, Transaction transaction) {
        
        Account sourceAccount = accountRepo.findByAccountNumber(transactionRequest.getAccountNumber())
            .orElseThrow(() -> new NotFoundException("Account not found"));

        Account destinationAccount = accountRepo.findByAccountNumber(transactionRequest.getDestinationAccountNumber())
            .orElseThrow(() -> new NotFoundException("Destination account not found"));

        if(sourceAccount.getBalance().compareTo(transactionRequest.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        sourceAccount.setBalance(sourceAccount.getBalance().subtract(transactionRequest.getAmount()));
        accountRepo.save(sourceAccount);

        destinationAccount.setBalance(destinationAccount.getBalance().add(transactionRequest.getAmount()));
        accountRepo.save(destinationAccount);

        transaction.setAccount(sourceAccount);
        transaction.setSourceAccount(sourceAccount.getAccountNumber());
        transaction.setDestinationAccount(destinationAccount.getAccountNumber());

    }

    private void sendTransactionNotification(Transaction transaction) {

        User user = transaction.getAccount().getUser();
        String subject;
        String template;

        Map<String, Object> vars = new HashMap<>();
        vars.put("name", user.getFirstName());
        vars.put("amount", transaction.getAmount());
        vars.put("accountNumber", transaction.getAccount().getAccountNumber());
        vars.put("date", transaction.getTransactionDate());
        vars.put("balance", transaction.getAccount().getBalance());

        if(transaction.getTransactionType() == TransactionType.DEPOSIT) {
            subject = "Credit Alert";
            template = "credit-alert";

            NotificationDTO email = NotificationDTO.builder()
                .recipient(user.getEmail())
                .subject(subject)
                .templateName(template)
                .templateVariables(vars)
                .build();
            
            notificationService.sendEmail(email, user);

        } else if(transaction.getTransactionType() == TransactionType.WITHDRAWAL) {
            subject = "Debit Alert";
            template = "debit-alert";

            NotificationDTO email = NotificationDTO.builder()
                .recipient(user.getEmail())
                .subject(subject)
                .templateName(template)
                .templateVariables(vars)
                .build();

            notificationService.sendEmail(email, user);

        } else if(transaction.getTransactionType() == TransactionType.TRANSFER) {
            subject = "Debit Alert";
            template = "debit-alert";

            NotificationDTO email = NotificationDTO.builder()
                .recipient(user.getEmail())
                .subject(subject)
                .templateName(template)
                .templateVariables(vars)
                .build();

            notificationService.sendEmail(email, user);

            Account destinationAccount = accountRepo.findByAccountNumber(transaction.getDestinationAccount())
                .orElseThrow(() -> new NotFoundException("Destination account not found"));

            User receiver = destinationAccount.getUser();

            Map<String, Object> receiverVars = new HashMap<>();
            receiverVars.put("name", receiver.getFirstName());
            receiverVars.put("amount", transaction.getAmount());
            receiverVars.put("accountNumber", transaction.getAccount().getAccountNumber());
            receiverVars.put("date", transaction.getTransactionDate());
            receiverVars.put("balance", transaction.getAccount().getBalance());
        
            NotificationDTO receiverEmail = NotificationDTO.builder()
                .recipient(receiver.getEmail())
                .subject("Credit Alert")
                .templateName("credit-alert")
                .templateVariables(receiverVars)
                .build();

            notificationService.sendEmail(receiverEmail, user);
        }

    }
}