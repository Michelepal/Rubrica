package it.michelepal.rubrica.config;

import it.michelepal.rubrica.entity.AppUser;
import it.michelepal.rubrica.entity.Contact;
import it.michelepal.rubrica.entity.ContactEmail;
import it.michelepal.rubrica.entity.ContactPhone;
import it.michelepal.rubrica.entity.Tag;
import it.michelepal.rubrica.repository.AppUserRepository;
import it.michelepal.rubrica.repository.ContactRepository;
import it.michelepal.rubrica.repository.TagRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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

            if (contactRepository.findByUserUsernameOrderByLastNameAscFirstNameAsc(user.getUsername()).isEmpty()) {
                Tag lavoro = findOrCreateTag(user, "Lavoro", "#2563eb", tagRepository);
                Tag vip = findOrCreateTag(user, "VIP", "#b45309", tagRepository);
                Tag clienti = findOrCreateTag(user, "Clienti", "#047857", tagRepository);

                Contact laura = createContact(user, "Laura", "Bianchi", "Northwind", "laura.bianchi@example.local", "+39 333 123 4567");
                laura.getTags().add(lavoro);
                laura.getTags().add(vip);

                Contact marco = createContact(user, "Marco", "Rossi", "Studio Rossi", "m.rossi@example.local", "02 555 0199");
                marco.getTags().add(clienti);

                contactRepository.saveAll(Objects.requireNonNull(List.of(laura, marco)));
            }
        };
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
}
