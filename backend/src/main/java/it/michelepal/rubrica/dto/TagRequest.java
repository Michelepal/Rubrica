package it.michelepal.rubrica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TagRequest(
    @NotBlank(message = "Nome tag obbligatorio.")
    @Size(max = 40, message = "Nome tag troppo lungo.")
    @Pattern(regexp = "^[\\p{L}0-9][\\p{L}0-9 _\\-]*$", message = "Nome tag non valido.")
    String name,

    @Size(max = 20, message = "Colore tag troppo lungo.")
    String color
) {
}

