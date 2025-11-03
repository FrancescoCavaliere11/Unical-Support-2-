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

    @TempDir
    Path tempDir;

    @Test
    void importValidFile_callsService() throws Exception {
        String json = "[ { \"name\": \"MENSA\", \"description\": \"Prova mensa\" } ]";
        Path file = tempDir.resolve("categories.json");
        Files.writeString(file, json);

        CategoryCommand categoryCommand = new CategoryCommand(categoryService);

        when(categoryService.createCategories(file.toString()))
                .thenReturn("1 categories imported successfully.");

        String result = categoryCommand.importCategories(file.toString());

        verify(categoryService, times(1)).createCategories(file.toString());
        assertEquals("1 categories imported successfully.", result);
    }

    @Test
    void importFile_notFound_returnsError() {
        CategoryCommand categoryCommand = new CategoryCommand(categoryService);

        when(categoryService.createCategories("nonexistent.json"))
                .thenReturn("Error reading JSON file: nonexistent.json (No such file or directory)");

        String result = categoryCommand.importCategories("nonexistent.json");

        verify(categoryService, times(1)).createCategories("nonexistent.json");
        assertTrue(result.startsWith("Error reading JSON file"));
    }
}