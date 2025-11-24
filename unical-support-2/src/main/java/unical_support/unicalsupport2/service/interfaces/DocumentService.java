package unical_support.unicalsupport2.service.interfaces;

import unical_support.unicalsupport2.data.entities.Category;

import java.io.File;

public interface DocumentService {
    void processAndSaveDocumentFromPath(String filePath, String categoryName);
    void processAndSaveDocumentFromMultipart(File file, Category category); // todo
}
