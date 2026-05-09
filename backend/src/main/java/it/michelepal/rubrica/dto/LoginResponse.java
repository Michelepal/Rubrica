package it.michelepal.rubrica.dto;

public record LoginResponse(
    String token,
    String tokenType,
    String username
) {
}

