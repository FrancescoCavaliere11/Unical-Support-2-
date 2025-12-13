package unical_support.unicalsupport2.service.implementation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import unical_support.unicalsupport2.configurations.factory.LlmStrategyFactory;
import unical_support.unicalsupport2.data.dto.classifier.ClassificationResultDto;
import unical_support.unicalsupport2.data.dto.classifier.ClassificationEmailDto;
import unical_support.unicalsupport2.data.dto.classifier.SingleCategoryDto;
import unical_support.unicalsupport2.data.entities.Category;
import unical_support.unicalsupport2.data.enumerators.ModuleName;
import unical_support.unicalsupport2.data.repositories.CategoryRepository;
import unical_support.unicalsupport2.service.interfaces.EmailClassifier;
import unical_support.unicalsupport2.service.interfaces.LlmClient;
import unical_support.unicalsupport2.service.interfaces.PromptService;

import java.util.*;

@Service
@RequiredArgsConstructor
public class EmailClassifierImpl implements EmailClassifier {
    private final CategoryRepository categoryRepository;
    private final PromptService promptService;
    private final LlmStrategyFactory llmStrategyFactory;
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public List<ClassificationResultDto> classifyEmail(List<ClassificationEmailDto> classificationEmailDtos) {
        try {

            String prompt = promptService.buildClassifyPrompt(classificationEmailDtos);

            LlmClient llmClient = llmStrategyFactory.getLlmClient(ModuleName.CLASSIFIER);

            String raw = llmClient.chat(prompt);
            System.out.println("Prompt:" + raw);

            String cleaned = sanitizeJson(raw);
            System.out.println("Prompt:" + cleaned);

            JsonNode root = mapper.readTree(cleaned);
            ArrayNode arr;
            if (root.isArray()) {
                arr = (ArrayNode) root;
            } else {
                arr = mapper.createArrayNode();
                arr.add(root);
            }

            // Prepara lista risultati con NON RICONOSCIUTA di default
            List<ClassificationResultDto> out = new ArrayList<>();
            for (int i = 0; i < classificationEmailDtos.size(); i++) {
                out.add(
                        new ClassificationResultDto(
                                List.of(new SingleCategoryDto("NON RICONOSCIUTA", 0.0, "")),
                                "No result",
                                i // ID = posizione nella lista
                        )
                );
            }

            for (JsonNode n : arr) {
                int id = n.path("id").asInt(-1);
                if (id < 0 || id >= classificationEmailDtos.size()) {

                    continue;
                }

                out.set(id, parseSingleResult(n));
            }

            System.out.println(out);
            return out;

        } catch (Exception x) {
            // In caso di JSON non array o errore, restituisci tutti NON RICONOSCIUTA
            List<ClassificationResultDto> classificationResultDtos = new ArrayList<>();
            for (int i = 0; i < classificationEmailDtos.size(); i++) {
                classificationResultDtos.add(
                        new ClassificationResultDto(
                                List.of(new SingleCategoryDto("NON RICONOSCIUTA", 0.0, "")),
                                "Errore batch/API: " + x.getMessage(),
                                i // ID = posizione nella lista
                        )
                );
            }

            return classificationResultDtos;
        }
    }

    /**
     * Parsing del risultato per una singola email
     */
    private ClassificationResultDto parseSingleResult(JsonNode json) {
        List<SingleCategoryDto> categoriesList = new ArrayList<>();
        String explanation = safe(json.path("explanation").asText());


        List<String> categories = categoryRepository.findAll()
                .stream()
                .map(Category::getName)
                .toList();

        if (json.path("categories").isArray()) {
            for (JsonNode n : json.path("categories")) {
                String cat = safe(n.path("name").asText());
                double conf = n.path("confidence").isNumber() ? n.path("confidence").asDouble() : 0.0;
                String text = safe(n.path("text").asText());

                addCategoryToList(categoriesList, cat, conf, text, categories);
            }
        } else {
            String categoryStr = safe(json.path("category").asText());
            double confidence = json.path("confidence").isNumber() ? json.path("confidence").asDouble() : 0.0;
            String text = safe(json.path("text").asText());

            addCategoryToList(categoriesList, categoryStr, confidence, text, categories);
        }

        return new ClassificationResultDto(categoriesList, explanation, json.path("id").asInt(-1));
    }

    /**
     * Aggiunge una categoria alla lista, validandola rispetto a quelle di DB e clampando la confidence.
     */
    private void addCategoryToList(List<SingleCategoryDto> categoriesList,
                                   String category,
                                   double confidence,
                                   String text,
                                   List<String> validCategories) {


        String cat = validCategories.stream()
                .filter(c -> c.equalsIgnoreCase(category))
                .findFirst()
                .orElse("NON RICONOSCIUTA");

        // Clamping della confidence nell'intervallo [0,1]
        if (confidence < 0) confidence = 0;
        if (confidence > 1) confidence = 1;

        categoriesList.add(new SingleCategoryDto(cat, confidence, text));
    }


    private String safe(String s) {
        return s == null ? "" : s;
    }

    /**
     * Rende la risposta del modello parsabile come JSON "pulito".
     * - Rimuove eventuali ```
     * - Se ci sono caratteri prima di '[' o '{', taglia fino al primo di questi.
     */
    private String sanitizeJson(String raw) {
        if (raw == null) return "[]";
        String s = raw.trim();


        if (s.startsWith("```")) {
            int firstNewline = s.indexOf('\n');
            if (firstNewline > 0) {
                s = s.substring(firstNewline + 1);
            } else {
                s = s.substring(3);
            }
            int lastFence = s.lastIndexOf("```");
            if (lastFence >= 0) {
                s = s.substring(0, lastFence);
            }
            s = s.trim();
        }


        int start = -1;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '[' || c == '{') {
                start = i;
                break;
            }
        }
        if (start > 0) {
            s = s.substring(start);
        }

        return s.trim();
    }
}