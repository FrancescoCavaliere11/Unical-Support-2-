package unical_support.unicalsupport2.service.interfaces;

import unical_support.unicalsupport2.data.dto.template.TemplateCreateDto;
import unical_support.unicalsupport2.data.dto.template.TemplateDto;
import unical_support.unicalsupport2.data.dto.template.TemplateUpdateDto;

import java.util.List;
import java.util.Map;

public interface TemplateService {
    //Metodi per il controller Rest
    List<TemplateDto> getAll(String categoryId);
    TemplateDto createTemplate(TemplateCreateDto templateCreateDto);
    void updateTemplate(TemplateUpdateDto templateUpdateDto);
    void deleteTemplateById(String id);

    //Metodi per il command shell
    String renderTemplate(String templateName, Map<String, String> parameters);
    String listTemplates(String categoryName);
    void createTemplates(String pathFile);
    void deleteTemplateByName(String name);
}
