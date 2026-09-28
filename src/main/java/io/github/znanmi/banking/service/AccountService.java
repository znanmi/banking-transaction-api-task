package io.github.znanmi.banking.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import io.github.znanmi.banking.dto.AccountResponseDTO;
import io.github.znanmi.banking.dto.CreateAccountRequestDTO;
import io.github.znanmi.banking.model.Account;
import io.github.znanmi.banking.repository.AccountRepository;

@Service
public class AccountService {
  private final AccountRepository accountRepository;

  public AccountService(AccountRepository accountRepository) { // constructor injection
    this.accountRepository = accountRepository;
  }

  public AccountResponseDTO createAccount(CreateAccountRequestDTO request) {
    Account account = new Account(request.accountHolderName(), request.initialBalance(),
        accountRepository.nextAccountNumber());
    Account saved = accountRepository.save(account);
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
