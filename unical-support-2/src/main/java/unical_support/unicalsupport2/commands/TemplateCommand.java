package unical_support.unicalsupport2.commands;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.validation.annotation.Validated;
import unical_support.unicalsupport2.service.interfaces.TemplateService;

/**
 * Spring Shell command class for managing email templates.
 * Provides CLI commands for listing, importing, and deleting templates.
 */
@Command(command = "template", alias = "t", description = "Commands for managing templates")
@Validated
@RequiredArgsConstructor
public class TemplateCommand {

    private final TemplateService templateService;

    /**
     * Returns a sample JSON file content illustrating a valid templates array.
     *
     * <p>This content is shown by demonstrate the expected JSON structure for importing templates into the system.</p>
     *
     * @return a multi-line JSON example containing one template object:
     *         "WelcomeTemplate".
     */
    @Command(command = "example", alias = "ex", description = "Show an example of a valid templates JSON file")
    public String showExampleContentFile() {
        return """
            [
              {
                "name": "WelcomeTemplate",
                "category": "ExampleCategory",
                "content": "Hello {{username}}, welcome to our service!",
                "parameters": [
                  { "name": "username", "required": true }
                ]
              },
              ...
            ]
            """;
    }

    /**
     * Lists all templates, optionally filtered by category name.
     * Usage:
     * <pre>
     * template list     *  --category Support
     * </pre>
     */
    @Command(command = "list", alias = "ls", description = "List all templates, optionally filtered by category")
    public String listTemplates(
            @Option(longNames = "category", shortNames = 'c', description = "Filter templates by category name")
            @Nullable
            String categoryName
    ) {
        return templateService.listTemplates(categoryName);
    }

    /**
     * Imports templates from a JSON file.
     * Usage:
     * <pre>
     * template import --file "/path/to/templates.json"
     * </pre>
     */
    @Command(command = "import", alias = "i", description = "Import templates from a JSON file")
    public void importTemplates(
            @Option(longNames = "file", shortNames = 'f', description = "Path to the JSON file containing templates")
            @NotBlank(message = "File path is required")
            String pathFile
    ) {
        templateService.createTemplates(pathFile);
    }

    /**
     * Deletes a template by name.
     * Usage:
     * <pre>
     * template delete --name "WelcomeTemplate"
     * </pre>
     */
    @Command(command = "delete", alias = "d", description = "Delete a template by name")
    public void deleteTemplate(
            @Option(longNames = "name", shortNames = 'n', description = "Specify the template name to delete")
            @NotBlank(message = "Name is required")
            @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
            String name
    ) {
        templateService.deleteTemplateByName(name);
    }
}
