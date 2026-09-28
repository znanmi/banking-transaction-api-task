package io.github.znanmi.banking.service;

import io.github.znanmi.banking.exception.AccountNotFoundException;
import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.znanmi.banking.dto.AccountResponseDTO;
import io.github.znanmi.banking.dto.CreateAccountRequestDTO;
import io.github.znanmi.banking.dto.TransactionResponseDTO;
import io.github.znanmi.banking.model.TransactionType;
import io.github.znanmi.banking.repository.InMemoryAccountRepository;
import io.github.znanmi.banking.repository.InMemoryTransactionRepository;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AccountServiceTest {
  private AccountService accountService;

  @BeforeEach
  void setUp() {
    accountService = new AccountService(
        new InMemoryAccountRepository(),
        new InMemoryTransactionRepository());
  }

  @Test
  @DisplayName("should create account with balance and opening deposit in history")
  void createAccount_recordsOpeningDeposit() {
    AccountResponseDTO account = accountService.createAccount(
        new CreateAccountRequestDTO("Alice", new BigDecimal("100.00")));

    assertThat(account.accountNumber()).isNotBlank();
    assertThat(account.accountHolderName()).isEqualTo("Alice");
    assertThat(account.balance()).isEqualByComparingTo("100.00");

    List<TransactionResponseDTO> history = accountService.getTransactionHistory(account.accountNumber());

    assertThat(history).hasSize(1);
    assertThat(history.get(0).type()).isEqualTo(TransactionType.CREDIT);
    assertThat(history.get(0).amount()).isEqualByComparingTo("100.00");
  }

  @Test
  @DisplayName("should throw AccountNotFound when getting history of unknown account")
  void getTransactionHistory_unknownAccount_throws() {
    assertThatThrownBy(() -> accountService.getTransactionHistory("ACCNUM9999"))
        .isInstanceOf(AccountNotFoundException.class)
        .hasMessageContaining("ACCNUM9999");
  }
}
