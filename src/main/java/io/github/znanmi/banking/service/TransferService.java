package io.github.znanmi.banking.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import io.github.znanmi.banking.dto.TransferRequestDTO;
import io.github.znanmi.banking.dto.TransferResponseDTO;
import io.github.znanmi.banking.exception.AccountNotFoundException;
import io.github.znanmi.banking.exception.SameAccountTransferException;
import io.github.znanmi.banking.model.Account;
import io.github.znanmi.banking.model.TransactionEntry;
import io.github.znanmi.banking.model.TransactionType;
import io.github.znanmi.banking.repository.AccountRepository;
import io.github.znanmi.banking.repository.TransactionRepository;

@Service
public class TransferService {
  private final AccountRepository accountRepository;
  private final TransactionRepository transactionRepository;

  public TransferService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
    this.accountRepository = accountRepository;
    this.transactionRepository = transactionRepository;
  }

  public TransferResponseDTO transfer(TransferRequestDTO request) {
    String fromNumber = request.fromAccountNumber();
    String toNumber = request.toAccountNumber();
    BigDecimal amount = request.amount();

    // validations: if to and from account are the same
    if (fromNumber.equals(toNumber)) {
      throw new SameAccountTransferException();
    }

    // find both accounts, check if they exist
    Optional<Account> fromResult = accountRepository.findByAccountNumber(fromNumber);
    if (fromResult.isEmpty()) {
      throw new AccountNotFoundException(fromNumber);
    }
    Account fromAccount = fromResult.get();

    Optional<Account> toResult = accountRepository.findByAccountNumber(toNumber);
    if (toResult.isEmpty()) {
      throw new AccountNotFoundException(toNumber);
    }
    Account toAccount = toResult.get();

    // TODO: lock both accounts with concurrency branch

    // make transaction, debit first and credit after
    fromAccount.debit(amount);
    toAccount.credit(amount);
    // add it to ledger, two entries, linked by one reference
    String transferReference = UUID.randomUUID().toString();
    Instant now = Instant.now();

    TransactionEntry debitEntry = new TransactionEntry(UUID.randomUUID().toString(), transferReference, fromNumber,
        TransactionType.DEBIT, amount, fromAccount.getBalance(), toNumber, now);
    TransactionEntry creditEntry = new TransactionEntry(UUID.randomUUID().toString(), transferReference, toNumber,
        TransactionType.CREDIT, amount, toAccount.getBalance(), fromNumber, now);

    transactionRepository.save(debitEntry);
    transactionRepository.save(creditEntry);
    // build receipt for response
    return new TransferResponseDTO(transferReference, fromNumber, toNumber, amount, now);
  }
}
