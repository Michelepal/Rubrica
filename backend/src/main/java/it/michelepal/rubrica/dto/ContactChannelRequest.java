package it.michelepal.rubrica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContactChannelRequest(
    @NotBlank(message = "Tipo obbligatorio.")
    @Size(max = 30, message = "Tipo troppo lungo.")
    String type,

    @NotBlank(message = "Valore obbligatorio.")
    @Size(max = 254, message = "Valore troppo lungo.")
    String value,

    boolean primary
) {
}

