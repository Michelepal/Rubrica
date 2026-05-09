package it.michelepal.rubrica.repository;

import static org.junit.jupiter.api.Assertions.*;

import it.michelepal.rubrica.entity.AppUser;
import it.michelepal.rubrica.entity.Contact;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("ContactRepository Tests")
public class ContactRepositoryTest {

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    private AppUser testUser;
    private Contact testContact;

    @BeforeEach
    void setUp() {
        // Create and save a test user
        testUser = new AppUser();
        testUser.setUsername("testuser");
        testUser.setEmail("testuser@example.com");
        testUser.setPasswordHash("hashedpassword");
        if (testUser != null) {
            appUserRepository.save(testUser);
        } else {
            fail("Test user was not initialized properly");
        }

        // Create a test contact
        testContact = new Contact();
        testContact.setUser(testUser);
        testContact.setFirstName("John");
        testContact.setLastName("Doe");
        testContact.setCompany("Acme Corp");
        testContact.setJobTitle("Developer");
        testContact.setFavorite(false);
    }

    @Test
    @DisplayName("findByUserUsernameOrderByLastNameAscFirstNameAsc: should return all contacts ordered correctly")
    void testFindByUserUsernameOrderByLastNameAscFirstNameAsc() {
        // Arrange
        Contact contact1 = new Contact();
        contact1.setUser(testUser);
        contact1.setFirstName("John");
        contact1.setLastName("Doe");
        contactRepository.save(contact1);

        Contact contact2 = new Contact();
        contact2.setUser(testUser);
        contact2.setFirstName("Jane");
        contact2.setLastName("Doe");
        contactRepository.save(contact2);

        Contact contact3 = new Contact();
        contact3.setUser(testUser);
        contact3.setFirstName("Bob");
        contact3.setLastName("Smith");
        contactRepository.save(contact3);

        // Act
        List<Contact> result = contactRepository.findByUserUsernameOrderByLastNameAscFirstNameAsc("testuser");

        // Assert
        assertEquals(3, result.size());
        assertEquals("Jane", result.get(0).getFirstName());
        assertEquals("Doe", result.get(0).getLastName());
        assertEquals("John", result.get(1).getFirstName());
        assertEquals("Doe", result.get(1).getLastName());
        assertEquals("Bob", result.get(2).getFirstName());
        assertEquals("Smith", result.get(2).getLastName());
    }

    @Test
    @DisplayName("findByUserUsernameOrderByLastNameAscFirstNameAsc: should return empty list for user with no contacts")
    void testFindByUserUsernameOrderByLastNameAscFirstNameAscEmpty() {
        // Act
        List<Contact> result = contactRepository.findByUserUsernameOrderByLastNameAscFirstNameAsc("testuser");

        // Assert
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("findByUserUsernameOrderByLastNameAscFirstNameAsc: should not return contacts from other users")
    void testFindByUserUsernameOrderByLastNameAscFirstNameAscMultipleUsers() {
        // Arrange
        AppUser anotherUser = new AppUser();
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPasswordHash("hashedpassword");
        appUserRepository.save(anotherUser);

        Contact contact1 = new Contact();
        contact1.setUser(testUser);
        contact1.setFirstName("John");
        contact1.setLastName("Doe");
        contactRepository.save(contact1);

        Contact contact2 = new Contact();
        contact2.setUser(anotherUser);
        contact2.setFirstName("Jane");
        contact2.setLastName("Smith");
        contactRepository.save(contact2);

        // Act
        List<Contact> result = contactRepository.findByUserUsernameOrderByLastNameAscFirstNameAsc("testuser");

        // Assert
        assertEquals(1, result.size());
        assertEquals(testUser.getId(), result.get(0).getUser().getId());
    }

    @Test
    @DisplayName("findByIdAndUserUsername: should find a contact by id and username")
    void testFindByIdAndUserUsername() {
        if (testContact != null) {
            // Arrange
            Contact saved = contactRepository.save(testContact);

            // Act
            Optional<Contact> result = contactRepository.findByIdAndUserUsername(saved.getId(), "testuser");

            // Assert
            assertTrue(result.isPresent());
            assertEquals("John", result.get().getFirstName());
            assertEquals("Doe", result.get().getLastName());
        } else {
            fail("Test contact was not initialized properly");
        }

    }

    @Test
    @DisplayName("findByIdAndUserUsername: should return empty when contact does not belong to user")
    void testFindByIdAndUserUsernameWrongUser() {
        if (testContact != null) {
            // Arrange
            Contact saved = contactRepository.save(testContact);
            // Act
            Optional<Contact> result = contactRepository.findByIdAndUserUsername(saved.getId(), "wronguser");

            // Assert
            assertTrue(result.isEmpty());
        } else {
            fail("Test contact was not initialized properly");
        }
    }

    @Test
    @DisplayName("findByIdAndUserUsername: should return empty when contact id not found")
    void testFindByIdAndUserUsernameNotFound() {
        // Act
        Optional<Contact> result = contactRepository.findByIdAndUserUsername(999L, "testuser");

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("save: should persist a contact")
    void testSaveContact() {
        if (testContact != null) {
            // Act
            Contact saved = contactRepository.save(testContact);
            // Assert
            assertNotNull(saved.getId());
            Optional<Contact> retrieved = contactRepository.findById(saved.getId() != null? saved.getId() : -1L);
            assertTrue(retrieved.isPresent());
            assertEquals("John", retrieved.get().getFirstName());
        } else {
            fail("Test contact was not initialized properly");
        }
    }

    @Test
    @DisplayName("update: should modify an existing contact")
    void testUpdateContact() {
        if (testContact != null) {
            // Arrange
            Contact saved = contactRepository.save(testContact);
            Long contactId = saved.getId();

            // Act
            Contact toUpdate = contactRepository.findById(contactId != null? contactId : -1L).get();
            toUpdate.setFirstName("Jane");
            toUpdate.setLastName("Smith");
            contactRepository.save(toUpdate);

            // Assert
            Contact updated = contactRepository.findById(contactId != null? contactId : -1L).get();
            assertEquals("Jane", updated.getFirstName());
            assertEquals("Smith", updated.getLastName());
        } else {
            fail("Test contact was not initialized properly");
        }
    }

    @Test
    @DisplayName("delete: should remove a contact")
    void testDeleteContact() {
        if (testContact != null) {
            // Arrange
            Contact saved = contactRepository.save(testContact);
            Long contactId = saved.getId();

            // Act
            contactRepository.delete(saved);

            // Assert
            Optional<Contact> result = contactRepository.findById(contactId != null? contactId : -1L);
            assertTrue(result.isEmpty());
        } else {
            fail("Test contact was not initialized properly");
        }
    }

    @Test
    @DisplayName("should maintain relationship with AppUser")
    void testContactUserRelationship() {
        if (testContact != null) {
            // Arrange
            Contact saved = contactRepository.save(testContact);
            // Act
            Contact retrieved = contactRepository.findById(saved.getId() != null? saved.getId() : -1L).get();

            // Assert
            assertNotNull(retrieved.getUser());
            assertEquals(testUser.getId(), retrieved.getUser().getId());
            assertEquals("testuser", retrieved.getUser().getUsername());
        } else {
            fail("Test contact was not initialized properly");
        }
    }
}
