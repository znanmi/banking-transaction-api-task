package io.github.znanmi.banking.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateAccountRequestDTO(@NotBlank(message = "Account Holder Name is required") String accountHolderName,
    @NotNull(message = "Initial Balance is required") @PositiveOrZero(message = "Initial Balance cannot be negative") @Digits(integer = 15, fraction = 2, message = "Initial Balance can have at most 2 decimal places") BigDecimal initialBalance) {
}
