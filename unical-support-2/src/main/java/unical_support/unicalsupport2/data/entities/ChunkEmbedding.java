package unical_support.unicalsupport2.data.entities;

import com.pgvector.PGvector;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UuidGenerator;
import unical_support.unicalsupport2.data.type.VectorType;

@Entity
@Table(name = "chunk_embeddings")
@Data
public class ChunkEmbedding {

    @Id
    @UuidGenerator
    private String id;

    @Column(name = "embedding", columnDefinition = "vector", nullable = false)
    @Type(VectorType.class)
    private PGvector embedding;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL,optional = false)
    @JoinColumn(name = "chunk_id", nullable = false)
    private DocumentChunk chunk;
}
