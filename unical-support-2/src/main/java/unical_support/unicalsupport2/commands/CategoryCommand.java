package unical_support.unicalsupport2.commands;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.validation.annotation.Validated;
import unical_support.unicalsupport2.security.customAnnotations.annotation.UniqueCategoryName;
import unical_support.unicalsupport2.service.interfaces.CategoryService;

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



}
