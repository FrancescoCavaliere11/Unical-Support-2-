package unical_support.unicalsupport2.commands;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.validation.annotation.Validated;
import unical_support.unicalsupport2.security.customAnnotations.annotation.UniqueCategoryName;
import unical_support.unicalsupport2.service.interfaces.CategoryService;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Command(command = "category" , alias = "c", description = "Commands for managing categories")
@Validated
@RequiredArgsConstructor
public class CategoryCommand {
    private final CategoryService categoryService;

    @Command(command = "generate", alias = "g", description = "Create a new category for the email")
    public String generateCategory(
            @Option(longNames = "name", shortNames = 'n', description = "Specify the name of the category")
            @NotBlank(message = "name is required")
            @Size(min = 3, max = 50, message = "name must be between 3 and 50 characters")
            @UniqueCategoryName
            String name,

            @Option(longNames = "description", shortNames = 'd', description = "Specify the description of the category")
            @NotBlank(message = "description is required")
            @Size(min = 10, max = 500, message = "description must be between 10 and 500 characters")
            String description
    ) {
        categoryService.createCategory(name, description);
        return "Category created successfully";
    }

    // Metodo per importare categorie da un file JSON
    // Esempio d'uso: shell:> category import --file src/main/resources/dataset.json
    @Command(command = "import", alias = "i", description = "Import categories from a JSON file")
    public String importCategories(
            @Option(longNames = "file", shortNames = 'f', description = "Path to the JSON file containing categories")
            @NotBlank(message = "file path is required")
            String pathFile
    ) {
        try {

            // Creo un'istanza di ObjectMapper per leggere/scrivere da un file JSON
            ObjectMapper mapper = new ObjectMapper();

            // Leggo il contenuto del file JSON e lo converto in una lista di mappe
            List<Map<String, String>> categories = mapper.readValue(
                    new File(pathFile),
                    new TypeReference<List<Map<String, String>>>() {}
            );

            // Stampa un messaggio di conferma del corretto parsing
            System.out.println("File letto correttamente. Trovate " + categories.size() + " categorie.");

            int count = 0;

            // Scorre ogni categoria del file JSON
            for (Map<String, String> cat : categories) {

                // Estrae i campi "name" e "description"
                String name = cat.get("name");
                String description = cat.get("description");

                // Se uno dei due campi è mancante, salta l'elemento
                if (name == null || description == null) {
                    System.out.println("Invalid category: " + cat);
                    continue;
                }

                try {
                    // Chiama il servizio CategoryService per creare una nuova categoria nel database
                    categoryService.createCategory(name, description);
                    count++;
                } catch (Exception e) {
                    System.out.println("Could not create category '" + name + "': " + e.getMessage());
                }
            }
            return count + " categories imported successfully.";
        } catch (IOException e) {
            return "Error reading JSON file: " + e.getMessage();
        }
    }




}