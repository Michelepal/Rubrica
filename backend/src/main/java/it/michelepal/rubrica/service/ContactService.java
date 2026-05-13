package it.michelepal.rubrica.service;

import it.michelepal.rubrica.dto.AddressRequest;
import it.michelepal.rubrica.dto.ContactChannelRequest;
import it.michelepal.rubrica.dto.ContactRequest;
import it.michelepal.rubrica.dto.ContactResponse;
import it.michelepal.rubrica.dto.PageResponse;
import it.michelepal.rubrica.entity.AppUser;
import it.michelepal.rubrica.entity.Contact;
import it.michelepal.rubrica.entity.ContactAddress;
import it.michelepal.rubrica.entity.ContactEmail;
import it.michelepal.rubrica.entity.ContactPhone;
import it.michelepal.rubrica.entity.Tag;
import it.michelepal.rubrica.exception.ConflictException;
import it.michelepal.rubrica.exception.NotFoundException;
import it.michelepal.rubrica.mapper.ContactMapper;
import it.michelepal.rubrica.repository.AppUserRepository;
import it.michelepal.rubrica.repository.ContactRepository;
import it.michelepal.rubrica.repository.TagRepository;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
    public PageResponse<ContactResponse> list(String username, int page, int size, String query, Long tagId, Boolean favorite, String sort) {
        Page<Contact> contacts = contactRepository.findAll(
            contactSpecification(username, query, tagId, favorite),
            PageRequest.of(safePage(page), safeSize(size), contactSort(sort))
        );
        return new PageResponse<>(
            contacts.getContent().stream().map(mapper::toResponse).toList(),
            contacts.getNumber(),
            contacts.getSize(),
            contacts.getTotalElements(),
            contacts.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public List<ContactResponse> list(String username) {
        return contactRepository.findByUserUsernameOrderByLastNameAscFirstNameAsc(username).stream().map(mapper::toResponse).toList();
    }

    private Sort contactSort(String sort) {
        if ("recent".equalsIgnoreCase(sort)) {
            return Sort.by("createdAt").descending().and(Sort.by("id").descending());
        }
        return Sort.by("lastName").ascending().and(Sort.by("firstName").ascending());
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
        validateUniqueChannels(username, null, request);
        apply(contact, username, request);
        return mapper.toResponse(contactRepository.save(Objects.requireNonNull(contact)));
    }

    @Transactional
    public ContactResponse update(String username, Long id, ContactRequest request) {
        Contact contact = findOwned(username, id);
        validateUniqueChannels(username, id, request);
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

    private Specification<Contact> contactSpecification(String username, String query, Long tagId, Boolean favorite) {
        return (root, criteriaQuery, builder) -> {
            criteriaQuery.distinct(true);
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(builder.equal(root.get("user").get("username"), username));
            if (favorite != null) {
                predicates.add(builder.equal(root.get("favorite"), favorite));
            }
            if (tagId != null) {
                predicates.add(builder.equal(root.join("tags", JoinType.INNER).get("id"), tagId));
            }
            String term = normalizer.text(query);
            if (term != null) {
                String like = "%" + term.toLowerCase(Locale.ROOT) + "%";
                var emails = root.join("emails", JoinType.LEFT);
                var phones = root.join("phones", JoinType.LEFT);
                predicates.add(builder.or(
                    builder.like(builder.lower(root.get("firstName")), like),
                    builder.like(builder.lower(root.get("lastName")), like),
                    builder.like(builder.lower(root.get("notes")), like),
                    builder.like(builder.lower(emails.get("email")), like),
                    builder.like(builder.lower(phones.get("phoneNumber")), like)
                ));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private int safePage(int page) {
        return Math.max(page, 0);
    }

    private int safeSize(int size) {
        return Math.min(Math.max(size, 1), 10);
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

    private void validateUniqueChannels(String username, Long currentContactId, ContactRequest request) {
        Set<String> requestEmails = new HashSet<>();
        if (request.emails() != null) {
            for (ContactChannelRequest email : request.emails()) {
                String normalizedEmail = normalizeEmail(email.value());
                if (normalizedEmail != null && !requestEmails.add(normalizedEmail)) {
                    throw new ConflictException("Esiste giÃ  un contatto con questa email.");
                }
            }
        }

        Set<String> requestPhones = new HashSet<>();
        if (request.phones() != null) {
            for (ContactChannelRequest phone : request.phones()) {
                String normalizedPhone = normalizePhone(phone.value());
                if (normalizedPhone != null && !requestPhones.add(normalizedPhone)) {
                    throw new ConflictException("Esiste giÃ  un contatto con questo numero di telefono.");
                }
            }
        }

        if (!requestEmails.isEmpty()) {
            contactRepository.findByUserUsernameOrderByLastNameAscFirstNameAsc(username).stream()
                .filter(contact -> currentContactId == null || !Objects.equals(contact.getId(), currentContactId))
                .forEach(contact -> {
                boolean emailExists = contact.getEmails().stream()
                    .map(ContactEmail::getEmail)
                    .map(this::normalizeEmail)
                    .filter(Objects::nonNull)
                    .anyMatch(requestEmails::contains);
                if (emailExists) {
                    throw new ConflictException("Esiste giÃ  un contatto con questa email.");
                }
                });
        }
        if (!requestPhones.isEmpty()) {
            contactRepository.findWithPhonesByUsername(username).stream()
                .filter(contact -> currentContactId == null || !Objects.equals(contact.getId(), currentContactId))
                .forEach(contact -> {
                boolean phoneExists = contact.getPhones().stream()
                    .map(ContactPhone::getPhoneNumber)
                    .map(this::normalizePhone)
                    .filter(Objects::nonNull)
                    .anyMatch(requestPhones::contains);
                if (phoneExists) {
                    throw new ConflictException("Esiste giÃ  un contatto con questo numero di telefono.");
                }
                });
        }
    }

    private String normalizePhone(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().replaceAll("[^0-9+]", "");
    }

    private String normalizeEmail(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
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
