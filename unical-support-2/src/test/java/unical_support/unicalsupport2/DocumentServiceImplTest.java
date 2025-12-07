package unical_support.unicalsupport2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import unical_support.unicalsupport2.data.entities.Category;
import unical_support.unicalsupport2.data.entities.Document;
import unical_support.unicalsupport2.data.repositories.CategoryRepository;
import unical_support.unicalsupport2.data.repositories.DocumentRepository;
import unical_support.unicalsupport2.service.implementation.DocumentServiceImpl;
import unical_support.unicalsupport2.service.implementation.TextExtractorService;
import unical_support.unicalsupport2.service.interfaces.LlmClient;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TextExtractorService textExtractorService;

    @Mock
    private LlmClient geminiApiClient;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private DocumentServiceImpl service;

    @Test
    void testProcessAndSaveDocumentFromPath(@TempDir Path tempDir) throws Exception {
        Path tempFile = tempDir.resolve("test.pdf");
        Files.createFile(tempFile);
        String absolutePath = tempFile.toAbsolutePath().toString();

        Category category = new Category();
        category.setName("test");

        Document savedDoc = new Document();
        savedDoc.setId("123");
        savedDoc.setOriginalFilename("test.pdf");

        Mockito.when(categoryRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.of(category));
        Mockito.when(documentRepository.save(any())).thenReturn(savedDoc);
        Mockito.when(textExtractorService.extractText((File) any())).thenReturn("uno due tre quattro cinque sei");
        Mockito.when(geminiApiClient.embed(anyString())).thenReturn(new float[]{0.1f, 0.2f});

        service.processAndSaveDocumentFromPath(absolutePath, "test");

        Mockito.verify(textExtractorService).extractText(any(File.class));
        Mockito.verify(geminiApiClient, Mockito.atLeastOnce()).embed(anyString());
        Mockito.verify(documentRepository, Mockito.atLeast(2)).save(any(Document.class));
    }
}
