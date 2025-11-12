package unical_support.unicalsupport2.service.implementation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import unical_support.unicalsupport2.data.dto.ClassificationResultDto;
import unical_support.unicalsupport2.data.dto.ClassificationEmailDto;
import unical_support.unicalsupport2.data.dto.SingleCategoryDto;
import unical_support.unicalsupport2.data.entities.Category;
import unical_support.unicalsupport2.data.repositories.CategoryRepository;
import unical_support.unicalsupport2.prompting.PromptService;
import unical_support.unicalsupport2.service.interfaces.EmailClassifier;
import unical_support.unicalsupport2.service.interfaces.LlmClient;

import java.util.*;

@Service
@RequiredArgsConstructor
public class EmailClassifierImpl implements EmailClassifier {
    private final CategoryRepository categoryRepository;
    private final LlmClient geminiApiClient;
    private final PromptService promptService;
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public List<ClassificationResultDto> classifyEmail(List<ClassificationEmailDto> classificationEmailDtos) {
        try {
            String system = promptService.buildClassifyPrompt(classificationEmailDtos);     // Sei un LLM fai questo
            String raw = geminiApiClient.chat(system);        // Così si invia una sola richiesta con TUTTE LE EMAIL
            ArrayNode arr = (ArrayNode) mapper.readTree(raw);       // JSON navigabile, se ci sono problemi da eccezione

            // Prepara lista risultati con NON_RICONOSCIUTA di default
            List<ClassificationResultDto> out = new ArrayList<>(Collections.nCopies(
                    classificationEmailDtos.size(),
                    new ClassificationResultDto(List.of(new SingleCategoryDto("NON_RICONOSCIUTA", 0.0, "")), "No result")
            ));

            for (JsonNode n : arr) {
                // Ogni Email ha un ID che corrisponde alla posizione dell'email
                int id = n.path("id").asInt(-1);
                if (id < 0 || id >= classificationEmailDtos.size()) continue;

                out.set(id, parseSingleResult(n));
            }
            return out;

        } catch (Exception x) {
            // In caso di JSON non array o errore, restituisci tutti NON_RICONOSCIUTA
            return classificationEmailDtos.stream()
                    .map(e -> new ClassificationResultDto(
                            List.of(new SingleCategoryDto("NON_RICONOSCIUTA", 0.0, "")),
                            "Errore batch/API: " + x.getMessage())
                    )
                    .toList();
        }
    }

    private ClassificationResultDto parseSingleResult(JsonNode json){
        List<SingleCategoryDto> categoriesList = new ArrayList<>();
        String explanation = safe(json.path("explanation").asText());

        // Recupero l'elenco delle categorie dal DB
        List<String> categories = categoryRepository.findAll()
                .stream()
                .map(Category::getName)
                .toList();

        // Classificazione multi-label
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

        return new ClassificationResultDto(categoriesList, explanation);
    }

    // Metodo per aggiungere alla mappa una categoria con la propria confidenza
    private void addCategoryToList(List<SingleCategoryDto> categoriesList, String category, double confidence, String text, List<String> categories){
        // Valida la categoria
        String cat = categories.stream()
                .filter(c -> c.equalsIgnoreCase(category))
                .findFirst()
                .orElse("NON_RICONOSCIUTA");

        // Intervallo valore confidenza
        if (confidence < 0) confidence = 0;
        if (confidence > 1) confidence = 1;

        categoriesList.add(new SingleCategoryDto(cat, confidence, text));
    }

    // Serve a evitare se il modello restituisce valore vuoto, che ci sia eccezione e da ""
    private String safe(String s) { return s == null ? "" : s; }
}