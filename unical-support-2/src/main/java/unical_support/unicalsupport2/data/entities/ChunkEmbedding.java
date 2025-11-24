package unical_support.unicalsupport2.data.entities;

import com.pgvector.PGvector;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "chunk_embeddings")
@Data
public class ChunkEmbedding {
    @UuidGenerator
    @Id
    private String id;

    @OneToOne()
    @JoinColumn(name = "chunk_id", nullable = false)
    private DocumentChunk chunk;

    @Column(name = "embedding", nullable = false)
    private PGvector embedding;
}
