package io.github.znanmi.banking.repository;

import java.util.List;

import io.github.znanmi.banking.model.TransactionEntry;

public interface TransactionRepository {
  TransactionEntry save(TransactionEntry entry);

  List<TransactionEntry> findByAccountNumber(String accountNumber);
}
