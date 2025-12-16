package unical_support.unicalsupport2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import unical_support.unicalsupport2.configurations.factory.LlmStrategyFactory;
import unical_support.unicalsupport2.data.entities.Category;
import unical_support.unicalsupport2.data.entities.Document;
import unical_support.unicalsupport2.data.enumerators.ModuleName;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TextExtractorService textExtractorService;

    @Mock
    private LlmClient llmClient;

    @Mock
    private LlmStrategyFactory llmStrategyFactory;

    @InjectMocks
    private DocumentServiceImpl service;

    @BeforeEach
    void setUp() {
        when(llmStrategyFactory.getLlmClient(ModuleName.EMBEDDER)).thenReturn(llmClient);
    }

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
        Mockito.when(llmStrategyFactory.getLlmClient(ModuleName.EMBEDDER).embed(anyString())).thenReturn(new float[]{0.1f, 0.2f});

        service.processAndSaveDocumentFromPath(absolutePath, "test");

        Mockito.verify(textExtractorService).extractText(any(File.class));
        Mockito.verify(llmClient, Mockito.atLeastOnce()).embed(anyString());
        Mockito.verify(documentRepository, Mockito.atLeast(1)).save(any(Document.class));
    }
}
