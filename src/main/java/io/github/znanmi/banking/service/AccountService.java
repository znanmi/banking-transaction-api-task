package io.github.znanmi.banking.service;

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

  private AccountResponseDTO toResponse(Account account) {
    return new AccountResponseDTO(account.getAccountNumber(), account.getAccountHolderName(), account.getBalance());
  }
}
