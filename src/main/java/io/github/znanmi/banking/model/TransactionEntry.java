package io.github.znanmi.banking.model;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionEntry(String entryId,
    String transferReference,
    String accountNumber,
    TransactionType type,
    BigDecimal amount,
    BigDecimal balanceAfter,
    String counterpartyAccountNumber,
    Instant timestamp) {

}
