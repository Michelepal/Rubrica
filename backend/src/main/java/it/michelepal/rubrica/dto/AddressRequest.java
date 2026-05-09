package it.michelepal.rubrica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddressRequest(
    @NotBlank(message = "Tipo indirizzo obbligatorio.")
    @Size(max = 30, message = "Tipo indirizzo troppo lungo.")
    String type,

    @Size(max = 160, message = "Via troppo lunga.")
    String street,

    @Size(max = 100, message = "Citta' troppo lunga.")
    String city,

    @Size(max = 80, message = "Provincia troppo lunga.")
    String province,

    @Size(max = 20, message = "CAP troppo lungo.")
    String postalCode,

    @Size(max = 80, message = "Paese troppo lungo.")
    String country,

    boolean primary
) {
}

