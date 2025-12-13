package unical_support.unicalsupport2.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unical_support.unicalsupport2.data.dto.classifier.ClassificationEmailDto;
import unical_support.unicalsupport2.data.dto.classifier.ClassificationResultDto;
import unical_support.unicalsupport2.data.dto.classifier.SingleCategoryDto;
import unical_support.unicalsupport2.data.entities.DocumentChunk;
import unical_support.unicalsupport2.data.entities.Template;
import unical_support.unicalsupport2.data.repositories.CategoryRepository;
import unical_support.unicalsupport2.data.repositories.TemplateRepository;
import unical_support.unicalsupport2.configurations.factory.PromptStrategyFactory;
import unical_support.unicalsupport2.service.interfaces.DocumentChunkService;
import unical_support.unicalsupport2.service.interfaces.PromptService;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromptServiceImpl implements PromptService {

    private final PromptStrategyFactory promptStrategyFactory;

    private final CategoryRepository categoryRepository;
    private final TemplateRepository templateRepository;
    private final DocumentChunkService documentChunkService;

    public String buildClassifyPrompt(List<ClassificationEmailDto> emails) {

        String categoriesList = categoryRepository.findAll().stream()
                .map(cat -> cat.getName() + " (" + cat.getDescription() + ")")
                .collect(Collectors.joining(", "));

        Map<String, Object> placeholders = Map.of(
                "CATEGORIES", categoriesList,
                "EMAIL_LIST", emails
        );

        return promptStrategyFactory.generate("classifier", placeholders);
    }

    public String buildJudgePrompt(List<ClassificationEmailDto> emails, List<ClassificationResultDto> results) {

        String categoriesDescription = categoryRepository.findAll().stream()
                .map(c -> String.format("- %s: %s", c.getName(), c.getDescription()))
                .collect(Collectors.joining("\n"));

        List<Map<String, Object>> inputDataList = new ArrayList<>();

        for (int i = 0; i < emails.size(); i++) {
            ClassificationEmailDto email = emails.get(i);
            ClassificationResultDto result = results.get(i);

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", i);

            item.put("OGGETTO_EMAIL", email.getSubject());
            item.put("CORPO_EMAIL", email.getBody());
            item.put("CATEGORIE_PROPOSTE", result.getCategories());
            item.put("SPIEGAZIONE_CLASSIFICATORE", result.getExplanation());

            inputDataList.add(item);
        }

        Map<String, Object> placeholders = Map.of(
                "CATEGORIES", categoriesDescription,
                "INPUT_LIST_OF_CLASSIFICATIONS", inputDataList
        );

        return promptStrategyFactory.generate("judger", placeholders);
    }

    @Transactional(readOnly = true)
    public String buildResponderPrompt(List<ClassificationResultDto> emails) {
        String enrichedInput = buildEnrichedRAGContext(emails);

        Map<String, Object> placeholders = Map.of(
                "ENRICHED_INPUT", enrichedInput
        );

        return promptStrategyFactory.generate("responder", placeholders);
    }

    private String buildEnrichedRAGContext(List<ClassificationResultDto> emails) {
        StringBuilder sb = new StringBuilder();
        Map<String, List<Template>> templatesCache = new HashMap<>();

        int K_CHUNKS = 10;

        for (ClassificationResultDto email : emails) {
            sb.append("=== INIZIO EMAIL ID: ").append(email.getId()).append(" ===\n");

            for (SingleCategoryDto category : email.getCategories()) {
                String categoryName = category.getCategory();
                if ("NON RICONOSCIUTA".equalsIgnoreCase(categoryName)) continue;

                sb.append(">>> CATEGORIA: ").append(categoryName).append("\n");
                sb.append("    Domanda/Testo Utente: \"").append(category.getText()).append("\"\n");

                List<Template> templates = templatesCache.computeIfAbsent(
                        categoryName,
                        k -> templateRepository.findByCategoryNameIgnoreCase(categoryName)
                );

                if (!templates.isEmpty()) {
                    sb.append("    TEMPLATE DISPONIBILI:\n");
                    for (Template t : templates) {
                        sb.append("      - Nome ID: ").append(t.getName()).append("\n");
                        sb.append("        Descrizione: " ).append(t.getDescription()).append("\n");
                        sb.append("        Parametri richiesti: ").append(
                                t.getParameters().stream()
                                        .map(p -> p.getName() + (p.isRequired() ? "*" : ""))
                                        .collect(Collectors.joining(", "))
                        ).append("\n");
                        sb.append("        Corpo Template: ").append(
                                t.getContent().replace("\n", " ").trim()
                        ).append("\n");
                    }
                } else {
                    sb.append("    (Nessun template configurato per questa categoria)\n");
                }
                sb.append("\n");
            }

            sb.append(">>> FONTI INFORMATIVE (DA USARE PER RISPONDERE):\n");
            List<DocumentChunk> relevantChunks = documentChunkService.findRelevantChunks(email, K_CHUNKS);

            if (!relevantChunks.isEmpty()) {
                for (DocumentChunk chunk : relevantChunks) {
                    sb.append("[Fonte: ").append(chunk.getDocument().getOriginalFilename()).append("] ");
                    sb.append(chunk.getContent().replace("\n", " ").trim()).append("\n\n");
                }
            } else {
                sb.append("(Nessuna fonte rilevante trovata nel database documenti)\n");
            }

            sb.append("=== FINE EMAIL ID: ").append(email.getId()).append(" ===\n\n");
        }
        return sb.toString();
    }
}
