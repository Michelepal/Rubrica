package it.michelepal.rubrica.repository;

import static org.junit.jupiter.api.Assertions.*;

import it.michelepal.rubrica.entity.AppUser;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("AppUserRepository Tests")
public
class AppUserRepositoryTest {

    @Autowired
    private AppUserRepository appUserRepository;

    private AppUser testUser;

    @BeforeEach
    void setUp() {
        testUser = new AppUser();
        testUser.setUsername("testuser");
        testUser.setEmail("testuser@example.com");
        testUser.setPasswordHash("hashedpassword");
    }

    @Test
    @DisplayName("findByUsername: should find a user by username")
    void testFindByUsername() {
        if (testUser != null) {
            // Arrange
            appUserRepository.save(testUser);
        } else {
            fail("Test user was not initialized properly");
        }

        // Act
        Optional<AppUser> result = appUserRepository.findByUsername("testuser");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        assertEquals("testuser@example.com", result.get().getEmail());
    }

    @Test
    @DisplayName("findByUsername: should return empty when username not found")
    void testFindByUsernameNotFound() {
        // Act
        Optional<AppUser> result = appUserRepository.findByUsername("nonexistent");

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("existsByUsername: should return true when username exists")
    void testExistsByUsername() {
        // Arrange
        if (testUser != null) {
            // Arrange
            appUserRepository.save(testUser);
        } else {
            fail("Test user was not initialized properly");
        }

        // Act
        boolean exists = appUserRepository.existsByUsername("testuser");

        // Assert
        assertTrue(exists);
    }

    @Test
    @DisplayName("existsByUsername: should return false when username does not exist")
    void testExistsByUsernameNotFound() {
        // Act
        boolean exists = appUserRepository.existsByUsername("nonexistent");

        // Assert
        assertFalse(exists);
    }

    @Test
    @DisplayName("existsByEmail: should return true when email exists")
    void testExistsByEmail() {
        // Arrange
        if (testUser != null) {
            // Arrange
            appUserRepository.save(testUser);
        } else {
            fail("Test user was not initialized properly");
        }

        // Act
        boolean exists = appUserRepository.existsByEmail("testuser@example.com");

        // Assert
        assertTrue(exists);
    }

    @Test
    @DisplayName("existsByEmail: should return false when email does not exist")
    void testExistsByEmailNotFound() {
        // Act
        boolean exists = appUserRepository.existsByEmail("nonexistent@example.com");

        // Assert
        assertFalse(exists);
    }

    @Test
    @DisplayName("should enforce unique username constraint")
    void testUniqueUsernameConstraint() {
        // Arrange
        if (testUser != null) {
            // Arrange
            appUserRepository.save(testUser);
        } else {
            fail("Test user was not initialized properly");
        }

        AppUser duplicateUser = new AppUser();
        duplicateUser.setUsername("testuser");
        duplicateUser.setEmail("different@example.com");
        duplicateUser.setPasswordHash("hashedpassword");

        // Act & Assert
        assertThrows(Exception.class, () -> appUserRepository.save(duplicateUser));
    }

    @Test
    @DisplayName("should enforce unique email constraint")
    void testUniqueEmailConstraint() {
        // Arrange
        if (testUser != null) {
            // Arrange
            appUserRepository.save(testUser);
        } else {
            fail("Test user was not initialized properly");
        }

        AppUser duplicateUser = new AppUser();
        duplicateUser.setUsername("anotheruser");
        duplicateUser.setEmail("testuser@example.com");
        duplicateUser.setPasswordHash("hashedpassword");

        // Act & Assert
        assertThrows(Exception.class, () -> appUserRepository.save(duplicateUser));
    }

    @Test
    @DisplayName("save: should persist and retrieve a user")
    void testSaveAndRetrieve() {
        // Act
        if (testUser != null) {
            // Arrange
            AppUser saved = appUserRepository.save(testUser);
            // Assert
            assertNotNull(saved.getId());
            Optional<AppUser> retrieved = appUserRepository.findById(saved.getId() != null? saved.getId() : -1L);
            assertTrue(retrieved.isPresent());
            assertEquals("testuser", retrieved.get().getUsername());
        } else {
            fail("Test user was not initialized properly");
        }

    }

    @Test
    @DisplayName("delete: should remove a user from database")
    void testDelete() {
        if (testUser != null) {
            // Arrange
            AppUser saved = appUserRepository.save(testUser);
            Long userId = saved.getId();

            // Act
            appUserRepository.delete(saved);

            // Assert
            Optional<AppUser> result = appUserRepository.findById(userId != null? userId : -1L);
            assertTrue(result.isEmpty());
        } else {
            fail("Test user was not initialized properly");
        }
    }
}
