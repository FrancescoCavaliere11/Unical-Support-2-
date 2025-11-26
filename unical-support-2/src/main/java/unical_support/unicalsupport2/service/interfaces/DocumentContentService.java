package unical_support.unicalsupport2.service.interfaces;

import java.io.File;

public interface DocumentContentService {
    String extractText(File file);
    String readFromPath(String path);
}

