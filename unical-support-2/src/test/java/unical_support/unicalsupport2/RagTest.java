package unical_support.unicalsupport2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import unical_support.unicalsupport2.data.dto.classifier.ClassificationResultDto;
import unical_support.unicalsupport2.data.dto.classifier.SingleCategoryDto;
import unical_support.unicalsupport2.data.entities.Document;
import unical_support.unicalsupport2.data.entities.DocumentChunk;
import unical_support.unicalsupport2.data.repositories.DocumentChunkRepository;
import unical_support.unicalsupport2.service.implementation.DocumentChunkServiceImpl;
import unical_support.unicalsupport2.service.interfaces.LlmClient;


import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RagTest {

    @Mock
    private DocumentChunkRepository chunkRepository;

    @Mock
    private LlmClient llmClient;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private DocumentChunkServiceImpl ragService;

    @Test
    void testFindRelevantChunks_StandardFlow() {

        String categoryName = "Tasse ed esoneri";
        String userQuery = "Quanto pago?";
        float[] mockEmbedding = new float[]{0.1f, 0.2f, 0.3f};
        String mockChunkId = "chunk-uuid-123";

        // Simuliamo l'input del classificatore
        ClassificationResultDto input = new ClassificationResultDto(
                List.of(new SingleCategoryDto(categoryName, 0.95, userQuery)),
                "Spiegazione",
                0
        );

        // Simuliamo l'embedding generato da Gemini/
        when(llmClient.embed(userQuery)).thenReturn(mockEmbedding);

        // Simuliamo la query JDBC che ritorna gli ID dei chunk trovati
        // Mockare jdbcTemplate.query con lambda è complesso, qui usiamo any() per semplicità
        when(jdbcTemplate.query(
                anyString(),
                any(PreparedStatementSetter.class),
                any(RowMapper.class))
        ).thenReturn(List.of(mockChunkId));

        // Simuliamo il recupero dell'entità completa dal repository
        DocumentChunk chunk = new DocumentChunk();
        chunk.setId(mockChunkId);
        chunk.setContent("Testo del regolamento tasse...");

        // Creiamo un documento finto per evitare NullPointer su Hibernate.initialize
        Document doc = new Document();
        doc.setOriginalFilename("Regolamento.pdf");
        chunk.setDocument(doc);

        when(chunkRepository.findAllById(List.of(mockChunkId))).thenReturn(List.of(chunk));

        // ACT
        List<DocumentChunk> results = ragService.findRelevantChunks(input, 5);

        // ASSERT
        assertFalse(results.isEmpty(), "Dovrebbe trovare almeno un chunk.");
        assertEquals(1, results.size());
        assertEquals(mockChunkId, results.getFirst().getId());
        assertEquals("Regolamento.pdf", results.getFirst().getDocument().getOriginalFilename());

        // Verifiche chiamate
        verify(llmClient).embed(userQuery);
        verify(jdbcTemplate).query(anyString(), any(PreparedStatementSetter.class), any(RowMapper.class));
    }

    @Test
    void testFindRelevantChunks_IgnoraCategoriaNonRiconosciuta() {
        // ARRANGE
        ClassificationResultDto input = new ClassificationResultDto(
                List.of(new SingleCategoryDto("NON_RICONOSCIUTA", 0.99, "bo?")),
                "Boh",
                0
        );


        List<DocumentChunk> results = ragService.findRelevantChunks(input, 5);


        assertTrue(results.isEmpty(), "Non deve cercare chunk per categorie non riconosciute");


        verify(llmClient, never()).embed(anyString());
        verifyNoInteractions(jdbcTemplate);
    }
}