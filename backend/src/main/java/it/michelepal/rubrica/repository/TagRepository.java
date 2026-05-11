package it.michelepal.rubrica.repository;

import it.michelepal.rubrica.entity.Tag;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findByUserUsernameOrderByNameAsc(String username);
    Page<Tag> findByUserUsername(String username, Pageable pageable);
    @NonNull Optional<Tag> findByIdAndUserUsername(Long id, String username);
    boolean existsByUserUsernameAndNameIgnoreCase(String username, String name);
}
