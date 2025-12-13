package unical_support.unicalsupport2.service.implementation;

import com.pgvector.PGvector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import unical_support.unicalsupport2.configurations.factory.LlmStrategyFactory;
import unical_support.unicalsupport2.data.dto.Document.DocumentProcessingResult;
import unical_support.unicalsupport2.data.entities.Category;
import unical_support.unicalsupport2.data.entities.ChunkEmbedding;
import unical_support.unicalsupport2.data.entities.Document;
import unical_support.unicalsupport2.data.entities.DocumentChunk;
import unical_support.unicalsupport2.data.enumerators.ModuleName;
import unical_support.unicalsupport2.data.repositories.CategoryRepository;
import unical_support.unicalsupport2.data.repositories.DocumentRepository;
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
    private final TextExtractorService textExtractorService;
    private final CategoryRepository categoryRepository;
    private final LlmStrategyFactory llmStrategyFactory;

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

        List<String> chunksText = getTextFromFile(doc, textExtractorService.extractText(file));

        return new DocumentProcessingResult(doc.getId(), doc.getOriginalFilename(), doc.getFileType(), category.getName(), chunksText.size());
    }

    private List<String> getTextFromFile(Document doc, String text) {
        List<String> chunksText = splitIntoChunksByWords(text, 300, 50);

        List<DocumentChunk> chunks = new ArrayList<>();
        for (int i = 0; i < chunksText.size(); i++) {
            String chText = chunksText.get(i);

            DocumentChunk chunk = new DocumentChunk();
            chunk.setChunkIndex(i);
            chunk.setContent(chText);
            chunk.setDocument(doc);

            LlmClient llmClient = llmStrategyFactory.getLlmClient(ModuleName.EMBEDDER);
            float[] emb = llmClient.embed(chText);

            ChunkEmbedding embedding = new ChunkEmbedding();
            embedding.setEmbedding(new PGvector(emb));
            embedding.setChunk(chunk);

            chunk.setEmbedding(embedding);
            chunks.add(chunk);
        }

        doc.setChunks(chunks);
        documentRepository.save(doc);

        return chunksText;
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


        getTextFromFile(doc, textExtractorService.extractText(multipart));
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }


    @Override
    @Transactional
    public String removeDocument(String fileName) {
        Document doc = documentRepository.findByOriginalFilename(fileName)
                .orElseThrow(() -> new IllegalArgumentException("Documento non trovato con file name: " + fileName));
        String id = doc.getId();
        documentRepository.delete(doc);

        return String.format("Documento '%s' (ID: %s) eliminato. Rimossi i chunk ed i vettori associati.", fileName, id);
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
