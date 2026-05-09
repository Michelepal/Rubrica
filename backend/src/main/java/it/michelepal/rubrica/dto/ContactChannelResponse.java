package it.michelepal.rubrica.dto;

public record ContactChannelResponse(
    Long id,
    String type,
    String value,
    boolean primary
) {
}

