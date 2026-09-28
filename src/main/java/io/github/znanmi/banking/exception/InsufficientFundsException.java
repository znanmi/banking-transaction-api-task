package io.github.znanmi.banking.exception;

public class InsufficientFundsException extends RuntimeException {
  public InsufficientFundsException(String accountNumber) {
    super("Insufficient funds in account " + accountNumber);
  }
}
