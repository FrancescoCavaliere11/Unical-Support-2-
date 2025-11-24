package unical_support.unicalsupport2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import unical_support.unicalsupport2.data.entities.Category;
import unical_support.unicalsupport2.data.entities.Document;
import unical_support.unicalsupport2.data.repositories.CategoryRepository;
import unical_support.unicalsupport2.data.repositories.DocumentChunkRepository;
import unical_support.unicalsupport2.data.repositories.DocumentRepository;
import unical_support.unicalsupport2.service.implementation.DocumentServiceImpl;
import unical_support.unicalsupport2.service.implementation.TextExtractorService;
import unical_support.unicalsupport2.service.interfaces.LlmClient;

import java.io.File;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentChunkRepository chunkRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TextExtractorService extractor;

    @Mock
    private LlmClient gemini;

    @InjectMocks
    private DocumentServiceImpl service;

    @Test
    void testProcessAndSaveDocumentFromPath() {
        File file = new File("test.txt");
        Category category = new Category();
        category.setName("test");

        Document savedDoc = new Document();
        savedDoc.setId("123");

        Mockito.when(documentRepository.save(any())).thenReturn(savedDoc);
        Mockito.when(extractor.extractText(any())).thenReturn("uno due tre quattro cinque sei");
        Mockito.when(chunkRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        Mockito.when(gemini.embed(any())).thenReturn(new float[]{0.1f, 0.2f});
        Mockito.when(categoryRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.of(category));

        service.processAndSaveDocumentFromPath("test.txt", "test");

        Mockito.verify(extractor).extractText(file);
        Mockito.verify(gemini, Mockito.atLeastOnce()).embed(anyString());
        Mockito.verify(chunkRepository, Mockito.atLeastOnce()).save(any());
    }
}

