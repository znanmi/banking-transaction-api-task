package io.github.znanmi.banking.repository;

import io.github.znanmi.banking.model.Account;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Optional;

import org.springframework.stereotype.Repository;

@Repository
public class InMemoryAccountRepository implements AccountRepository {
  private final Map<String, Account> accounts = new ConcurrentHashMap<>();
  private final AtomicLong accountNumberSequence = new AtomicLong();

  @Override
  public Account save(Account account) {
    accounts.put(account.getAccountNumber(), account);
    return account;
  }

  @Override
  public String nextAccountNumber() {
    return String.format("ACCNUM%04d", accountNumberSequence.incrementAndGet()); // ACCNUM0001, ACCNUM0002, ...
  }

  @Override
  public Optional<Account> findByAccountNumber(String accountNumber) {
    return Optional.ofNullable(accounts.get(accountNumber));
  }
}
