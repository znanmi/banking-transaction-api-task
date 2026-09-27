package io.github.znanmi.banking.model;

import java.math.BigDecimal;

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
}
