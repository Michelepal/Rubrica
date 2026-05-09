package it.michelepal.rubrica.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Set;

public record ContactRequest(
    @NotBlank(message = "Il nome e' obbligatorio.")
    @Size(min = 2, max = 80, message = "Il nome deve avere tra 2 e 80 caratteri.")
    @Pattern(regexp = "^[\\p{L}][\\p{L} '\\-]*$", message = "Il nome contiene caratteri non validi.")
    String firstName,

    @Size(max = 80, message = "Il cognome deve avere al massimo 80 caratteri.")
    String lastName,

    @Size(max = 120, message = "L'azienda deve avere al massimo 120 caratteri.")
    String company,

    @Size(max = 120, message = "Il ruolo deve avere al massimo 120 caratteri.")
    String jobTitle,

    @Size(max = 1000, message = "Le note devono avere al massimo 1000 caratteri.")
    String notes,

    boolean favorite,

    List<@Valid ContactChannelRequest> phones,
    List<@Valid ContactChannelRequest> emails,
    List<@Valid AddressRequest> addresses,
    Set<Long> tagIds
) {
}

