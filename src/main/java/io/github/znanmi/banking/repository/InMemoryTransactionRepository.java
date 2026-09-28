package io.github.znanmi.banking.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import io.github.znanmi.banking.model.TransactionEntry;

@Repository
// Not yet safe for concurrent saves to a new account; addressed in the
// concurrency branch
public class InMemoryTransactionRepository implements TransactionRepository {
  private final Map<String, List<TransactionEntry>> entriesByAccount = new ConcurrentHashMap<>();

  @Override
  public TransactionEntry save(TransactionEntry entry) {
    List<TransactionEntry> list = entriesByAccount.get(entry.accountNumber());
    if (list == null) {
      list = new ArrayList<>();
      entriesByAccount.put(entry.accountNumber(), list);
    }
    list.add(entry);
    return entry;
  }

  @Override
  public List<TransactionEntry> findByAccountNumber(String accountNumber) {
    List<TransactionEntry> list = entriesByAccount.get(accountNumber);
    if (list == null) {
      return new ArrayList<>();
    }
    return new ArrayList<>(list);
  }
}
