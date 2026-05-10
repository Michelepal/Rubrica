package it.michelepal.rubrica.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import it.michelepal.rubrica.dto.ContactRequest;
import it.michelepal.rubrica.dto.ContactResponse;
import it.michelepal.rubrica.entity.AppUser;
import it.michelepal.rubrica.entity.Contact;
import it.michelepal.rubrica.exception.NotFoundException;
import it.michelepal.rubrica.mapper.ContactMapper;
import it.michelepal.rubrica.repository.AppUserRepository;
import it.michelepal.rubrica.repository.ContactRepository;
import it.michelepal.rubrica.repository.TagRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContactService Tests")
public
class ContactServiceTest {

    private ContactService contactService;

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private AppUserRepository userRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private ContactMapper mapper;

    @Mock
    private InputNormalizer normalizer;

    private String testUsername = "testuser";
    private Long testContactId = 1L;

    @BeforeEach
    void setUp() {
        contactService = new ContactService(
            contactRepository,
            userRepository,
            tagRepository,
            mapper,
            normalizer
        );
    }

    @Test
    @DisplayName("list: should return all contacts for a user ordered by name")
    void testListContacts() {
        // Arrange
        Contact contact1 = new Contact();
        contact1.setFirstName("John");
        contact1.setLastName("Doe");
        contact1.setCompany("Acme Corp");

        Contact contact2 = new Contact();
        contact2.setFirstName("Jane");
        contact2.setLastName("Smith");
        contact2.setCompany("Tech Inc");

        ContactResponse response1 = new ContactResponse(1L, "John", "Doe", "Acme Corp", null, null, false, null, null, null, null);
        ContactResponse response2 = new ContactResponse(2L, "Jane", "Smith", "Tech Inc", null, null, false, null, null, null, null);

        when(contactRepository.findByUserUsernameOrderByLastNameAscFirstNameAsc(testUsername))
            .thenReturn(Arrays.asList(contact1, contact2));
        when(mapper.toResponse(contact1)).thenReturn(response1);
        when(mapper.toResponse(contact2)).thenReturn(response2);

        // Act
        List<ContactResponse> result = contactService.list(testUsername);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(2, result.size(), "Should return 2 contacts");
        verify(contactRepository, times(1)).findByUserUsernameOrderByLastNameAscFirstNameAsc(testUsername);
    }

    @Test
    @DisplayName("list: should return empty list when user has no contacts")
    void testListContactsEmpty() {
        // Arrange
        when(contactRepository.findByUserUsernameOrderByLastNameAscFirstNameAsc(testUsername))
            .thenReturn(Arrays.asList());

        // Act
        List<ContactResponse> result = contactService.list(testUsername);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertTrue(result.isEmpty(), "Result should be empty");
    }

    @Test
    @DisplayName("get: should retrieve a specific contact by id")
    void testGetContact() {
        // Arrange
        AppUser user = createTestUser();
        Contact contact = new Contact();
        contact.setFirstName("John");
        contact.setLastName("Doe");
        contact.setCompany("Acme Corp");
        contact.setUser(user);

        ContactResponse response = new ContactResponse(testContactId, "John", "Doe", "Acme Corp", null, null, false, null, null, null, null);

        when(contactRepository.findByIdAndUserUsername(testContactId, testUsername))
            .thenReturn(Optional.of(contact));
        when(mapper.toResponse(contact)).thenReturn(response);

        // Act
        ContactResponse result = contactService.get(testUsername, testContactId);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals("John", result.firstName(), "First name should be John");
        assertEquals("Doe", result.lastName(), "Last name should be Doe");
        verify(contactRepository, times(1)).findByIdAndUserUsername(testContactId, testUsername);
    }

    @Test
    @DisplayName("get: should throw NotFoundException when contact not found")
    void testGetContactNotFound() {
        // Arrange
        when(contactRepository.findByIdAndUserUsername(testContactId, testUsername))
            .thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException thrown = assertThrows(NotFoundException.class, () -> contactService.get(testUsername, testContactId), "Should throw NotFoundException");
        assertTrue(thrown.getMessage().contains("Contatto"), "Exception message should mention contact");
    }

    @Test
    @DisplayName("create: should create a new contact")
    void testCreateContact() {
        // Arrange
        ContactRequest request = new ContactRequest(
            "John",
            "Doe",
            "Acme Corp",
            "Developer",
            "Some notes",
            false,
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>(),
            new HashSet<>()
        );

        AppUser user = createTestUser();

        Contact savedContact = new Contact();
        savedContact.setFirstName("John");
        savedContact.setLastName("Doe");
        savedContact.setCompany("Acme Corp");
        savedContact.setJobTitle("Developer");
        savedContact.setNotes("Some notes");
        savedContact.setUser(user);

        ContactResponse response = new ContactResponse(testContactId, "John", "Doe", "Acme Corp", "Developer", "Some notes", false, null, null, null, null);

        when(normalizer.requiredText("John")).thenReturn("John");
        when(normalizer.text("Doe")).thenReturn("Doe");
        when(normalizer.text("Acme Corp")).thenReturn("Acme Corp");
        when(normalizer.text("Developer")).thenReturn("Developer");
        when(normalizer.text("Some notes")).thenReturn("Some notes");
        when(userRepository.findByUsername(testUsername)).thenReturn(Optional.of(user));
        when(contactRepository.save(any(Contact.class))).thenReturn(savedContact);
        when(mapper.toResponse(savedContact)).thenReturn(response);

        // Act
        ContactResponse result = contactService.create(testUsername, request);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals("John", result.firstName(), "First name should be John");
        assertEquals("Doe", result.lastName(), "Last name should be Doe");
        assertEquals("Acme Corp", result.company(), "Company should be set");
        verify(userRepository, times(1)).findByUsername(testUsername);
        verify(contactRepository, times(1)).save(any(Contact.class));
    }

