package it.michelepal.rubrica.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import it.michelepal.rubrica.dto.TagRequest;
import it.michelepal.rubrica.dto.TagResponse;
import it.michelepal.rubrica.entity.AppUser;
import it.michelepal.rubrica.entity.Tag;
import it.michelepal.rubrica.exception.ConflictException;
import it.michelepal.rubrica.exception.NotFoundException;
import it.michelepal.rubrica.mapper.TagMapper;
import it.michelepal.rubrica.repository.AppUserRepository;
import it.michelepal.rubrica.repository.TagRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("TagService Tests")
@SuppressWarnings("null")
public
class TagServiceTest {

    private TagService tagService;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private AppUserRepository userRepository;

    @Mock
    private TagMapper mapper;

    @Mock
    private InputNormalizer normalizer;

    private String testUsername = "testuser";
    private Long testTagId = 1L;

    @BeforeEach
    void setUp() {
        tagService = new TagService(tagRepository, userRepository, mapper, normalizer);
    }

    @Test
    @DisplayName("list: should return all tags for a user ordered by name")
    void testListTags() {
        // Arrange
        Tag tag1 = new Tag();
        tag1.setName("Work");
        tag1.setColor("#FF0000");

        Tag tag2 = new Tag();
        tag2.setName("Personal");
        tag2.setColor("#00FF00");

        TagResponse response1 = new TagResponse(1L, "Work", "#FF0000");
        TagResponse response2 = new TagResponse(2L, "Personal", "#00FF00");

        when(tagRepository.findByUserUsernameOrderByNameAsc(testUsername))
            .thenReturn(Arrays.asList(tag2, tag1));
        when(mapper.toResponse(tag2)).thenReturn(response2);
        when(mapper.toResponse(tag1)).thenReturn(response1);

        // Act
        List<TagResponse> result = tagService.list(testUsername);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(2, result.size(), "Should return 2 tags");
        assertEquals("Personal", result.get(0).name(), "First tag should be Personal");
        assertEquals("Work", result.get(1).name(), "Second tag should be Work");
        verify(tagRepository, times(1)).findByUserUsernameOrderByNameAsc(testUsername);
        verify(mapper, times(2)).toResponse(any(Tag.class));
    }

    @Test
    @DisplayName("list: should return empty list when user has no tags")
    void testListTagsEmpty() {
        // Arrange
        when(tagRepository.findByUserUsernameOrderByNameAsc(testUsername))
            .thenReturn(Arrays.asList());

        // Act
        List<TagResponse> result = tagService.list(testUsername);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertTrue(result.isEmpty(), "Result should be empty");
        verify(tagRepository, times(1)).findByUserUsernameOrderByNameAsc(testUsername);
    }

    @Test
    @DisplayName("create: should create a new tag")
    void testCreateTag() {
        // Arrange
        TagRequest request = new TagRequest("Work", "#FF0000");
        AppUser user = createTestUser();

        Tag savedTag = new Tag();
        savedTag.setName("Work");
        savedTag.setColor("#FF0000");
        savedTag.setUser(user);

        TagResponse response = new TagResponse(testTagId, "Work", "#FF0000");

        when(normalizer.requiredText("Work")).thenReturn("Work");
        when(normalizer.text("#FF0000")).thenReturn("#FF0000");
        when(tagRepository.existsByUserUsernameAndNameIgnoreCase(testUsername, "Work"))
            .thenReturn(false);
        when(userRepository.findByUsername(testUsername)).thenReturn(Optional.of(user));
        when(tagRepository.save(any(Tag.class))).thenReturn(savedTag);
        when(mapper.toResponse(savedTag)).thenReturn(response);

        // Act
        TagResponse result = tagService.create(testUsername, request);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals("Work", result.name(), "Tag name should be 'Work'");
        assertEquals("#FF0000", result.color(), "Tag color should be '#FF0000'");
        verify(tagRepository, times(1)).existsByUserUsernameAndNameIgnoreCase(testUsername, "Work");
        verify(userRepository, times(1)).findByUsername(testUsername);
        verify(tagRepository, times(1)).save(any(Tag.class));
    }

    @Test
    @DisplayName("create: should throw ConflictException when tag name already exists")
    void testCreateTagConflict() {
        // Arrange
        TagRequest request = new TagRequest("Work", "#FF0000");

        when(normalizer.requiredText("Work")).thenReturn("Work");
        when(tagRepository.existsByUserUsernameAndNameIgnoreCase(testUsername, "Work"))
            .thenReturn(true);

        // Act & Assert
        ConflictException thrown = assertThrows(ConflictException.class, () -> tagService.create(testUsername, request), "Should throw ConflictException");
        assertTrue(thrown.getMessage().contains("tag"), "Exception message should mention tag");
        verify(tagRepository, times(0)).save(any(Tag.class));
    }

