package it.michelepal.rubrica.dto;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public record ContactResponse(
    Long id,
    String firstName,
    String lastName,
    String company,
    String jobTitle,
    String notes,
    Instant createdAt,
    boolean favorite,
    List<ContactChannelResponse> phones,
    List<ContactChannelResponse> emails,
    List<AddressResponse> addresses,
    Set<TagResponse> tags
) {
}
