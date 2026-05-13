package it.michelepal.rubrica.controller;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    @DisplayName("contacts: should paginate with max size 10 and filter by favorite and tag")
    void contactsShouldPaginateAndFilter() throws Exception {
        Tag tag = new Tag();
        tag.setUser(user);
        tag.setName("Clienti");
        tag.setColor("#047857");
        tagRepository.save(tag);

        for (int index = 0; index < 12; index++) {
            Contact contact = new Contact();
            contact.setUser(user);
            contact.setFirstName("Contatto" + index);
            contact.setLastName("Demo");
            contact.setFavorite(index % 2 == 0);
            if (index % 2 == 0) {
                contact.getTags().add(tag);
            }
            contactRepository.save(contact);
        }

        mockMvc.perform(get("/api/contacts")
                .header("Authorization", bearerToken())
                .param("size", "50"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size").value(10))
            .andExpect(jsonPath("$.content.length()").value(10))
            .andExpect(jsonPath("$.totalElements").value(12));

        mockMvc.perform(get("/api/contacts")
                .header("Authorization", bearerToken())
                .param("favorite", "true")
                .param("tagId", tag.getId().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(6))
            .andExpect(jsonPath("$.content[0].favorite").value(true))
            .andExpect(jsonPath("$.content[0].tags[0].name").value("Clienti"));

        mockMvc.perform(get("/api/contacts")
                .header("Authorization", bearerToken())
                .param("q", "contatto1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(3));

        mockMvc.perform(get("/api/contacts")
                .header("Authorization", bearerToken())
                .param("q", "Clienti"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("contacts: should allow contacts to review and persist favorite changes")
    void contactsShouldAllowReviewRecordsAndFavoriteUpdates() throws Exception {
        String created = mockMvc.perform(post("/api/contacts")
                .header("Authorization", bearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "firstName":"SoloNome",
                      "lastName":null,
                      "company":null,
                      "jobTitle":null,
                      "notes":null,
                      "favorite":false,
                      "phones":[],
                      "emails":[],
                      "addresses":[],
                      "tagIds":[]
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.phones.length()").value(0))
            .andExpect(jsonPath("$.emails.length()").value(0))
            .andExpect(jsonPath("$.favorite").value(false))
            .andReturn()
            .getResponse()
            .getContentAsString();

        Long id = objectMapper.readTree(created).get("id").asLong();

        mockMvc.perform(put("/api/contacts/{id}", id)
                .header("Authorization", bearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "firstName":"SoloNome",
                      "lastName":null,
                      "company":null,
                      "jobTitle":null,
                      "notes":null,
                      "favorite":true,
                      "phones":[],
                      "emails":[],
                      "addresses":[],
                      "tagIds":[]
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.favorite").value(true));

        mockMvc.perform(get("/api/contacts")
                .header("Authorization", bearerToken())
                .param("favorite", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.content[0].firstName").value("SoloNome"));
    }

    @Test
    @DisplayName("tags: should paginate with max size 10")
    void tagsShouldPaginate() throws Exception {
        for (int index = 0; index < 12; index++) {
            Tag tag = new Tag();
            tag.setUser(user);
            tag.setName("Tag " + index);
            tagRepository.save(tag);
        }

        mockMvc.perform(get("/api/tags")
                .header("Authorization", bearerToken())
                .param("size", "50"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size").value(10))
            .andExpect(jsonPath("$.content.length()").value(10))
            .andExpect(jsonPath("$.totalElements").value(12));
    }

    @Test
    @DisplayName("tags: should report conflicts and delete by explicit path variable id")
    void tagConflictAndDeleteShouldUsePathVariableId() throws Exception {
        Tag tag = new Tag();
        tag.setUser(user);
        tag.setName("Lavoro");
        tag.setColor("#2563eb");
        tagRepository.save(tag);

        Contact contact = new Contact();
        contact.setUser(user);
        contact.setFirstName("Mario");
        contact.getTags().add(tag);
        contactRepository.save(contact);

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

        mockMvc.perform(get("/api/contacts")
                .header("Authorization", bearerToken())
                .param("tagId", tag.getId().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(0));
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
