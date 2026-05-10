package it.michelepal.rubrica.service;

import it.michelepal.rubrica.dto.AddressRequest;
import it.michelepal.rubrica.dto.ContactChannelRequest;
import it.michelepal.rubrica.dto.ContactRequest;
import it.michelepal.rubrica.dto.ContactResponse;
import it.michelepal.rubrica.entity.AppUser;
import it.michelepal.rubrica.entity.Contact;
import it.michelepal.rubrica.entity.ContactAddress;
import it.michelepal.rubrica.entity.ContactEmail;
import it.michelepal.rubrica.entity.ContactPhone;
import it.michelepal.rubrica.entity.Tag;
import it.michelepal.rubrica.exception.NotFoundException;
import it.michelepal.rubrica.mapper.ContactMapper;
import it.michelepal.rubrica.repository.AppUserRepository;
import it.michelepal.rubrica.repository.ContactRepository;
import it.michelepal.rubrica.repository.TagRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@SuppressWarnings("null")
public class ContactService {
    private final ContactRepository contactRepository;
    private final AppUserRepository userRepository;
    private final TagRepository tagRepository;
    private final ContactMapper mapper;
    private final InputNormalizer normalizer;

    public ContactService(
        ContactRepository contactRepository,
        AppUserRepository userRepository,
        TagRepository tagRepository,
        ContactMapper mapper,
        InputNormalizer normalizer
    ) {
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
        this.mapper = mapper;
        this.normalizer = normalizer;
    }

    @Transactional(readOnly = true)
    public List<ContactResponse> list(String username) {
        return contactRepository.findByUserUsernameOrderByLastNameAscFirstNameAsc(username).stream()
            .map(mapper::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public ContactResponse get(String username, Long id) {
        return mapper.toResponse(findOwned(username, id));
    }

    @Transactional
    public ContactResponse create(String username, ContactRequest request) {
        AppUser user = userRepository.findByUsername(username).orElseThrow(() -> new NotFoundException("Utente non trovato."));
        Contact contact = new Contact();
        contact.setUser(user);
        apply(contact, username, request);
        return mapper.toResponse(contactRepository.save(Objects.requireNonNull(contact)));
    }

    @Transactional
    public ContactResponse update(String username, Long id, ContactRequest request) {
        Contact contact = findOwned(username, id);
        apply(contact, username, request);
        return mapper.toResponse(contactRepository.save(Objects.requireNonNull(contact)));
    }

    @Transactional
    public void delete(String username, Long id) {
        contactRepository.delete(findOwned(username, id));
    }

    private Contact findOwned(String username, Long id) {
        return contactRepository.findByIdAndUserUsername(id, username)
            .orElseThrow(() -> new NotFoundException("Contatto non trovato."));
    }

    private void apply(Contact contact, String username, ContactRequest request) {
        contact.setFirstName(normalizer.requiredText(request.firstName()));
        contact.setLastName(normalizer.text(request.lastName()));
        contact.setCompany(normalizer.text(request.company()));
        contact.setJobTitle(normalizer.text(request.jobTitle()));
        contact.setNotes(normalizer.text(request.notes()));
        contact.setFavorite(request.favorite());
        replacePhones(contact, request.phones());
        replaceEmails(contact, request.emails());
        replaceAddresses(contact, request.addresses());
        replaceTags(contact, username, request.tagIds());
    }

    private void replacePhones(Contact contact, List<ContactChannelRequest> requests) {
        contact.getPhones().clear();
        if (requests == null) {
            return;
        }
        for (ContactChannelRequest request : requests) {
            ContactPhone phone = new ContactPhone();
            phone.setContact(contact);
            phone.setType(normalizer.requiredText(request.type()));
            phone.setPhoneNumber(normalizer.requiredText(request.value()));
            phone.setPrimaryPhone(request.primary());
            contact.getPhones().add(phone);
        }
    }

    private void replaceEmails(Contact contact, List<ContactChannelRequest> requests) {
        contact.getEmails().clear();
        if (requests == null) {
            return;
        }
        for (ContactChannelRequest request : requests) {
            ContactEmail email = new ContactEmail();
            email.setContact(contact);
            email.setType(normalizer.requiredText(request.type()));
            email.setEmail(normalizer.email(request.value()));
            email.setPrimaryEmail(request.primary());
            contact.getEmails().add(email);
        }
    }

    private void replaceAddresses(Contact contact, List<AddressRequest> requests) {
        contact.getAddresses().clear();
        if (requests == null) {
            return;
        }
        for (AddressRequest request : requests) {
            ContactAddress address = new ContactAddress();
            address.setContact(contact);
            address.setType(normalizer.requiredText(request.type()));
            address.setStreet(normalizer.text(request.street()));
            address.setCity(normalizer.text(request.city()));
            address.setProvince(normalizer.text(request.province()));
            address.setPostalCode(normalizer.text(request.postalCode()));
            address.setCountry(normalizer.text(request.country()));
            address.setPrimaryAddress(request.primary());
            contact.getAddresses().add(address);
        }
    }

    private void replaceTags(Contact contact, String username, Set<Long> tagIds) {
        contact.getTags().clear();
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        Set<Tag> tags = new HashSet<>();
        for (Long tagId : tagIds) {
            tags.add(tagRepository.findByIdAndUserUsername(tagId, username)
                .orElseThrow(() -> new NotFoundException("Tag non trovato.")));
        }
        contact.getTags().addAll(tags);
    }
}
