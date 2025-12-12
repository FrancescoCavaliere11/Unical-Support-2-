package unical_support.unicalsupport2.service.implementation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unical_support.unicalsupport2.data.dto.classifier.ClassificationResultDto;
import unical_support.unicalsupport2.data.dto.classifier.SingleCategoryDto;
import unical_support.unicalsupport2.data.entities.DocumentChunk;
import unical_support.unicalsupport2.data.repositories.DocumentChunkRepository;
import unical_support.unicalsupport2.service.interfaces.DocumentChunkService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentChunkServiceImpl implements DocumentChunkService {

    private static final double MAX_COSINE_DISTANCE = 0.6;
    private final DocumentChunkRepository chunkRepository;
    private final GeminiApiClientImpl llmClient;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public List<DocumentChunk> findRelevantChunks(ClassificationResultDto classificationResult, int kPerCategory) {
        List<DocumentChunk> results = new ArrayList<>();

        System.out.println("\n--- INIZIO RICERCA RAG ---");

        for (SingleCategoryDto cat : classificationResult.getCategories()) {
            if ("NON RICONOSCIUTA".equalsIgnoreCase(cat.getCategory())) continue;

            String queryText = cat.getText();
            if (queryText == null || queryText.isBlank()) queryText = classificationResult.getExplanation();

            System.out.println("Cerco documenti per categoria: " + cat.getCategory() + " basandomi su: \"" + queryText + "\"");

            float[] embedding = llmClient.embed(queryText);

            // 2. Query SQL
            /*String sql = """
                SELECT c.id 
                FROM chunks c
                JOIN documents d ON c.document_id = d.id
                JOIN categories cat ON d.category_id = cat.id
                JOIN chunk_embeddings ce ON ce.chunk_id = c.id
                WHERE LOWER(cat.name) = LOWER(?)
                AND ce.embedding <=> ?::vector <= ?
                ORDER BY ce.embedding <=> ?::vector
                LIMIT ?
            """;*/
            //AND ce.embedding <=> ?::vector <= ?  --> filtro per soglia di similarità
            //Accette solo chunl di distanza <= valore soglia

            String embeddingString;
            try {
                embeddingString = objectMapper.writeValueAsString(embedding);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Errore nella serializzazione dell'embedding", e);
            }

            List<String> chunkIds = chunkRepository.findRelevantChunkIds(
                cat.getCategory(), 
                embeddingString, 
                MAX_COSINE_DISTANCE, 
                kPerCategory
            );
            
            /*jdbcTemplate.query(sql,
                    (ps) -> {
                        ps.setString(1, cat.getCategory()); // WHERE LOWER(cat.name) = LOWER(?)
                        ps.setString(2, embeddingString); // AND ce.embedding <=> ?::vector <= ?
                        ps.setDouble(3, MAX_COSINE_DISTANCE); // soglia di similarità
                        ps.setString(4, embeddingString); // ORDER BY ce.embedding <=> ?::vector
                        ps.setInt(5, kPerCategory); // LIMIT ?
                    },
                    (rs, rowNum) -> rs.getString("id")
            );*/
            // In questo modo per calcolare la distanza del coseno
            // Esculde i chunk che sono oltre la soglia di similarità
            // Ordina i rimanenti per distanza crescente
            // Ne prende al massimo kPerCategory

            if (!chunkIds.isEmpty()) {
                List<DocumentChunk> chunks = chunkRepository.findAllById(chunkIds);

                for (DocumentChunk chunk : chunks) {
//                    Hibernate.initialize(chunk.getDocument());
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