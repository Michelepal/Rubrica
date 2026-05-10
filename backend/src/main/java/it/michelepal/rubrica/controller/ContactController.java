package it.michelepal.rubrica.controller;

import it.michelepal.rubrica.dto.ContactRequest;
import it.michelepal.rubrica.dto.ContactResponse;
import it.michelepal.rubrica.service.ContactService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {
    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping
    public List<ContactResponse> list(Principal principal) {
        return contactService.list(principal.getName());
    }

    @GetMapping("/{id}")
    public ContactResponse get(Principal principal, @PathVariable("id") Long id) {
        return contactService.get(principal.getName(), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContactResponse create(Principal principal, @Valid @RequestBody ContactRequest request) {
        return contactService.create(principal.getName(), request);
    }

    @PutMapping("/{id}")
    public ContactResponse update(Principal principal, @PathVariable("id") Long id, @Valid @RequestBody ContactRequest request) {
        return contactService.update(principal.getName(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Principal principal, @PathVariable("id") Long id) {
        contactService.delete(principal.getName(), id);
    }
}
