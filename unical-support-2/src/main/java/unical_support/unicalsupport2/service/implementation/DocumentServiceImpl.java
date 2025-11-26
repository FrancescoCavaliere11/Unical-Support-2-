package unical_support.unicalsupport2.service.implementation;

import com.pgvector.PGvector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unical_support.unicalsupport2.data.dto.Document.DocumentProcessingResult;
import unical_support.unicalsupport2.data.entities.Category;
import unical_support.unicalsupport2.data.entities.ChunkEmbedding;
import unical_support.unicalsupport2.data.entities.Document;
import unical_support.unicalsupport2.data.entities.DocumentChunk;
import unical_support.unicalsupport2.data.repositories.CategoryRepository;
import unical_support.unicalsupport2.data.repositories.DocumentChunkRepository;
import unical_support.unicalsupport2.data.repositories.DocumentRepository;
import unical_support.unicalsupport2.service.interfaces.DocumentContentService;
import unical_support.unicalsupport2.service.interfaces.DocumentService;
import unical_support.unicalsupport2.service.interfaces.LlmClient;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository chunkRepository;
    private final DocumentContentService documentContentService;
    private final LlmClient geminiApiClient;
    private final CategoryRepository categoryRepository;

    @Transactional
    @Override
    public DocumentProcessingResult processAndSaveDocumentFromPath(String filePath, String categoryName) {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("File path is required");
        }
        if (categoryName == null || categoryName.isBlank()) {
            throw new IllegalArgumentException("Category name is required");
        }

        File file = new File(filePath);
        Category category = categoryRepository.findByNameIgnoreCase(categoryName)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryName));

        return processAndSaveDocument(file, category);
    }

    @Transactional
    @Override
    public DocumentProcessingResult processAndSaveDocumentFromMultipart(File file, Category category) {
        throw new UnsupportedOperationException("Multipart document processing not implemented yet");
    }

    private DocumentProcessingResult processAndSaveDocument(File file, Category category) {
        Document doc = new Document();
        doc.setOriginalFilename(file.getName());
        doc.setFileType(getExtension(file.getName()));
        doc.setCategory(category);
        doc = documentRepository.save(doc);

        String text = documentContentService.extractText(file);

        if (text == null || text.isBlank()) {
            throw new IllegalStateException("Extracted text is empty for file: " + file.getName());
        }

        List<String> chunks = splitIntoChunksByWords(text, 300, 50);

        int index = 0;
        int chunksCount = 0;

        for (String ch : chunks) {
            if (ch == null || ch.isBlank()) {
                continue;
            }

            DocumentChunk chunk = new DocumentChunk();
            chunk.setChunkIndex(index++);
            chunk.setContent(ch);
            chunk.setDocument(doc);

            chunk = chunkRepository.save(chunk);

            float[] emb = geminiApiClient.embed(ch);

            ChunkEmbedding embedding = new ChunkEmbedding();
            embedding.setChunk(chunk);
            embedding.setEmbedding(new PGvector(emb));

            chunk.setEmbedding(embedding);
            chunkRepository.save(chunk);

            chunksCount++;
        }

        documentRepository.save(doc);

        return new DocumentProcessingResult(
                doc.getId(),
                doc.getOriginalFilename(),
                doc.getFileType(),
                category.getName(),
                chunksCount
        );
    }

    private String getExtension(String filename) {
        int idx = filename.lastIndexOf('.');
        if (idx == -1 || idx == filename.length() - 1) {
            return "";
        }
        return filename.substring(idx + 1).toLowerCase();
    }

    private List<String> splitIntoChunksByWords(String text, int maxWords, int overlapWords) {
        String[] words = text.split("\\s+");
        List<String> chunks = new ArrayList<>();

        int start = 0;

        while (start < words.length) {
            int end = Math.min(start + maxWords, words.length);

            String chunk = String.join(" ", Arrays.copyOfRange(words, start, end));
            chunks.add(chunk);

            start += (maxWords - overlapWords);
            if (maxWords <= overlapWords) {
                break;
            }
        }

        return chunks;
    }
}
