package unical_support.unicalsupport2.commands;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;

@Command(command = "category" , alias = "c", description = "Commands for managing categories")
public class CategoryCommand {
    @Command(command = "generate", alias = "g", description = "Create a new category for the email")
    public String generateCategory(
            @Option(longNames = "name", shortNames = 'n', description = "Specify the name of the category", required = true)
            @NotBlank(message = "name is required")
            @Size(min = 3, max = 50, message = "name must be between 3 and 50 characters")
            String name,

            @Option(longNames = "description", shortNames = 'd', description = "Specify the description of the category", required = true)
            @NotBlank(message = "description is required")
            @Size(min = 10, max = 500, message = "description must be between 10 and 500 characters")
            String description
    ) {
        return "Categoria creata:\n" + name + "\n" + description + "\n";
    }



}