    @Test
    @DisplayName("create: should throw NotFoundException when user not found")
    void testCreateTagUserNotFound() {
        // Arrange
        TagRequest request = new TagRequest("Work", "#FF0000");

        when(normalizer.requiredText("Work")).thenReturn("Work");
        when(tagRepository.existsByUserUsernameAndNameIgnoreCase(testUsername, "Work"))
            .thenReturn(false);
        when(userRepository.findByUsername(testUsername)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException thrown = assertThrows(NotFoundException.class, () -> tagService.create(testUsername, request), "Should throw NotFoundException");
        assertTrue(thrown.getMessage().contains("Utente"), "Exception message should mention user");
        verify(tagRepository, times(0)).save(any(Tag.class));
    }

    @Test
    @DisplayName("update: should update an existing tag")
    void testUpdateTag() {
        // Arrange
        TagRequest request = new TagRequest("Updated Work", "#00FF00");
        AppUser user = createTestUser();

        Tag existingTag = new Tag();
        existingTag.setName("Work");
        existingTag.setColor("#FF0000");
        existingTag.setUser(user);

        TagResponse response = new TagResponse(testTagId, "Updated Work", "#00FF00");

        when(tagRepository.findByIdAndUserUsername(testTagId, testUsername))
            .thenReturn(Optional.of(existingTag));
        when(normalizer.requiredText("Updated Work")).thenReturn("Updated Work");
        when(normalizer.text("#00FF00")).thenReturn("#00FF00");
        when(tagRepository.existsByUserUsernameAndNameIgnoreCase(testUsername, "Updated Work"))
            .thenReturn(false);
        when(mapper.toResponse(existingTag)).thenReturn(response);

        // Act
        TagResponse result = tagService.update(testUsername, testTagId, request);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals("Updated Work", result.name(), "Tag name should be updated");
        assertEquals("#00FF00", result.color(), "Tag color should be updated");
        verify(tagRepository, times(1)).findByIdAndUserUsername(testTagId, testUsername);
    }

    @Test
    @DisplayName("update: should throw NotFoundException when tag not found")
    void testUpdateTagNotFound() {
        // Arrange
        TagRequest request = new TagRequest("Updated Work", "#00FF00");

        when(tagRepository.findByIdAndUserUsername(testTagId, testUsername))
            .thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException thrown = assertThrows(NotFoundException.class, () -> tagService.update(testUsername, testTagId, request), "Should throw NotFoundException");
        assertTrue(thrown.getMessage().contains("Tag"), "Exception message should mention tag");
    }

    @Test
    @DisplayName("update: should throw ConflictException when new name already exists for another tag")
    void testUpdateTagConflict() {
        // Arrange
        TagRequest request = new TagRequest("ExistingTag", "#00FF00");
        AppUser user = createTestUser();

        Tag existingTag = new Tag();
        existingTag.setName("Work");
        existingTag.setUser(user);

        when(tagRepository.findByIdAndUserUsername(testTagId, testUsername))
            .thenReturn(Optional.of(existingTag));
        when(normalizer.requiredText("ExistingTag")).thenReturn("ExistingTag");
        when(tagRepository.existsByUserUsernameAndNameIgnoreCase(testUsername, "ExistingTag"))
            .thenReturn(true);

        // Act & Assert
        ConflictException thrown = assertThrows(ConflictException.class, () -> tagService.update(testUsername, testTagId, request), "Should throw ConflictException");
        assertTrue(thrown.getMessage().contains("tag"), "Exception message should mention tag");
    }

    @Test
    @DisplayName("delete: should delete a tag")
    void testDeleteTag() {
        // Arrange
        AppUser user = createTestUser();
        Tag tagToDelete = new Tag();
        tagToDelete.setName("Work");
        tagToDelete.setColor("#FF0000");
        tagToDelete.setUser(user);

        when(tagRepository.findByIdAndUserUsername(testTagId, testUsername))
            .thenReturn(Optional.of(tagToDelete));

        // Act
        tagService.delete(testUsername, testTagId);

        // Assert
        verify(tagRepository, times(1)).delete(tagToDelete);
    }

    @Test
    @DisplayName("delete: should throw NotFoundException when tag not found")
    void testDeleteTagNotFound() {
        // Arrange
        when(tagRepository.findByIdAndUserUsername(testTagId, testUsername))
            .thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException thrown = assertThrows(NotFoundException.class, () -> tagService.delete(testUsername, testTagId), "Should throw NotFoundException");
        assertTrue(thrown.getMessage().contains("Tag"), "Exception message should mention tag");
        verify(tagRepository, times(0)).delete(any(Tag.class));
    }

    private AppUser createTestUser() {
        AppUser user = new AppUser();
        user.setUsername(testUsername);
        user.setEmail(testUsername + "@example.com");
        user.setPasswordHash("hashedpassword");
        return user;
    }
}
