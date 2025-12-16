package unical_support.unicalsupport2.service.interfaces;

import org.springframework.web.multipart.MultipartFile;
import unical_support.unicalsupport2.data.dto.document.DocumentCreateDto;
import unical_support.unicalsupport2.data.dto.document.DocumentDto;
import unical_support.unicalsupport2.data.dto.document.DocumentProcessingResult;

import java.util.List;

public interface DocumentService {
    DocumentProcessingResult processAndSaveDocumentFromPath(String filePath, String categoryName);
    DocumentDto processAndSaveDocumentFromMultipart(MultipartFile multipart, DocumentCreateDto documentCreateDto);
    String removeDocument(String fileName);
    String listDocuments();
    List<DocumentDto> getAll();
    void removeById(String id);
}
