package it.michelepal.rubrica.mapper;

import it.michelepal.rubrica.dto.AddressResponse;
import it.michelepal.rubrica.dto.ContactChannelResponse;
import it.michelepal.rubrica.dto.ContactResponse;
import it.michelepal.rubrica.dto.TagResponse;
import it.michelepal.rubrica.entity.Contact;
import java.util.Comparator;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class ContactMapper {
    public ContactResponse toResponse(Contact contact) {
        return new ContactResponse(
            contact.getId(),
            contact.getFirstName(),
            contact.getLastName(),
            contact.getCompany(),
            contact.getJobTitle(),
            contact.getNotes(),
            contact.getCreatedAt(),
            contact.isFavorite(),
            contact.getPhones().stream()
                .map(phone -> new ContactChannelResponse(phone.getId(), phone.getType(), phone.getPhoneNumber(), phone.isPrimaryPhone()))
                .toList(),
            contact.getEmails().stream()
                .map(email -> new ContactChannelResponse(email.getId(), email.getType(), email.getEmail(), email.isPrimaryEmail()))
                .toList(),
            contact.getAddresses().stream()
                .map(address -> new AddressResponse(address.getId(), address.getType(), address.getStreet(), address.getCity(), address.getProvince(), address.getPostalCode(), address.getCountry(), address.isPrimaryAddress()))
                .toList(),
            contact.getTags().stream()
                .map(tag -> new TagResponse(tag.getId(), tag.getName(), tag.getColor()))
                .sorted(Comparator.comparing(TagResponse::name))
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new))
        );
    }
}
