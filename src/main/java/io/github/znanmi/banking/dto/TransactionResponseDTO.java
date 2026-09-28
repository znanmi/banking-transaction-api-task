package io.github.znanmi.banking.dto;

import java.math.BigDecimal;
import java.time.Instant;

import io.github.znanmi.banking.model.TransactionType;

public record TransactionResponseDTO(String transferReference, TransactionType type, BigDecimal amount,
    BigDecimal balanceAfter, String counterpartyAccountNumber, Instant timestamp) {

}