    @Test
    @DisplayName("create: should throw NotFoundException when user not found")
    void testCreateContactUserNotFound() {
        // Arrange
        ContactRequest request = new ContactRequest(
            "John",
            "Doe",
            null,
            null,
            null,
            false,
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>(),
            new HashSet<>()
        );

        when(userRepository.findByUsername(testUsername)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException thrown = assertThrows(NotFoundException.class, () -> contactService.create(testUsername, request), "Should throw NotFoundException");
        assertTrue(thrown.getMessage().contains("Utente"), "Exception message should mention user");
        verify(contactRepository, times(0)).save(any(Contact.class));
    }

    @Test
    @DisplayName("update: should update an existing contact")
    void testUpdateContact() {
        // Arrange
        ContactRequest request = new ContactRequest(
            "John",
            "Smith",
            "New Corp",
            "Senior Developer",
            "Updated notes",
            true,
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>(),
            new HashSet<>()
        );

        AppUser user = createTestUser();
        Contact existingContact = new Contact();
        existingContact.setFirstName("John");
        existingContact.setLastName("Doe");
        existingContact.setCompany("Acme Corp");
        existingContact.setUser(user);

        ContactResponse response = new ContactResponse(testContactId, "John", "Smith", "New Corp", "Senior Developer", "Updated notes", true, null, null, null, null);

        when(contactRepository.findByIdAndUserUsername(testContactId, testUsername))
            .thenReturn(Optional.of(existingContact));
        when(normalizer.requiredText("John")).thenReturn("John");
        when(normalizer.text("Smith")).thenReturn("Smith");
        when(normalizer.text("New Corp")).thenReturn("New Corp");
        when(normalizer.text("Senior Developer")).thenReturn("Senior Developer");
        when(normalizer.text("Updated notes")).thenReturn("Updated notes");
        when(contactRepository.save(any(Contact.class))).thenReturn(existingContact);
        when(mapper.toResponse(existingContact)).thenReturn(response);

        // Act
        ContactResponse result = contactService.update(testUsername, testContactId, request);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals("Smith", result.lastName(), "Last name should be updated");
        verify(contactRepository, times(1)).findByIdAndUserUsername(testContactId, testUsername);
        verify(contactRepository, times(1)).save(any(Contact.class));
    }

    @Test
    @DisplayName("update: should throw NotFoundException when contact not found")
    void testUpdateContactNotFound() {
        // Arrange
        ContactRequest request = new ContactRequest(
            "John",
            "Doe",
            null,
            null,
            null,
            false,
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>(),
            new HashSet<>()
        );

        when(contactRepository.findByIdAndUserUsername(testContactId, testUsername))
            .thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException thrown = assertThrows(NotFoundException.class, () -> contactService.update(testUsername, testContactId, request), "Should throw NotFoundException");
        assertTrue(thrown.getMessage().contains("Contatto"), "Exception message should mention contact");
    }

    @Test
    @DisplayName("delete: should delete a contact")
    void testDeleteContact() {
        // Arrange
        AppUser user = createTestUser();
        Contact contactToDelete = new Contact();
        contactToDelete.setFirstName("John");
        contactToDelete.setLastName("Doe");
        contactToDelete.setUser(user);

        when(contactRepository.findByIdAndUserUsername(testContactId, testUsername))
            .thenReturn(Optional.of(contactToDelete));

        // Act
        contactService.delete(testUsername, testContactId);

        // Assert
        verify(contactRepository, times(1)).delete(contactToDelete);
    }

    @Test
    @DisplayName("delete: should throw NotFoundException when contact not found")
    void testDeleteContactNotFound() {
        // Arrange
        when(contactRepository.findByIdAndUserUsername(testContactId, testUsername))
            .thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException thrown = assertThrows(NotFoundException.class, () -> contactService.delete(testUsername, testContactId), "Should throw NotFoundException");
        assertTrue(thrown.getMessage().contains("Contatto"), "Exception message should mention contact");
        verify(contactRepository, times(0)).delete(any(Contact.class));
    }

    private AppUser createTestUser() {
        AppUser user = new AppUser();
        user.setUsername(testUsername);
        user.setEmail(testUsername + "@example.com");
        user.setPasswordHash("hashedpassword");
        return user;
    }
}
