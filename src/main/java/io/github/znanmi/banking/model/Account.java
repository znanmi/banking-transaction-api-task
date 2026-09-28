package io.github.znanmi.banking.model;

import java.math.BigDecimal;

import io.github.znanmi.banking.exception.InsufficientFundsException;

public class Account {
  private final String accountHolderName;
  private final String accountNumber;
  private BigDecimal balance; // no setter, balance updates only through debit or credit

  public Account(String accountHolderName, BigDecimal balance, String accountNumber) {
    this.accountNumber = accountNumber;
    this.accountHolderName = accountHolderName;
    this.balance = balance;
  }

  public String getAccountHolderName() {
    return accountHolderName;
  }

  public BigDecimal getBalance() {
    return balance;
  }

  public String getAccountNumber() {
    return accountNumber;
  }

  public void debit(BigDecimal amount) {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) { // compareTo, returns a negative number, zero, or a
                                                                    // positive number
      throw new IllegalArgumentException("Debit amount must be greater than 0");// 500
    }
    if (amount.compareTo(balance) > 0) {
      throw new InsufficientFundsException(accountNumber);
    }
    balance = balance.subtract(amount);
  }

  public void credit(BigDecimal amount) {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Credit amount must be greater than 0");// 500
    }
    balance = balance.add(amount);
  }
}
