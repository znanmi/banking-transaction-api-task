package io.github.znanmi.banking.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import io.github.znanmi.banking.dto.AccountResponseDTO;
import io.github.znanmi.banking.dto.CreateAccountRequestDTO;
import io.github.znanmi.banking.model.Account;
import io.github.znanmi.banking.model.TransactionEntry;
import io.github.znanmi.banking.model.TransactionType;
import io.github.znanmi.banking.repository.AccountRepository;
import io.github.znanmi.banking.repository.TransactionRepository;

@Service
public class AccountService {
  private final AccountRepository accountRepository;
  private final TransactionRepository transactionRepository;

  public AccountService(AccountRepository accountRepository, TransactionRepository transactionRepository) { // constructor
                                                                                                            // injection
    this.accountRepository = accountRepository;
    this.transactionRepository = transactionRepository;
  }

  public AccountResponseDTO createAccount(CreateAccountRequestDTO request) {
    Account account = new Account(request.accountHolderName(), request.initialBalance(),
        accountRepository.nextAccountNumber());
    Account saved = accountRepository.save(account);
    TransactionEntry transactionEntry = new TransactionEntry(UUID.randomUUID().toString(), null,
        account.getAccountNumber(), TransactionType.CREDIT, request.initialBalance(), request.initialBalance(), null,
        Instant.now());
    transactionRepository.save(transactionEntry);
    return toResponse(saved);
  }

  public AccountResponseDTO getAccount(String accountNumber) {
    Optional<Account> result = accountRepository.findByAccountNumber(accountNumber);
    if (result.isEmpty()) {
      throw new IllegalArgumentException("Account not found: " + accountNumber);
    }
    return toResponse(result.get());
  }

  private AccountResponseDTO toResponse(Account account) {
    return new AccountResponseDTO(account.getAccountNumber(), account.getAccountHolderName(), account.getBalance());
  }
}
