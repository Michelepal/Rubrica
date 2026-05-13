package it.michelepal.rubrica.repository;

import it.michelepal.rubrica.entity.Contact;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

public interface ContactRepository extends JpaRepository<Contact, Long>, JpaSpecificationExecutor<Contact> {
    @EntityGraph(attributePaths = "emails")
    List<Contact> findByUserUsernameOrderByLastNameAscFirstNameAsc(String username);
    @EntityGraph(attributePaths = "phones")
    @Query("select c from Contact c join c.user u where u.username = :username order by c.lastName asc, c.firstName asc")
    List<Contact> findWithPhonesByUsername(@Param("username") String username);
    @EntityGraph(attributePaths = "tags")
    List<Contact> findByTagsIdAndUserUsername(Long tagId, String username);
    @NonNull Optional<Contact> findByIdAndUserUsername(Long id, String username);
}
