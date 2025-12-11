package unical_support.unicalsupport2.data.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import unical_support.unicalsupport2.data.entities.DocumentChunk;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, String> {

    @Query(value = """
        SELECT c.id
        FROM chunks c
        JOIN documents d ON c.document_id = d.id
        JOIN categories cat ON d.category_id = cat.id
        JOIN chunk_embeddings ce ON ce.chunk_id = c.id
        WHERE LOWER(cat.name) = LOWER(:category)
        AND (ce.embedding <=> CAST(:embedding AS vector)) <= :maxDistance
        ORDER BY ce.embedding <=> CAST(:embedding AS vector)
        LIMIT :limitValue
        """, nativeQuery = true)
    List<String> findRelevantChunkIds(
            @Param("category") String category,
            @Param("embedding") String embedding,
            @Param("maxDistance") double maxDistance,
            @Param("limitValue") int limitValue
    );
}
