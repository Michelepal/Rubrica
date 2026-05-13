package it.michelepal.rubrica.controller;

import it.michelepal.rubrica.dto.ContactRequest;
import it.michelepal.rubrica.dto.ContactResponse;
import it.michelepal.rubrica.dto.PageResponse;
import it.michelepal.rubrica.service.ContactService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public PageResponse<ContactResponse> list(
        Principal principal,
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "10") int size,
        @RequestParam(name = "q", required = false) String query,
        @RequestParam(name = "tagId", required = false) Long tagId,
        @RequestParam(name = "favorite", required = false) Boolean favorite,
        @RequestParam(name = "sort", defaultValue = "name") String sort
    ) {
        return contactService.list(principal.getName(), page, size, query, tagId, favorite, sort);
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
