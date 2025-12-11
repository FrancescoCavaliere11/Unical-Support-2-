package unical_support.unicalsupport2.commands;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.validation.annotation.Validated;
import unical_support.unicalsupport2.data.dto.Document.DocumentProcessingResult;
import unical_support.unicalsupport2.security.customAnnotations.annotation.ValidCategoryName;
import unical_support.unicalsupport2.service.interfaces.DocumentService;

@Command(command = "document", alias = "d", description = "Commands for document processing")
@Validated
@RequiredArgsConstructor
public class DocumentCommand {
    private final DocumentService documentService;

    @Command(command = "save", alias = "s", description = "Save a document")
    public String saveDocument(
            @Option(required = true, longNames = "path", shortNames = 'p', description = "Path to the document")
            @NotBlank(message = "Path cannot be null")
            String path,

            @Option(required = true, longNames = "category", shortNames = 'c', description = "Category of the document")
            @NotBlank(message = "Category cannot be null")
            @ValidCategoryName
            String category
    ) {
        DocumentProcessingResult result = documentService.processAndSaveDocumentFromPath(path, category);
        return "Document saved: id=" + result.getDocumentId()
                + ", file=" + result.getOriginalFilename()
                + ", type=" + result.getFileType()
                + ", category=" + result.getCategoryName()
                + ", chunks=" + result.getChunksCount();
    }

    @Command(command = "delete", alias = "del", description = "Delete a document and its vectors by filename")
    public String deleteDocument(
            @Option(required = true, longNames = "name", shortNames = 'n', description = "Exact filename (e.g. Regolamento.pdf)")
            @NotBlank
            String filename
    ) {
        try {
            return documentService.removeDocument(filename);
        } catch (Exception e) {
            return "Errore durante la cancellazione: " + e.getMessage();
        }
    }

    @Command(command = "list", alias = "ls", description = "List all vectorized documents")
    public String listDocuments() {
        return documentService.listDocuments();
    }
}
