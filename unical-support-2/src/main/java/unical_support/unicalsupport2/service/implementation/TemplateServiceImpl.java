package unical_support.unicalsupport2.service.implementation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import unical_support.unicalsupport2.data.embeddables.TemplateParameter;
import unical_support.unicalsupport2.data.entities.Category;
import unical_support.unicalsupport2.data.entities.Template;
import unical_support.unicalsupport2.data.repositories.CategoryRepository;
import unical_support.unicalsupport2.data.repositories.TemplateRepository;
import unical_support.unicalsupport2.service.interfaces.TemplateService;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service implementation that manages email templates and their dynamic parameters.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Loading and validating templates from JSON files</li>
 *   <li>Rendering templates by replacing parameter placeholders</li>
 *   <li>Ensuring templates and categories consistency</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateServiceImpl implements TemplateService {

    private final TemplateRepository templateRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Renders a given template by replacing placeholders with provided values.
     *
     * @param templateName the template name (case-insensitive)
     * @param parameters   map of parameter names and values
     * @return the rendered template content
     * @throws IllegalArgumentException if the template does not exist or required parameters are missing
     */
    @Override
    public String renderTemplate(String templateName, Map<String, String> parameters) {
        Template template = templateRepository.findByNameIgnoreCase(templateName)
                .orElseThrow(() -> new IllegalArgumentException("Template not found: " + templateName));

        List<TemplateParameter> requiredParams = template.getParameters();
        for (TemplateParameter param : requiredParams) {
            if (param.isRequired() && !parameters.containsKey(param.getName())) {
                throw new IllegalArgumentException("Missing required parameter: " + param.getName());
            }
        }

        String renderedTemplate = template.getContent();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            renderedTemplate = renderedTemplate.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }

        return renderedTemplate;
    }

    /**
     * Lists all templates, optionally filtered by category name.
     *
     * @param categoryName optional category name to filter templates
     * @return formatted string containing all found templates
     */
    @Override
    public String listTemplates(String categoryName) {
        List<Template> templates = categoryName != null ?
                templateRepository.findByCategoryNameIgnoreCase(categoryName) :
                templateRepository.findAll();

        if (templates.isEmpty()) {
            log.info("No templates found in the repository{}.",
                    categoryName != null ? " by category '" + categoryName + "'" : "");
        } else {
            log.info("Listing all templates{}.",
                    categoryName != null ? " filtered by category '" + categoryName + "'" : "");
        }

        StringBuilder sb = new StringBuilder();
        for (Template template : templates) {
            sb.append(template);
        }
        return sb.toString();
    }

    /**
     * Creates templates from a JSON file. Validates template data before persisting.
     *
     * @param pathFile absolute path to the JSON file
     */
    @Override
    public void createTemplates(String pathFile) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            List<Map<String, Object>> templatesFromJson = mapper.readValue(
                    new File(pathFile),
                    new TypeReference<>() {}
            );

            log.info("Found {} templates in JSON file.", templatesFromJson.size());

            List<Template> validTemplates = new ArrayList<>();

            for (Map<String, Object> t : templatesFromJson) {
                try {
                    String name = (String) t.get("name");
                    String content = (String) t.get("content");
                    String categoryName = (String) t.get("category");

                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> paramsList = (List<Map<String, Object>>) t.get("parameters");

                    if (!validateTemplateName(name)) continue;
                    Category category = validateCategoryExists(categoryName);
                    if (category == null) continue;
                    if (!validateContent(content)) continue;

                    List<TemplateParameter> parameters = validateAndBuildParameters(paramsList);
                    if (parameters == null) continue;

                    if (!validateParameterOccurrences(content, parameters)) continue;

                    Template template = new Template();
                    template.setName(name);
                    template.setContent(content);
                    template.setCategory(category);
                    template.setParameters(parameters);

                    validTemplates.add(template);

                } catch (Exception e) {
                    log.warn("Error parsing a template: {}", e.getMessage());
                }
            }

            if (validTemplates.isEmpty()) {
                log.warn("No valid templates found in JSON file.");
            }

            templateRepository.saveAll(validTemplates);
            log.info("{} templates successfully created.", validTemplates.size());

        } catch (IOException e) {
            log.error("Error reading JSON file: {}", e.getMessage());
        }
    }

    /**
     * Deletes a template by its name.
     *
     * @param name template name (case-insensitive)
     */
    @Override
    public void deleteTemplate(String name) {
        templateRepository.findByNameIgnoreCase(name)
                .ifPresentOrElse(template -> {
                        templateRepository.delete(template);
                        log.info("Template '{}' deleted successfully.", name);
                    }, () -> log.warn("Template '{}' not found.", name)
                );
    }



    /**
     * Validates that the template name is not empty and not already existing.
     */
    private boolean validateTemplateName(String name) {
        if (name == null || name.isBlank()) {
            log.warn("Template name is missing or blank. Skipped.");
            return false;
        }
        if (templateRepository.existsByNameIgnoreCase(name)) {
            log.warn("Template with name '{}' already exists. Skipped.", name);
            return false;
        }
        return true;
    }

    /**
     * Validates that the given category exists in the database.
     */
    private Category validateCategoryExists(String categoryName) {
        if (categoryName == null || categoryName.isBlank()) {
            log.warn("Category name is missing or blank. Skipped.");
            return null;
        }
        return categoryRepository.findByNameIgnoreCase(categoryName)
                .orElseGet(() -> {
                    log.warn("Category '{}' not found in database. Skipped.", categoryName);
                    return null;
                });
    }

    /**
     * Validates template content: must not be empty, and must have a valid character length.
     */
    private boolean validateContent(String content) {
        if (content == null || content.isBlank()) {
            log.warn("Template content is missing or blank.");
            return false;
        }
        int length = content.length();
        if (length < 20 || length > 2000) {
            log.warn("Invalid content length ({} chars). Accepted range: 20–2000.", length);
            return false;
        }
        return true;
    }

    /**
     * Validates and builds template parameters from JSON data.
     */
    private List<TemplateParameter> validateAndBuildParameters(List<Map<String, Object>> paramsList) {
        if (paramsList == null || paramsList.isEmpty()) {
            log.warn("No parameters defined for this template.");
            return Collections.emptyList();
        }

        List<TemplateParameter> params = new ArrayList<>();
        for (Map<String, Object> p : paramsList) {
            String name = (String) p.get("name");
            Boolean required = (Boolean) p.getOrDefault("required", true);

            if (name == null || name.isBlank()) {
                log.warn("Parameter with missing or blank name. Ignored.");
                continue;
            }

            TemplateParameter param = new TemplateParameter();
            param.setName(name.trim());
            param.setRequired(required != null && required);
            params.add(param);
        }

        if (params.isEmpty()) {
            log.warn("All parameters were invalid for this template.");
            return null;
        }

        return params;
    }

    /**
     * Ensures the number of placeholders {{}} in the content matches the number of parameters.
     */
    private boolean validateParameterOccurrences(String content, List<TemplateParameter> parameters) {
        Pattern pattern = Pattern.compile("\\{\\{(.*?)}}");
        Matcher matcher = pattern.matcher(content);

        Set<String> placeholders = new HashSet<>();
        while (matcher.find()) {
            placeholders.add(matcher.group(1).trim());
        }

        if (placeholders.size() != parameters.size()) {
            log.warn("Number of placeholders \\u007b\\u007b \\u007d\\u007d ({}) differs from number of parameters ({}).",
                    placeholders.size(), parameters.size());
            return false;
        }

        for (TemplateParameter p : parameters) {
            if (!placeholders.contains(p.getName())) {
                log.warn("Parameter '{}' not found as a placeholder in the template content.", p.getName());
                return false;
            }
        }

        return true;
    }
}
