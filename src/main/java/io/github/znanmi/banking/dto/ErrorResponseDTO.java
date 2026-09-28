package io.github.znanmi.banking.dto;

import java.time.Instant;
import java.util.Map;

public record ErrorResponseDTO(
    int status,
    String error,
    String message,
    Map<String, String> fieldErrors,
    Instant timestamp) {
}
