package it.michelepal.rubrica.service;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
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
    public List<TagResponse> list(String username) {
        return tagRepository.findByUserUsernameOrderByNameAsc(username).stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public TagResponse create(String username, TagRequest request) {
        String name = normalizer.requiredText(request.name());
        if (tagRepository.existsByUserUsernameAndNameIgnoreCase(username, name)) {
            throw new ConflictException("Esiste gia' un tag con questo nome.");
        }
        AppUser user = userRepository.findByUsername(username).orElseThrow(() -> new NotFoundException("Utente non trovato."));
        Tag tag = new Tag();
        tag.setUser(user);
        tag.setName(name);
        tag.setColor(normalizer.text(request.color()));
        return mapper.toResponse(tagRepository.save(tag));
    }

    @Transactional
    public TagResponse update(String username, Long id, TagRequest request) {
        Tag tag = java.util.Objects.requireNonNull(tagRepository.findByIdAndUserUsername(id, username).orElseThrow(() -> new NotFoundException("Tag non trovato.")));
        String name = normalizer.requiredText(request.name());
        if (!tag.getName().equalsIgnoreCase(name) && tagRepository.existsByUserUsernameAndNameIgnoreCase(username, name)) {
            throw new ConflictException("Esiste gia' un tag con questo nome.");
        }
        tag.setName(name);
        tag.setColor(normalizer.text(request.color()));
        return mapper.toResponse(tag);
    }

    @Transactional
    public void delete(String username, Long id) {
        Tag tag = java.util.Objects.requireNonNull(tagRepository.findByIdAndUserUsername(id, username).orElseThrow(() -> new NotFoundException("Tag non trovato.")));
        tagRepository.delete(tag);
    }
}

