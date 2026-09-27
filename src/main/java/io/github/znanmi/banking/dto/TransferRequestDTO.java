package io.github.znanmi.banking.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TransferRequestDTO(@NotBlank(message = "From Account Number is required") String fromAccountNumber,
    @NotBlank(message = "To Account Number is required") String toAccountNumber,
    @NotNull(message = "Amount is required") @Positive(message = "Amount must be greater than 0") @Digits(integer = 15, fraction = 2, message = "Amount can have at most 2 decimal places") BigDecimal amount) {
}
