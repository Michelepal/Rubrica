package it.michelepal.rubrica.repository;

import it.michelepal.rubrica.entity.Contact;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

public interface ContactRepository extends JpaRepository<Contact, Long> {
    List<Contact> findByUserUsernameOrderByLastNameAscFirstNameAsc(String username);
    @NonNull Optional<Contact> findByIdAndUserUsername(Long id, String username);
}

