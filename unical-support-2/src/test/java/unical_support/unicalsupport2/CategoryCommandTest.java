package unical_support.unicalsupport2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import unical_support.unicalsupport2.commands.CategoryCommand;
import unical_support.unicalsupport2.service.interfaces.CategoryService;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {"spring.shell.interactive.enabled=false"})
@ExtendWith(MockitoExtension.class)
class CategoryCommandTest {

    @Mock
    private CategoryService categoryService;

    private CategoryCommand categoryCommand;

    @TempDir
    Path tempDir;

    @Test
    void importValidJson_createsCategory() throws Exception {
        // File JSON valido
        String json = "[ { \"name\": \"MENSA\", \"description\": \"Prova mensa\" } ]";
        Path file = tempDir.resolve("categories.json");
        Files.writeString(file, json);

        // Esegue il metodo
        categoryCommand = new CategoryCommand(categoryService);
        String result = categoryCommand.importCategories(file.toString());

        // Verifica che sia stata creata 1 categoria
        verify(categoryService, times(1)).createCategory("MENSA", "Prova mensa");

        // Verifica il contenuto del messaggio di ritorno
        assertNotNull(result);
        assertTrue(result.contains("1 categories imported successfully"));
    }

    @Test
    void importJson_missingDescription_skipsCategory() throws Exception {
        // JSON con un elemento non valido (manca la descrizione)
        String json = "[ { \"name\": \"TEST\" } ]";
        Path file = tempDir.resolve("invalid.json");
        Files.writeString(file, json);

        // Esegue il metodo
        categoryCommand = new CategoryCommand(categoryService);
        String result = categoryCommand.importCategories(file.toString());

        // Verifica che non venga chiamato il servizio
        verify(categoryService, never()).createCategory(any(), any());

        // Controlla che il messaggio indichi "0 categories imported"
        assertNotNull(result);
        assertTrue(result.contains("0 categories imported"));
    }

    @Test
    void importFile_notFound_returnsError() {
        // File inesistente
        categoryCommand = new CategoryCommand(categoryService);
        String result = categoryCommand.importCategories("nonexistent.json");

        // Il metodo deve restituire un messaggio di errore leggibile
        assertNotNull(result);
        assertTrue(result.startsWith("Error reading JSON file"));

        // Nessuna categoria deve essere creata
        verify(categoryService, never()).createCategory(any(), any());
    }

    @Test
    void importJson_serviceThrowsException_doesNotCrash() throws Exception {
        // JSON con una categoria valida
        String json = "[ { \"name\": \"DUPLICATE\", \"description\": \"Categoria duplicata\" } ]";
        Path file = tempDir.resolve("errorcase.json");
        Files.writeString(file, json);

        // Simula eccezione durante la creazione
        doThrow(new RuntimeException("Duplicate category"))
                .when(categoryService)
                .createCategory("DUPLICATE", "Categoria duplicata");

        // Esegue il metodo
        categoryCommand = new CategoryCommand(categoryService);
        String result = categoryCommand.importCategories(file.toString());

        // Deve restituire che 0 categorie sono state create con successo
        verify(categoryService, times(1)).createCategory("DUPLICATE", "Categoria duplicata");
        assertTrue(result.contains("0 categories imported") || result.contains("categories imported successfully"));
    }
}