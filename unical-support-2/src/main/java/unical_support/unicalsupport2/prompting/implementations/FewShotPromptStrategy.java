package unical_support.unicalsupport2.prompting.implementations;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import unical_support.unicalsupport2.data.dto.classifier.ClassificationEmailDto;
import unical_support.unicalsupport2.data.dto.classifier.ClassificationResultDto;
import unical_support.unicalsupport2.data.dto.classifier.SingleCategoryDto;
import unical_support.unicalsupport2.data.entities.Template;
import unical_support.unicalsupport2.data.repositories.CategoryRepository;
import unical_support.unicalsupport2.data.repositories.TemplateRepository;
import unical_support.unicalsupport2.prompting.PromptStrategy;
import unical_support.unicalsupport2.prompting.PromptTemplateLoader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of PromptStrategy that builds prompts using few-shot learning examples.
 */
@Component("fewShot")
@RequiredArgsConstructor
public class FewShotPromptStrategy implements PromptStrategy {

    private final CategoryRepository categoryRepository;
    private final TemplateRepository templateRepository;
    private final PromptTemplateLoader promptTemplateLoader;
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Builds a prompt for classifying emails using few-shot learning.
     *
     * @param classificationEmailDtos List of emails to classify.
     * @return The constructed prompt string.
     */
    @Override
    public String buildClassifyPrompt(List<ClassificationEmailDto> classificationEmailDtos) {
        List<String> categories = categoryRepository.findAll()
                .stream()
                .map(cat -> "name: \"" + cat.getName() + "\", description: \"" + cat.getDescription() + "\"")
                .toList();

        String categoriesBlock = String.join(", ", categories);

        StringBuilder emailList = new StringBuilder();
        for (int i = 0; i < classificationEmailDtos.size(); i++) {
            ClassificationEmailDto e = classificationEmailDtos.get(i);
            emailList.append("ID: ").append(i).append("\n")
                    .append("OGGETTO: ").append(ns(e.getSubject())).append("\n")
                    .append("CORPO: ").append(ns(e.getBody())).append("\n\n");
        }

        String template = promptTemplateLoader.loadTemplate("classifier", "few_shot");

        return template
                .replace("{{CATEGORIES}}", categoriesBlock)
                .replace("{{EMAIL_LIST}}", emailList.toString());
    }

    /**
     * Builds a prompt for judging the classification results of emails.
     *
     * @param emails  List of emails that were classified.
     * @param results Corresponding classification results to judge.
     * @return The constructed prompt string.
     */
    @Override
    public String buildJudgePrompt(List<ClassificationEmailDto> emails,
                                   List<ClassificationResultDto> results) {

        StringBuilder emailList = new StringBuilder();

        for (int i = 0; i < emails.size(); i++) {
            ClassificationEmailDto email = emails.get(i);
            ClassificationResultDto classification = results.get(i);

            String categoriesJson;
            try {
                categoriesJson = mapper.writeValueAsString(classification.getCategories());
            } catch (Exception ex) {
                categoriesJson = "[]";
            }

            emailList.append("ID: ").append(i).append("\n")
                    .append("OGGETTO: ").append(ns(email.getSubject())).append("\n")
                    .append("CORPO: ").append(ns(email.getBody())).append("\n")
                    .append("CATEGORIE_PROPOSTE:\n")
                    .append(categoriesJson).append("\n")
                    .append("SPIEGAZIONE DEL CLASSIFICATORE:\n")
                    .append(ns(classification.getExplanation())).append("\n\n");
        }

        String template = promptTemplateLoader.loadTemplate("judger", "few_shot");
        return template.replace("{{EMAIL_LIST}}", emailList.toString());
    }

    /**
     * Builds a prompt for generating email responses based on classified emails.
     *
     * @param emails List of classified emails.
     * @return The constructed prompt string.
     */
    @Override
    public String buildResponderPrompt(List<ClassificationResultDto> emails) {
        Map<String, List<Template>> templatesCache = new HashMap<>();

        StringBuilder inputList = new StringBuilder();
        inputList.append("Di seguito le email classificate e i template disponibili.\n");
        inputList.append("Genera la risposta nel formato JSON richiesto.\n\n");

        for (ClassificationResultDto email : emails) {
            inputList.append("EMAIL ID: ").append(email.getId()).append("\n");
            inputList.append("CATEGORIE:\n");

            for (SingleCategoryDto category : email.getCategories()) {
                String categoryName = category.getCategory();

                List<Template> templates = templatesCache.computeIfAbsent(
                        categoryName,
                        k -> templateRepository.findByCategoryNameIgnoreCase(categoryName)
                );

                inputList.append(" - Categoria: ").append(category.getCategory()).append("\n");
                inputList.append("   Confidence: ").append(category.getConfidence()).append("\n");
                inputList.append("   Testo: ").append(ns(category.getText())).append("\n");

                if (!templates.isEmpty()) {
                    inputList.append("   Template disponibili:\n");
                    templates.forEach(t -> {
                        inputList.append("     * Nome: ").append(t.getName()).append("\n");
                        inputList.append("       Parametri: ").append(
                                t.getParameters().stream()
                                        .map(p -> p.getName() + " (required=" + p.isRequired() + ")")
                                        .toList()
                        ).append("\n");
                        inputList.append("       Contenuto template: ")
                                .append(t.getContent().replace("\n", " ").replace("\r", ""))
                                .append("\n");
                    });
                } else {
                    inputList.append("   Template disponibili: nessuno\n");
                }
            }
            inputList.append("\n");
        }

        String template = promptTemplateLoader.loadTemplate("responder", "few_shot");
        return template.replace("{{INPUT_LIST_OF_EMAILS}}", inputList.toString());
    }

    private String ns(String s) {
        return s == null ? "" : s;
    }
}
