package unical_support.unicalsupport2.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.validation.annotation.Validated;
import unical_support.unicalsupport2.data.dto.Document.DocumentProcessingResult;
import unical_support.unicalsupport2.service.interfaces.DocumentService;

@Command(command = "document", alias = "d", description = "Commands for document processing")
@Validated
@RequiredArgsConstructor
public class DocumentCommand {
    private final DocumentService documentService;

    @Command(command = "save", alias = "s", description = "Save a document")
    public String saveDocument(
            @Option(longNames = "path", shortNames = 'p', description = "Path to the document")
            String path,

            @Option(longNames = "category", shortNames = 'c', description = "Category of the document")
            String category
    ) {
        try {
            DocumentProcessingResult result = documentService.processAndSaveDocumentFromPath(path, category);
            return "Document saved: id=" + result.getDocumentId()
                    + ", file=" + result.getOriginalFilename()
                    + ", type=" + result.getFileType()
                    + ", category=" + result.getCategoryName()
                    + ", chunks=" + result.getChunksCount();
        } catch (IllegalArgumentException | UnsupportedOperationException e) {
            return "Error: " + e.getMessage();
        } catch (Exception e) {
            return "Error while processing document: " + e.getMessage();
        }
    }
}
