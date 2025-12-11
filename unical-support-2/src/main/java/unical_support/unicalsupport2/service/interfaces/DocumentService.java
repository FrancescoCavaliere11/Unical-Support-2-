package unical_support.unicalsupport2.service.interfaces;

import org.springframework.web.multipart.MultipartFile;
import unical_support.unicalsupport2.data.dto.Document.DocumentProcessingResult;

public interface DocumentService {
    DocumentProcessingResult processAndSaveDocumentFromPath(String filePath, String categoryName);
    void processAndSaveDocumentFromMultipart(MultipartFile multipart, String categoryId);
    String removeDocument(String fileName);

    String listDocuments();
}
