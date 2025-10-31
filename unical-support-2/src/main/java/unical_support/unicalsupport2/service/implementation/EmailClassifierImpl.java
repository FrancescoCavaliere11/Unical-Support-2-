package unical_support.unicalsupport2.service.implementation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import unical_support.unicalsupport2.data.dto.ClassificationResultDto;
import unical_support.unicalsupport2.data.dto.ClassificationEmailDto;
import unical_support.unicalsupport2.data.repositories.CategoryRepository;
import unical_support.unicalsupport2.service.interfaces.EmailClassifier;
import unical_support.unicalsupport2.service.interfaces.GeminiApiClient;
import unical_support.unicalsupport2.service.interfaces.PromptService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailClassifierImpl implements EmailClassifier {
    private final CategoryRepository categoryRepository;
    private final GeminiApiClient geminiApiClient;
    private final PromptService promptService;
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public List<ClassificationResultDto> classifyEmail(List<ClassificationEmailDto> classificationEmailDtos) {
        try {
            String system = promptService.buildSystemMessageBatch(); // Sei un LLM fai questo
            String user   = promptService.buildUserMessageBatch(classificationEmailDtos); // Stringa con Email0..EmailN

            String raw = geminiApiClient.chat(system, user);      // così si invia una sola richiesta con TUTTE LE EMAIL
            ArrayNode arr = (ArrayNode) mapper.readTree(raw); // JSON navigabile, se ci sono problemi da eccezione

            // Prepara lista risultati con NON_RICONOSCIUTA di default
            List<ClassificationResultDto> out = new ArrayList<>(Collections.nCopies(
                    classificationEmailDtos.size(),
                    new ClassificationResultDto(ClassificationResultDto.Category.NON_RICONOSCIUTA, 0.0, "No result")
            ));

            for (JsonNode n : arr) {
                // ogni Email ha un ID che corrisponde alla posizione dell'email
                int id = n.path("id").asInt(-1);
                if (id < 0 || id >= classificationEmailDtos.size()) continue;

                out.set(id, parseSingleResult(n));
            }
            return out;

        } catch (Exception x) {
            // In caso di JSON non array o errore, restituisci tutti NON_RICONOSCIUTA
            return classificationEmailDtos.stream()
                    .map(e -> new ClassificationResultDto(
                            ClassificationResultDto.Category.NON_RICONOSCIUTA, 0.0,
                            "Errore batch/API: " + x.getMessage()))
                    .toList();
        }
    }

    private ClassificationResultDto parseSingleResult(JsonNode json){
        String categoryStr = safe(json.path("category").asText());
        double confidence  = json.path("confidence").isNumber() ? json.path("confidence").asDouble() : 0.0;
        String explanation = safe(json.path("explanation").asText());
        // uso .path e non .get perchè tendenzialmente funzionano uguale, ma path non lancia eccezione se è vuoto

        //List<Category> categories = categoryRepository.findAll();

        // todo le categorie vanno prese dal database
        ClassificationResultDto.Category category;
        try {
            category = ClassificationResultDto.Category.valueOf(categoryStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            category = ClassificationResultDto.Category.NON_RICONOSCIUTA;
        }

        // confidenza
        if (confidence < 0) confidence = 0;
        if (confidence > 1) confidence = 1;

        return new ClassificationResultDto(category, confidence, explanation);
    }

    // serve a evitare se il modello restituisce valore vuoto, che ci sia eccezione e da ""
    private String safe(String s) { return s == null ? "" : s; }
}
