package it.michelepal.rubrica.dto;

import java.util.List;
import java.util.Set;

public record ContactResponse(
    Long id,
    String firstName,
    String lastName,
    String company,
    String jobTitle,
    String notes,
    boolean favorite,
    List<ContactChannelResponse> phones,
    List<ContactChannelResponse> emails,
    List<AddressResponse> addresses,
    Set<TagResponse> tags
) {
}

