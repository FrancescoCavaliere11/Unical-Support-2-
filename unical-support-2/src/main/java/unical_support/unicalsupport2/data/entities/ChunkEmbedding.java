package unical_support.unicalsupport2.data.entities;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

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

    @Column(name = "embedding", columnDefinition = "vector(1536)", nullable = false)
    @JdbcTypeCode(SqlTypes.VECTOR)
    private float[] embedding;
}
