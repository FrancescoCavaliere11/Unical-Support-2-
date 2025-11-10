package unical_support.unicalsupport2.service.interfaces;

import java.util.Map;

public interface TemplateService {
    String renderTemplate(String templateName, Map<String, String> parameters);
    String listTemplates(String categoryName);
    void createTemplates(String pathFile);
    void deleteTemplate(String name);
}
