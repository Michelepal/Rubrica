package it.michelepal.rubrica.config;

import it.michelepal.rubrica.entity.AppUser;
import it.michelepal.rubrica.entity.Contact;
import it.michelepal.rubrica.entity.ContactEmail;
import it.michelepal.rubrica.entity.ContactPhone;
import it.michelepal.rubrica.entity.Tag;
import it.michelepal.rubrica.repository.AppUserRepository;
import it.michelepal.rubrica.repository.ContactRepository;
import it.michelepal.rubrica.repository.TagRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("dev")
@SuppressWarnings("null")
public class DataInitializer {

    @Bean
    CommandLineRunner demoUser(
        AppUserRepository userRepository,
        ContactRepository contactRepository,
        TagRepository tagRepository,
        PasswordEncoder passwordEncoder
    ) {
        return args -> {
            AppUser user = userRepository.findByUsername("admin")
                .orElseGet(() -> {
                    AppUser newUser = new AppUser();
                    newUser.setUsername("admin");
                    newUser.setEmail("admin@example.local");
                    newUser.setPasswordHash(passwordEncoder.encode("admin"));
                    newUser.setFirstName("Admin");
                    newUser.setLastName("Demo");
                    return userRepository.save(newUser);
                });

            Tag lavoro = findOrCreateTag(user, "Lavoro", "#2563eb", tagRepository);
            Tag vip = findOrCreateTag(user, "VIP", "#b45309", tagRepository);
            Tag clienti = findOrCreateTag(user, "Clienti", "#047857", tagRepository);
            Tag fornitori = findOrCreateTag(user, "Fornitori", "#dc2626", tagRepository);
            Tag famiglia = findOrCreateTag(user, "Famiglia", "#7c3aed", tagRepository);
            Map<String, Tag> tags = Map.of(
                "Lavoro", lavoro,
                "VIP", vip,
                "Clienti", clienti,
                "Fornitori", fornitori,
                "Famiglia", famiglia
            );

            List<Contact> existingContacts = contactRepository.findByUserUsernameOrderByLastNameAscFirstNameAsc(user.getUsername());
            List<Contact> demoContacts = demoContacts(user, tags);
            Map<String, Contact> existingByEmail = existingContacts.stream()
                .flatMap(contact -> contact.getEmails().stream().map(email -> Map.entry(email.getEmail().toLowerCase(), contact)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (first, second) -> first));
            Map<String, Instant> demoCreatedAtByEmail = demoContacts.stream()
                .flatMap(contact -> contact.getEmails().stream().map(email -> Map.entry(email.getEmail().toLowerCase(), contact.getCreatedAt())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            Set<String> existingEmails = existingByEmail.keySet();
            List<Contact> contactsToUpdate = existingByEmail.entrySet().stream()
                .filter(entry -> demoCreatedAtByEmail.containsKey(entry.getKey()))
                .map(entry -> {
                    entry.getValue().setCreatedAt(demoCreatedAtByEmail.get(entry.getKey()));
                    return entry.getValue();
                })
                .distinct()
                .toList();
            if (!contactsToUpdate.isEmpty()) {
                contactRepository.saveAll(Objects.requireNonNull(contactsToUpdate));
            }
            List<Contact> contactsToCreate = demoContacts.stream()
                .filter(contact -> contact.getEmails().stream().noneMatch(email -> existingEmails.contains(email.getEmail().toLowerCase())))
                .toList();
            if (!contactsToCreate.isEmpty()) {
                contactRepository.saveAll(Objects.requireNonNull(contactsToCreate));
            }
        };
    }

    private List<Contact> demoContacts(AppUser user, Map<String, Tag> tags) {
        return List.of(
            createContact(user, 1, "Laura", "Bianchi", "Northwind", "laura.bianchi@example.local", "+39 333 123 4567", tags, "Lavoro", "VIP"),
            createContact(user, 2, "Marco", "Rossi", "Studio Rossi", "m.rossi@example.local", "02 555 0199", tags, "Clienti"),
            createContact(user, 3, "Giulia", "Verdi", "Contoso", "giulia.verdi@example.local", "+39 349 555 0103", tags, "Lavoro"),
            createContact(user, 4, "Andrea", "Neri", "Fabrikam", "andrea.neri@example.local", "+39 347 555 0104", tags, "Lavoro", "Fornitori"),
            createContact(user, 5, "Sara", "Ferrari", "Alpine Studio", "sara.ferrari@example.local", "+39 348 555 0105", tags, "Fornitori"),
            createContact(user, 6, "Paolo", "Romano", "Blue Moon", "paolo.romano@example.local", "+39 331 555 0106", tags, "VIP", "Clienti"),
            createContact(user, 7, "Elena", "Gallo", "Green Lab", "elena.gallo@example.local", "+39 332 555 0107", tags, "Famiglia"),
            createContact(user, 8, "Davide", "Costa", "Studio Costa", "davide.costa@example.local", "+39 333 555 0108", tags, "Clienti"),
            createContact(user, 9, "Marta", "Ricci", "Ricci & Co", "marta.ricci@example.local", "+39 334 555 0109", tags, "Lavoro", "Famiglia"),
            createContact(user, 10, "Luca", "Moretti", "Moretti Impianti", "luca.moretti@example.local", "+39 335 555 0110", tags, "Fornitori"),
            createContact(user, 11, "Chiara", "Marino", "Northwind", "chiara.marino@example.local", "+39 336 555 0111", tags, "Clienti"),
            createContact(user, 12, "Fabio", "Greco", "Greco Food", "fabio.greco@example.local", "+39 337 555 0112", tags, "VIP", "Fornitori"),
            createContact(user, 13, "Irene", "Rinaldi", "Rinaldi Group", "irene.rinaldi@example.local", "+39 338 555 0113", tags, "VIP"),
            createContact(user, 14, "Simone", "Lombardi", "Lombardi Casa", "simone.lombardi@example.local", "+39 339 555 0114", tags, "Famiglia"),
            createContact(user, 15, "Valeria", "Fontana", "Fontana Media", "valeria.fontana@example.local", "+39 340 555 0115", tags, "Lavoro", "Clienti")
        );
    }

    private Tag findOrCreateTag(AppUser user, String name, String color, TagRepository tagRepository) {
        Optional<Tag> existing = tagRepository.findByUserUsernameOrderByNameAsc(user.getUsername()).stream()
            .filter(tag -> tag.getName().equalsIgnoreCase(name))
            .findFirst();
        return existing.orElseGet(() -> tagRepository.save(Objects.requireNonNull(createTag(user, name, color))));
    }

    private Tag createTag(AppUser user, String name, String color) {
        Tag tag = new Tag();
        tag.setUser(user);
        tag.setName(name);
        tag.setColor(color);
        return tag;
    }

    private Contact createContact(AppUser user, String firstName, String lastName, String company, String emailValue, String phoneValue) {
        Contact contact = new Contact();
        contact.setUser(user);
        contact.setFirstName(firstName);
        contact.setLastName(lastName);
        contact.setCompany(company);
        contact.setNotes("Contatto demo modificabile.");

        ContactEmail email = new ContactEmail();
        email.setContact(contact);
        email.setType("email");
        email.setEmail(emailValue);
        email.setPrimaryEmail(true);
        contact.getEmails().add(email);

        ContactPhone phone = new ContactPhone();
        phone.setContact(contact);
        phone.setType("mobile");
        phone.setPhoneNumber(phoneValue);
        phone.setPrimaryPhone(true);
        contact.getPhones().add(phone);

        return contact;
    }

    private Contact createContact(
        AppUser user,
        int daysAgo,
        String firstName,
        String lastName,
        String company,
        String emailValue,
        String phoneValue,
        Map<String, Tag> tags,
        String... tagNames
    ) {
        Contact contact = createContact(user, firstName, lastName, company, emailValue, phoneValue);
        contact.setCreatedAt(Instant.now().minus(daysAgo, ChronoUnit.DAYS));
        for (String tagName : tagNames) {
            Optional.ofNullable(tags.get(tagName)).ifPresent(contact.getTags()::add);
        }
        return contact;
    }
}
