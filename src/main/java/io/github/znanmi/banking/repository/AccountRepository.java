package io.github.znanmi.banking.repository;

import io.github.znanmi.banking.model.Account;
import java.util.Optional;

public interface AccountRepository {
  Account save(Account account);

  String nextAccountNumber();// for generating next account numbers

  Optional<Account> findByAccountNumber(String accountNumber);
}
