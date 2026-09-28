package io.github.znanmi.banking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.znanmi.banking.dto.CreateAccountRequestDTO;
import io.github.znanmi.banking.dto.TransactionResponseDTO;
import io.github.znanmi.banking.dto.TransferRequestDTO;
import io.github.znanmi.banking.dto.TransferResponseDTO;
import io.github.znanmi.banking.exception.InsufficientFundsException;
import io.github.znanmi.banking.exception.SameAccountTransferException;
import io.github.znanmi.banking.model.TransactionType;
import io.github.znanmi.banking.repository.AccountRepository;
import io.github.znanmi.banking.repository.InMemoryAccountRepository;
import io.github.znanmi.banking.repository.InMemoryTransactionRepository;
import io.github.znanmi.banking.repository.TransactionRepository;
import io.github.znanmi.banking.exception.AccountNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TransferServiceTest {

  private AccountService accountService;
  private TransferService transferService;
  private String alice;
  private String bob;

  @BeforeEach
  void setUp() {
    AccountRepository accountRepository = new InMemoryAccountRepository();
    TransactionRepository transactionRepository = new InMemoryTransactionRepository();

    accountService = new AccountService(accountRepository, transactionRepository);
    transferService = new TransferService(accountRepository, transactionRepository);

    alice = accountService.createAccount(
        new CreateAccountRequestDTO("Alice", new BigDecimal("100.00"))).accountNumber();
    bob = accountService.createAccount(
        new CreateAccountRequestDTO("Bob", new BigDecimal("0.00"))).accountNumber();
  }

  @Test
  @DisplayName("should move money between accounts when balance is sufficient")
  void transfer_movesMoney() {
    transferService.transfer(new TransferRequestDTO(alice, bob, new BigDecimal("30.00")));

    assertThat(balanceOf(alice)).isEqualByComparingTo("70.00");
    assertThat(balanceOf(bob)).isEqualByComparingTo("30.00");
  }

  @Test
  @DisplayName("should record a debit and a credit entry linked by one reference")
  void transfer_recordsLinkedLedgerEntries() {
    TransferResponseDTO response = transferService.transfer(
        new TransferRequestDTO(alice, bob, new BigDecimal("30.00")));

    List<TransactionResponseDTO> aliceHistory = accountService.getTransactionHistory(alice);
    List<TransactionResponseDTO> bobHistory = accountService.getTransactionHistory(bob);

    TransactionResponseDTO debit = aliceHistory.get(aliceHistory.size() - 1);
    TransactionResponseDTO credit = bobHistory.get(bobHistory.size() - 1);

    assertThat(debit.type()).isEqualTo(TransactionType.DEBIT);
    assertThat(debit.balanceAfter()).isEqualByComparingTo("70.00");
    assertThat(debit.counterpartyAccountNumber()).isEqualTo(bob);

    assertThat(credit.type()).isEqualTo(TransactionType.CREDIT);
    assertThat(credit.balanceAfter()).isEqualByComparingTo("30.00");
    assertThat(credit.counterpartyAccountNumber()).isEqualTo(alice);

    assertThat(debit.transferReference())
        .isEqualTo(credit.transferReference())
        .isEqualTo(response.transferReference());
  }

  @Test
  @DisplayName("should allow transfer of the exact balance leaving zero")
  void transfer_exactBalance_leavesZero() {
    transferService.transfer(new TransferRequestDTO(alice, bob, new BigDecimal("100.00")));

    assertThat(balanceOf(alice)).isEqualByComparingTo("0.00");
    assertThat(balanceOf(bob)).isEqualByComparingTo("100.00");
  }

  @Test
  @DisplayName("should leave both balances unchanged when funds are insufficient")
  void transfer_insufficientFunds_changesNothing() {
    assertThatThrownBy(() -> transferService.transfer(
        new TransferRequestDTO(alice, bob, new BigDecimal("100.01"))))
        .isInstanceOf(InsufficientFundsException.class);

    assertThat(balanceOf(alice)).isEqualByComparingTo("100.00");
    assertThat(balanceOf(bob)).isEqualByComparingTo("0.00");
    assertThat(accountService.getTransactionHistory(alice)).hasSize(1);
  }

  @Test
  @DisplayName("should reject transfer to the same account")
  void transfer_sameAccount_throws() {
    assertThatThrownBy(() -> transferService.transfer(
        new TransferRequestDTO(alice, alice, new BigDecimal("10.00"))))
        .isInstanceOf(SameAccountTransferException.class);

    assertThat(balanceOf(alice)).isEqualByComparingTo("100.00");
  }

  private BigDecimal balanceOf(String accountNumber) {
    return accountService.getAccount(accountNumber).balance();
  }

  @Test
  @DisplayName("should throw AccountNotFound when destination account does not exist")
  void transfer_unknownAccount_throws() {
    assertThatThrownBy(() -> transferService.transfer(
        new TransferRequestDTO(alice, "ACCNUM9999", new BigDecimal("10.00"))))
        .isInstanceOf(AccountNotFoundException.class);

    assertThat(balanceOf(alice)).isEqualByComparingTo("100.00");
  }
}