package it.michelepal.rubrica.service;

import it.michelepal.rubrica.dto.PageResponse;
import it.michelepal.rubrica.dto.TagRequest;
import it.michelepal.rubrica.dto.TagResponse;
import it.michelepal.rubrica.entity.AppUser;
import it.michelepal.rubrica.entity.Tag;
import it.michelepal.rubrica.exception.ConflictException;
import it.michelepal.rubrica.exception.NotFoundException;
import it.michelepal.rubrica.mapper.TagMapper;
import it.michelepal.rubrica.repository.AppUserRepository;
import it.michelepal.rubrica.repository.TagRepository;
import java.util.List;
import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@SuppressWarnings("null")
public class TagService {
    private final TagRepository tagRepository;
    private final AppUserRepository userRepository;
    private final TagMapper mapper;
    private final InputNormalizer normalizer;

    public TagService(TagRepository tagRepository, AppUserRepository userRepository, TagMapper mapper, InputNormalizer normalizer) {
        this.tagRepository = tagRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
        this.normalizer = normalizer;
    }

    @Transactional(readOnly = true)
    public PageResponse<TagResponse> list(String username, int page, int size) {
        Page<Tag> tags = tagRepository.findByUserUsername(
            username,
            PageRequest.of(safePage(page), safeSize(size), Sort.by("name").ascending())
        );
        return new PageResponse<>(
            tags.getContent().stream().map(mapper::toResponse).toList(),
            tags.getNumber(),
            tags.getSize(),
            tags.getTotalElements(),
            tags.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public List<TagResponse> list(String username) {
        return tagRepository.findByUserUsernameOrderByNameAsc(username).stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public TagResponse create(String username, TagRequest request) {
        String name = normalizer.requiredText(request.name());
        if (tagRepository.existsByUserUsernameAndNameIgnoreCase(username, name)) {
            throw new ConflictException("Esiste già un tag con questo nome.");
        }
        AppUser user = userRepository.findByUsername(username).orElseThrow(() -> new NotFoundException("Utente non trovato."));
        Tag tag = new Tag();
        tag.setUser(user);
        tag.setName(name);
        tag.setColor(normalizer.text(request.color()));
        return mapper.toResponse(tagRepository.save(Objects.requireNonNull(tag)));
    }

    @Transactional
    public TagResponse update(String username, Long id, TagRequest request) {
        Tag tag = tagRepository.findByIdAndUserUsername(id, username)
            .orElseThrow(() -> new NotFoundException("Tag non trovato."));
        String name = normalizer.requiredText(request.name());
        if (!tag.getName().equalsIgnoreCase(name) && tagRepository.existsByUserUsernameAndNameIgnoreCase(username, name)) {
            throw new ConflictException("Esiste già un tag con questo nome.");
        }
        tag.setName(name);
        tag.setColor(normalizer.text(request.color()));
        return mapper.toResponse(tag);
    }

    @Transactional
    public void delete(String username, Long id) {
        Tag tag = tagRepository.findByIdAndUserUsername(id, username)
            .orElseThrow(() -> new NotFoundException("Tag non trovato."));
        tagRepository.delete(tag);
    }

    private int safePage(int page) {
        return Math.max(page, 0);
    }

    private int safeSize(int size) {
        return Math.min(Math.max(size, 1), 10);
    }
}
