package io.github.znanmi.banking.dto;

import java.math.BigDecimal;

public record AccountResponseDTO(String accountNumber, String accountHolderName, BigDecimal balance) {
}
