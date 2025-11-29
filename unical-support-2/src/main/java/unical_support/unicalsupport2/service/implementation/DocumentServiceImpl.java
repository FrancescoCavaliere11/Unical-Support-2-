package unical_support.unicalsupport2.service.implementation;

import com.google.gson.Gson; // Usa Gson per trasformare float[] in stringa JSON
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unical_support.unicalsupport2.data.dto.Document.DocumentProcessingResult;
import unical_support.unicalsupport2.data.entities.Category;
import unical_support.unicalsupport2.data.entities.Document;
import unical_support.unicalsupport2.data.entities.DocumentChunk;
import unical_support.unicalsupport2.data.repositories.CategoryRepository;
import unical_support.unicalsupport2.data.repositories.DocumentChunkRepository;
import unical_support.unicalsupport2.data.repositories.DocumentRepository;
import unical_support.unicalsupport2.service.interfaces.DocumentService;
import unical_support.unicalsupport2.service.interfaces.LlmClient;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository chunkRepository;
    private final TextExtractorService textExtractorService;
    private final LlmClient geminiApiClient;
    private final CategoryRepository categoryRepository;


    private final JdbcTemplate jdbcTemplate;
    private final Gson gson = new Gson();

    @Transactional
    @Override
    public DocumentProcessingResult processAndSaveDocumentFromPath(String filePath, String categoryName) {
        File file = new File(filePath);
        if(!file.exists()) throw new IllegalArgumentException("File non trovato");

        Category category = categoryRepository.findByNameIgnoreCase(categoryName)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        Document doc = new Document();
        doc.setOriginalFilename(file.getName());
        doc.setFileType("pdf");
        doc.setCategory(category);
        doc = documentRepository.save(doc);

        String text = textExtractorService.extractText(file);
        List<String> chunksText = splitIntoChunksByWords(text, 300, 50);

        int index = 0;
        for (String chText : chunksText) {
            // 1. Salva il Chunk (Testo) con Hibernate
            DocumentChunk chunk = new DocumentChunk();
            chunk.setChunkIndex(index++);
            chunk.setContent(chText);
            chunk.setDocument(doc);


            chunk = chunkRepository.saveAndFlush(chunk);


            // 2. Genera Embedding
            float[] emb = geminiApiClient.embed(chText);

            // 3. Salva Embedding con SQL PURO
            String embeddingJson = gson.toJson(emb);
            String sql = "INSERT INTO chunk_embeddings (id, chunk_id, embedding) VALUES (?, ?, ?::vector)";

            jdbcTemplate.update(sql, UUID.randomUUID().toString(), chunk.getId(), embeddingJson);
        }

        return new DocumentProcessingResult(doc.getId(), doc.getOriginalFilename(), doc.getFileType(), category.getName(), chunksText.size());
    }


    private List<String> splitIntoChunksByWords(String text, int maxWords, int overlapWords) {
        String[] words = text.split("\\s+");
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < words.length) {
            int end = Math.min(start + maxWords, words.length);
            chunks.add(String.join(" ", Arrays.copyOfRange(words, start, end)));
            start += (maxWords - overlapWords);
            if (maxWords <= overlapWords) break;
        }
        return chunks;
    }

    @Override
    public DocumentProcessingResult processAndSaveDocumentFromMultipart(File file, Category category) { return null; }
}