package unical_support.unicalsupport2.data.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import unical_support.unicalsupport2.data.entities.DocumentChunk;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, String> {

}