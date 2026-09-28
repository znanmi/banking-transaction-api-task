package io.github.znanmi.banking.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TransferResponseDTO(String transferReference, String fromAccountNumber, String toAccountNumber,
    BigDecimal amount,
    Instant transferredAt) {

}
