package it.michelepal.rubrica.dto;

public record AddressResponse(
    Long id,
    String type,
    String street,
    String city,
    String province,
    String postalCode,
    String country,
    boolean primary
) {
}

