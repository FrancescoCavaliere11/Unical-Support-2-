package unical_support.unicalsupport2.service.implementation;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
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

        for (int i = 0; i < chunksText.size(); i++) {
            String chText = chunksText.get(i);
            // 1. Salva il Chunk (Testo) con Hibernate
            DocumentChunk chunk = new DocumentChunk();
            chunk.setChunkIndex(i);
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
    public void processAndSaveDocumentFromMultipart(MultipartFile multipart, String categoryId) {
        if (multipart == null || multipart.isEmpty()) {
            throw new IllegalArgumentException("Multipart file non valido");
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        Document doc = new Document();
        doc.setOriginalFilename(multipart.getOriginalFilename());
        doc.setFileType(extractExtension(multipart.getOriginalFilename()));
        doc.setCategory(category);

        documentRepository.save(doc);

        String text = textExtractorService.extractText(multipart);
        List<String> chunksText = splitIntoChunksByWords(text, 300, 50);

        for (int i = 0; i < chunksText.size(); i++) {
            String chText = chunksText.get(i);
            // 1. Salva il Chunk (Testo) con Hibernate
            DocumentChunk chunk = new DocumentChunk();
            chunk.setChunkIndex(i);
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
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }


    @Override
    @Transactional
    public String removeDocument(String id) {
        // 1. Trova il documento per ID
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Documento non trovato con ID: " + id));

        int chunksCount = doc.getChunks() != null ? doc.getChunks().size() : 0;
        String filename = doc.getOriginalFilename();


        String sqlDeleteEmbeddings = "DELETE FROM chunk_embeddings WHERE chunk_id IN (SELECT id FROM chunks WHERE document_id = ?)";
        int vectorsDeleted = jdbcTemplate.update(sqlDeleteEmbeddings, id);


        documentRepository.delete(doc);

        return String.format("Documento '%s' (ID: %s) eliminato. Rimossi %d chunk e %d vettori.",
                filename, id, chunksCount, vectorsDeleted);
    }

    @Override
    @Transactional(readOnly = true) // Necessario per leggere la size() dei chunk lazy
    public String listDocuments() {
        List<Document> documents = documentRepository.findAll();

        if (documents.isEmpty()) {
            return "Nessun documento presente nel database.";
        }

        StringBuilder sb = new StringBuilder();
        String header = String.format("| %-36s | %-40s | %-25s | %-6s |%n",
                "ID", "Nome File", "Categoria", "Chunk");
        String separator = "+--------------------------------------+------------------------------------------+---------------------------+--------+%n";

        sb.append("\nElenco Documenti Vettorializzati:\n");
        sb.append(separator);
        sb.append(header);
        sb.append(separator);

        for (Document doc : documents) {
            String catName = (doc.getCategory() != null) ? doc.getCategory().getName() : "N/A";
            int chunkCount = (doc.getChunks() != null) ? doc.getChunks().size() : 0;

            // Tronca il nome file se troppo lungo per la tabella
            String filename = doc.getOriginalFilename();
            if (filename.length() > 37) {
                filename = filename.substring(0, 34) + "...";
            }

            sb.append(String.format("| %-36s | %-40s | %-25s | %-6d |%n",
                    doc.getId(), filename, catName, chunkCount));
        }
        sb.append(separator);
        sb.append("Totale documenti: ").append(documents.size()).append("\n");

        return sb.toString();
    }
}
