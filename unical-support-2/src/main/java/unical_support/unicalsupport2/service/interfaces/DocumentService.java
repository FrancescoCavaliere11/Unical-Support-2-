package unical_support.unicalsupport2.service.interfaces;

import unical_support.unicalsupport2.data.dto.Document.DocumentProcessingResult;
import unical_support.unicalsupport2.data.entities.Category;

import java.io.File;

public interface DocumentService {
    DocumentProcessingResult processAndSaveDocumentFromPath(String filePath, String categoryName);
    DocumentProcessingResult processAndSaveDocumentFromMultipart(File file, Category category); // todo
    String removeDocument(String id);

    String listDocuments();
}
