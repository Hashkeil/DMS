package at.fhtw.paperless.rest.repository;

import at.fhtw.paperless.rest.entity.Label;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LabelRepository extends JpaRepository<Label, Long> {
    Optional<Label> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}
