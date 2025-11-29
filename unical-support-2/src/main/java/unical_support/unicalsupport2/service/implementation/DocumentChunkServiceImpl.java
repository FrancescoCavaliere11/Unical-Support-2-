package unical_support.unicalsupport2.service.implementation;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate; // Importante
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importante
import unical_support.unicalsupport2.data.dto.classifier.ClassificationResultDto;
import unical_support.unicalsupport2.data.dto.classifier.SingleCategoryDto;
import unical_support.unicalsupport2.data.entities.DocumentChunk;
import unical_support.unicalsupport2.data.repositories.DocumentChunkRepository;
import unical_support.unicalsupport2.service.interfaces.DocumentChunkService;
import unical_support.unicalsupport2.service.interfaces.LlmClient;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentChunkServiceImpl implements DocumentChunkService {

    private final DocumentChunkRepository chunkRepository;
    private final LlmClient llmClient;
    private final JdbcTemplate jdbcTemplate;
    private final Gson gson = new Gson();

    @Override
    @Transactional(readOnly = true)
    public List<DocumentChunk> findRelevantChunks(ClassificationResultDto classificationResult, int kPerCategory) {
        List<DocumentChunk> results = new ArrayList<>();

        System.out.println("\n--- INIZIO RICERCA RAG ---"); // Log inizio

        for (SingleCategoryDto cat : classificationResult.getCategories()) {
            if ("NON_RICONOSCIUTA".equalsIgnoreCase(cat.getCategory())) continue;

            String queryText = cat.getText();
            if (queryText == null || queryText.isBlank()) queryText = classificationResult.getExplanation();

            System.out.println("Cerco documenti per categoria: " + cat.getCategory() + " basandomi su: \"" + queryText + "\"");

            // 1. Genera embedding query
            float[] embedding = llmClient.embed(queryText);
            String embeddingString = gson.toJson(embedding);

            // 2. Query SQL
            String sql = """
                SELECT c.id 
                FROM chunks c
                JOIN documents d ON c.document_id = d.id
                JOIN categories cat ON d.category_id = cat.id
                JOIN chunk_embeddings ce ON ce.chunk_id = c.id
                WHERE LOWER(cat.name) = LOWER(?)
                ORDER BY ce.embedding <=> ?::vector
                LIMIT ?
            """;

            List<String> chunkIds = jdbcTemplate.query(sql,
                    (ps) -> {
                        ps.setString(1, cat.getCategory());
                        ps.setString(2, embeddingString);
                        ps.setInt(3, kPerCategory);
                    },
                    (rs, rowNum) -> rs.getString("id")
            );

            if (!chunkIds.isEmpty()) {
                List<DocumentChunk> chunks = chunkRepository.findAllById(chunkIds);

                for (DocumentChunk chunk : chunks) {
                    Hibernate.initialize(chunk.getDocument());

                    // LOg che dice da quale file sta leggendo
                    System.out.println("   [RAG HIT] Trovato info utile nel file: " + chunk.getDocument().getOriginalFilename());

                }
                results.addAll(chunks);
            } else {
                System.out.println("   [RAG MISS] Nessun documento trovato per questa categoria.");
            }
        }
        System.out.println("--- FINE RICERCA RAG ---\n");
        return results;
    }
}