package it.michelepal.rubrica.repository;

import it.michelepal.rubrica.entity.Tag;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findByUserUsernameOrderByNameAsc(String username);
    Optional<Tag> findByIdAndUserUsername(Long id, String username);
    boolean existsByUserUsernameAndNameIgnoreCase(String username, String name);
}

