package it.michelepal.rubrica.repository;

import static org.junit.jupiter.api.Assertions.*;

import it.michelepal.rubrica.entity.AppUser;
import it.michelepal.rubrica.entity.Tag;
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
@DisplayName("TagRepository Tests")
public
class TagRepositoryTest {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    private AppUser testUser;
    private Tag testTag;

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

        // Create a test tag
        testTag = new Tag();
        testTag.setUser(testUser);
        testTag.setName("Work");
        testTag.setColor("#FF0000");
    }

    @Test
    @DisplayName("findByUserUsernameOrderByNameAsc: should return all tags for a user ordered by name")
    void testFindByUserUsernameOrderByNameAsc() {
        // Arrange
        Tag tag1 = new Tag();
        tag1.setUser(testUser);
        tag1.setName("Work");
        tagRepository.save(tag1);

        Tag tag2 = new Tag();
        tag2.setUser(testUser);
        tag2.setName("Personal");
        tagRepository.save(tag2);

        Tag tag3 = new Tag();
        tag3.setUser(testUser);
        tag3.setName("Family");
        tagRepository.save(tag3);

        // Act
        List<Tag> result = tagRepository.findByUserUsernameOrderByNameAsc("testuser");

        // Assert
        assertEquals(3, result.size());
        assertEquals("Family", result.get(0).getName());
        assertEquals("Personal", result.get(1).getName());
        assertEquals("Work", result.get(2).getName());
    }

    @Test
    @DisplayName("findByUserUsernameOrderByNameAsc: should return empty list for user with no tags")
    void testFindByUserUsernameOrderByNameAscEmpty() {
        // Act
        List<Tag> result = tagRepository.findByUserUsernameOrderByNameAsc("testuser");

        // Assert
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("findByUserUsernameOrderByNameAsc: should not return tags from other users")
    void testFindByUserUsernameOrderByNameAscMultipleUsers() {
        // Arrange
        AppUser anotherUser = new AppUser();
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPasswordHash("hashedpassword");
        appUserRepository.save(anotherUser);

        Tag tag1 = new Tag();
        tag1.setUser(testUser);
        tag1.setName("Work");
        tagRepository.save(tag1);

        Tag tag2 = new Tag();
        tag2.setUser(anotherUser);
        tag2.setName("Work");
        tagRepository.save(tag2);

        // Act
        List<Tag> result = tagRepository.findByUserUsernameOrderByNameAsc("testuser");

        // Assert
        assertEquals(1, result.size());
        assertEquals(testUser.getId(), result.get(0).getUser().getId());
    }

    @Test
    @DisplayName("findByIdAndUserUsername: should find a tag by id and username")
    void testFindByIdAndUserUsername() {
        if (testTag != null) {
            // Arrange
            Tag saved = tagRepository.save(testTag);
            // Act
            Optional<Tag> result = tagRepository.findByIdAndUserUsername(saved.getId(), "testuser");

            // Assert
            assertTrue(result.isPresent());
            assertEquals("Work", result.get().getName());
        } else {
            fail("Test tag was not initialized properly");
        }
    }

    @Test
    @DisplayName("findByIdAndUserUsername: should return empty when tag does not belong to user")
    void testFindByIdAndUserUsernameWrongUser() {
        if (testTag != null) {
            // Arrange
            Tag saved = tagRepository.save(testTag);
            // Act
            Optional<Tag> result = tagRepository.findByIdAndUserUsername(saved.getId(), "wronguser");

            // Assert
            assertTrue(result.isEmpty());
        } else {
            fail("Test tag was not initialized properly");
        }
    }

    @Test
    @DisplayName("findByIdAndUserUsername: should return empty when tag id not found")
    void testFindByIdAndUserUsernameNotFound() {
        // Act
        Optional<Tag> result = tagRepository.findByIdAndUserUsername(999L, "testuser");

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("existsByUserUsernameAndNameIgnoreCase: should return true when tag exists")
    void testExistsByUserUsernameAndNameIgnoreCase() {
        if (testTag != null) {
            // Arrange
            tagRepository.save(testTag);
        } else {
            fail("Test tag was not initialized properly");
        }

        // Act
        boolean exists = tagRepository.existsByUserUsernameAndNameIgnoreCase("testuser", "work");

        // Assert
        assertTrue(exists);
    }

    @Test
    @DisplayName("existsByUserUsernameAndNameIgnoreCase: should be case insensitive")
    void testExistsByUserUsernameAndNameIgnoreCaseCaseSensitive() {
        // Arrange
        if (testTag != null) {
            // Arrange
            tagRepository.save(testTag);
        } else {
            fail("Test tag was not initialized properly");
        }

        // Act
        boolean existsLower = tagRepository.existsByUserUsernameAndNameIgnoreCase("testuser", "work");
        boolean existsUpper = tagRepository.existsByUserUsernameAndNameIgnoreCase("testuser", "WORK");
        boolean existsMixed = tagRepository.existsByUserUsernameAndNameIgnoreCase("testuser", "WoRk");

        // Assert
        assertTrue(existsLower);
        assertTrue(existsUpper);
        assertTrue(existsMixed);
    }

    @Test
    @DisplayName("existsByUserUsernameAndNameIgnoreCase: should return false when tag does not exist")
    void testExistsByUserUsernameAndNameIgnoreCaseNotFound() {
        // Act
        boolean exists = tagRepository.existsByUserUsernameAndNameIgnoreCase("testuser", "nonexistent");

        // Assert
        assertFalse(exists);
    }

    @Test
    @DisplayName("existsByUserUsernameAndNameIgnoreCase: should not return tags from other users")
    void testExistsByUserUsernameAndNameIgnoreCaseWrongUser() {
        // Arrange
        if (testTag != null) {
            // Arrange
            tagRepository.save(testTag);
        } else {
            fail("Test tag was not initialized properly");
        }

        // Act
        boolean exists = tagRepository.existsByUserUsernameAndNameIgnoreCase("anotheruser", "work");

        // Assert
        assertFalse(exists);
    }

    @Test
    @DisplayName("save: should persist a tag")
    void testSaveTag() {
        // Act
       if (testTag != null) {
            // Arrange
            Tag saved = tagRepository.save(testTag);
            // Assert
            assertNotNull(saved.getId());
            Optional<Tag> retrieved = tagRepository.findById(saved.getId() != null? saved.getId() : -1L);
            assertTrue(retrieved.isPresent());
            assertEquals("Work", retrieved.get().getName());
        } else {
            fail("Test tag was not initialized properly");
        }
    }

    @Test
    @DisplayName("delete: should remove a tag")
    void testDeleteTag() {
        if (testTag != null) {
            // Arrange
            Tag saved = tagRepository.save(testTag);
            Long tagId = saved.getId();

            // Act
            tagRepository.delete(saved);

            // Assert
            Optional<Tag> result = tagRepository.findById(tagId != null? tagId : -1L);
            assertTrue(result.isEmpty());
        } else {
            fail("Test tag was not initialized properly");
        }
    }
}
