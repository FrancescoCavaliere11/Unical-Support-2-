package unical_support.unicalsupport2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import unical_support.unicalsupport2.data.dto.classifier.ClassificationResultDto;
import unical_support.unicalsupport2.data.dto.classifier.SingleCategoryDto;
import unical_support.unicalsupport2.data.entities.Document;
import unical_support.unicalsupport2.data.entities.DocumentChunk;
import unical_support.unicalsupport2.data.repositories.DocumentChunkRepository;
import unical_support.unicalsupport2.service.implementation.DocumentChunkServiceImpl;
import unical_support.unicalsupport2.service.implementation.GeminiApiClientImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RagTest {

    @Mock
    private DocumentChunkRepository chunkRepository;

    @Mock
    private GeminiApiClientImpl llmClient;

    @InjectMocks
    private DocumentChunkServiceImpl ragService;

    @Test
    void testFindRelevantChunks_StandardFlow() {

        String categoryName = "Tasse ed esoneri";
        String userQuery = "Quanto pago?";
        float[] mockEmbedding = new float[]{0.1f, 0.2f, 0.3f};
        String mockChunkId = "chunk-uuid-123";

        // Simulo l'input del classificatore
        ClassificationResultDto input = new ClassificationResultDto(
                List.of(new SingleCategoryDto(categoryName, 0.95, userQuery)),
                "Spiegazione",
                0
        );

        // Simulo l'embedding
        when(llmClient.embed(userQuery)).thenReturn(mockEmbedding);

        when(chunkRepository.findRelevantChunkIds(
                eq(categoryName),
                anyString(),
                anyDouble(),
                eq(5)
        )).thenReturn(List.of(mockChunkId));

        // 3. Simulo il recupero dell'entità completa
        DocumentChunk chunk = new DocumentChunk();
        chunk.setId(mockChunkId);
        chunk.setContent("Testo del regolamento tasse...");

        Document doc = new Document();
        doc.setOriginalFilename("Regolamento.pdf");
        chunk.setDocument(doc);

        when(chunkRepository.findAllById(List.of(mockChunkId))).thenReturn(List.of(chunk));

        List<DocumentChunk> results = ragService.findRelevantChunks(input, 5);

        assertFalse(results.isEmpty(), "Dovrebbe trovare almeno un chunk.");
        assertEquals(1, results.size());
        assertEquals(mockChunkId, results.getFirst().getId());
        assertEquals("Regolamento.pdf", results.getFirst().getDocument().getOriginalFilename());

        verify(llmClient).embed(userQuery);
        verify(chunkRepository).findRelevantChunkIds(eq(categoryName), anyString(), anyDouble(), eq(5));
    }

    @Test
    void testFindRelevantChunks_IgnoraCategoriaNonRiconosciuta() {
        ClassificationResultDto input = new ClassificationResultDto(
                List.of(new SingleCategoryDto("NON RICONOSCIUTA", 0.99, "bo?")),
                "Boh",
                0
        );

        List<DocumentChunk> results = ragService.findRelevantChunks(input, 5);

        assertTrue(results.isEmpty(), "Non deve cercare chunk per categorie non riconosciute");

        verify(llmClient, never()).embed(anyString());
        verify(chunkRepository, never()).findRelevantChunkIds(any(), any(), anyDouble(), anyInt());
    }
}