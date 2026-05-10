package it.michelepal.rubrica.controller;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.michelepal.rubrica.entity.AppUser;
import it.michelepal.rubrica.entity.Contact;
import it.michelepal.rubrica.entity.Tag;
import it.michelepal.rubrica.repository.AppUserRepository;
import it.michelepal.rubrica.repository.ContactRepository;
import it.michelepal.rubrica.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("REST API integration tests")
@SuppressWarnings("null")
public class RestApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private AppUser user;

    @BeforeEach
    void setUp() {
        contactRepository.deleteAll();
        tagRepository.deleteAll();
        userRepository.deleteAll();

        user = new AppUser();
        user.setUsername("admin");
        user.setEmail("admin@example.local");
        user.setPasswordHash(passwordEncoder.encode("admin"));
        userRepository.save(user);
    }

    @Test
    @DisplayName("login: should return a bearer token for valid credentials")
    void loginShouldReturnToken() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username":"admin","password":"admin"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.username").value("admin"));
    }

    @Test
    @DisplayName("login: should return unauthorized for invalid credentials")
    void loginShouldRejectInvalidCredentials() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username":"admin","password":"wrong"}
                    """))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Credenziali non valide."));
    }

    @Test
    @DisplayName("contacts: should require authentication")
    void contactsShouldRequireAuthentication() throws Exception {
        mockMvc.perform(post("/api/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validContactJson("Mario")))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("contacts: should update a contact through explicit path variable id")
    void updateContactShouldUsePathVariableId() throws Exception {
        Contact contact = new Contact();
        contact.setUser(user);
        contact.setFirstName("Mario");
        contactRepository.save(contact);

        mockMvc.perform(put("/api/contacts/{id}", contact.getId())
                .header("Authorization", bearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(validContactJson("Luigi")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(contact.getId()))
            .andExpect(jsonPath("$.firstName").value("Luigi"));
    }

    @Test
    @DisplayName("contacts: should return validation details for invalid payload")
    void createContactShouldReturnValidationDetails() throws Exception {
        mockMvc.perform(post("/api/contacts")
                .header("Authorization", bearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"firstName":"","favorite":false,"phones":[],"emails":[],"addresses":[],"tagIds":[]}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.fieldErrors.firstName").exists());
    }

    @Test
    @DisplayName("tags: should report conflicts and delete by explicit path variable id")
    void tagConflictAndDeleteShouldUsePathVariableId() throws Exception {
        Tag tag = new Tag();
        tag.setUser(user);
        tag.setName("Lavoro");
        tag.setColor("#2563eb");
        tagRepository.save(tag);

        mockMvc.perform(post("/api/tags")
                .header("Authorization", bearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"lavoro","color":"#2563eb"}
                    """))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("Esiste già un tag con questo nome."));

        mockMvc.perform(delete("/api/tags/{id}", tag.getId())
                .header("Authorization", bearerToken()))
            .andExpect(status().isNoContent());
    }

    private String bearerToken() throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username":"admin","password":"admin"}
                    """))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        JsonNode body = objectMapper.readTree(response);
        return "Bearer " + body.get("token").asText();
    }

    private String validContactJson(String firstName) {
        return """
            {
              "firstName":"%s",
              "lastName":"Rossi",
              "company":"Studio Rossi",
              "jobTitle":"Consulente",
              "notes":"Nota",
              "favorite":false,
              "phones":[{"type":"mobile","value":"1234567890","primary":true}],
              "emails":[{"type":"email","value":"mario.rossi@example.local","primary":true}],
              "addresses":[],
              "tagIds":[]
            }
            """.formatted(firstName);
    }
}
