package io.github.znanmi.banking.controller;

import org.springframework.web.bind.annotation.RestController;

import io.github.znanmi.banking.dto.AccountResponseDTO;
import io.github.znanmi.banking.dto.CreateAccountRequestDTO;
import io.github.znanmi.banking.service.AccountService;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/accounts")
public class AccountController {
  private final AccountService accountService;

  public AccountController(AccountService accountService) {
    this.accountService = accountService;
  }

  @PostMapping
  public ResponseEntity<AccountResponseDTO> createAccount(@Valid @RequestBody CreateAccountRequestDTO request) {
    AccountResponseDTO response = accountService.createAccount(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}
