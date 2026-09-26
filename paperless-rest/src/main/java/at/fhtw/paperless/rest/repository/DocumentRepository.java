package at.fhtw.paperless.rest.repository;

import at.fhtw.paperless.rest.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    boolean existsByCategoryId(Long categoryId);
}
