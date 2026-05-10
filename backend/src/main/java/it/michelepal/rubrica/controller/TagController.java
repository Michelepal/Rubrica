package it.michelepal.rubrica.controller;

import it.michelepal.rubrica.dto.TagRequest;
import it.michelepal.rubrica.dto.TagResponse;
import it.michelepal.rubrica.service.TagService;
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
@RequestMapping("/api/tags")
public class TagController {
    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public List<TagResponse> list(Principal principal) {
        return tagService.list(principal.getName());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TagResponse create(Principal principal, @Valid @RequestBody TagRequest request) {
        return tagService.create(principal.getName(), request);
    }

    @PutMapping("/{id}")
    public TagResponse update(Principal principal, @PathVariable("id") Long id, @Valid @RequestBody TagRequest request) {
        return tagService.update(principal.getName(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Principal principal, @PathVariable("id") Long id) {
        tagService.delete(principal.getName(), id);
    }
}
