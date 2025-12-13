package unical_support.unicalsupport2.service.implementation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unical_support.unicalsupport2.configurations.factory.LlmStrategyFactory;
import unical_support.unicalsupport2.data.dto.classifier.ClassificationResultDto;
import unical_support.unicalsupport2.data.dto.classifier.SingleCategoryDto;
import unical_support.unicalsupport2.data.entities.DocumentChunk;
import unical_support.unicalsupport2.data.enumerators.ModuleName;
import unical_support.unicalsupport2.data.repositories.DocumentChunkRepository;
import unical_support.unicalsupport2.service.interfaces.DocumentChunkService;
import unical_support.unicalsupport2.service.interfaces.LlmClient;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentChunkServiceImpl implements DocumentChunkService {

    private static final double MAX_COSINE_DISTANCE = 0.6;
    private final DocumentChunkRepository chunkRepository;
    private final LlmStrategyFactory llmStrategyFactory;
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

            LlmClient llmClient = llmStrategyFactory.getLlmClient(ModuleName.EMBEDDER);
            float[] embedding = llmClient.embed(queryText);

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

            if (!chunkIds.isEmpty()) {
                List<DocumentChunk> chunks = chunkRepository.findAllById(chunkIds);

                for (DocumentChunk chunk : chunks) {
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