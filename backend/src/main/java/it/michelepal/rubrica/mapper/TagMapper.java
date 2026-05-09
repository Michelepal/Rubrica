package it.michelepal.rubrica.mapper;

import it.michelepal.rubrica.dto.TagResponse;
import it.michelepal.rubrica.entity.Tag;
import org.springframework.stereotype.Component;

@Component
public class TagMapper {
    public TagResponse toResponse(Tag tag) {
        return new TagResponse(tag.getId(), tag.getName(), tag.getColor());
    }
}

