package at.fhtw.paperless.rest.repository;

import at.fhtw.paperless.rest.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    boolean existsByCategoryId(Long categoryId);

    @Modifying
    @Query(value = "DELETE FROM document_labels WHERE label_id = :labelId", nativeQuery = true)
    void deleteLabelReference(@Param("labelId") Long labelId);
}
