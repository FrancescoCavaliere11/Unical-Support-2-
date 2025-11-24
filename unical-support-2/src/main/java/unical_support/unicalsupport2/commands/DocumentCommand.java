package unical_support.unicalsupport2.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.validation.annotation.Validated;
import unical_support.unicalsupport2.service.interfaces.DocumentService;

@Command(command = "document", alias = "d", description = "Commands for document processing")
@Validated
@RequiredArgsConstructor
public class DocumentCommand {
    private final DocumentService documentService;

    @Command(command = "save", alias = "s", description = "Save a document")
    public void saveDocument(
            @Option(longNames = "path", shortNames = 'p', description = "Path to the document")
            String path,

            @Option(longNames = "category", shortNames = 'c', description = "Category of the document")
            // todo controllare se è una categoria valida
            String category
    ) {
        documentService.processAndSaveDocumentFromPath(path, category);
    }
}
