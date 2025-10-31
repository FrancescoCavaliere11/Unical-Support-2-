package unical_support.unicalsupport2.service.implementation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import unical_support.unicalsupport2.EmailClassifier.Model.ClassificationResult;
import unical_support.unicalsupport2.data.dto.ClassificationEmailDto;
import unical_support.unicalsupport2.service.interfaces.EmailClassifier;
import unical_support.unicalsupport2.service.interfaces.GeminiApiClient;
import unical_support.unicalsupport2.service.interfaces.PromptService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailClassifierImpl implements EmailClassifier {
    private final GeminiApiClient geminiApiClient;
    private final PromptService promptService;
    private final ObjectMapper mapper = new ObjectMapper();


    @Override
    public ClassificationResult classifyEmail(ClassificationEmailDto classificationEmailDto) {
        try {
            String system = promptService.buildSystemMessage();
            String user   = promptService.buildUserMessage(classificationEmailDto);

            String raw = geminiApiClient.chat(system, user);
            JsonNode json = mapper.readTree(raw); // così posso mappare in Json

            return parseSingleResult(json);
        } catch (Exception e) {
            return new ClassificationResult(
                    ClassificationResult.Category.NON_RICONOSCIUTA,
                    0.0,
                    "Errore classificazione/API: " + e.getMessage()
            );
        }
    }

    @Override
    public List<ClassificationResult> classifyEmailBatch(List<ClassificationEmailDto> classificationEmailDtos) {
        try {
            String system = promptService.buildSystemMessageBatch(); // Sei un LLM fai questo
            String user   = promptService.buildUserMessageBatch(classificationEmailDtos); // Stringa con Email0..EmailN

            String raw = geminiApiClient.chat(system, user);      // così si invia una sola richiesta con TUTTE LE EMAIL
            ArrayNode arr = (ArrayNode) mapper.readTree(raw); // JSON navigabile, se ci sono problemi da eccezione

            // Prepara lista risultati con NON_RICONOSCIUTA di default
            List<ClassificationResult> out = new ArrayList<>(Collections.nCopies(
                    classificationEmailDtos.size(),
                    new ClassificationResult(ClassificationResult.Category.NON_RICONOSCIUTA, 0.0, "No result")
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
                    .map(e -> new ClassificationResult(
                            ClassificationResult.Category.NON_RICONOSCIUTA, 0.0,
                            "Errore batch/API: " + x.getMessage()))
                    .toList();
        }
    }

    private ClassificationResult parseSingleResult(JsonNode json){
        String categoryStr = safe(json.path("category").asText());
        double confidence  = json.path("confidence").isNumber() ? json.path("confidence").asDouble() : 0.0;
        String explanation = safe(json.path("explanation").asText());
        // uso .path e non .get perchè tendenzialmente funzionano uguale, ma path non lancia eccezione se è vuoto

        // todo le categorie vanno prese dal database
        ClassificationResult.Category category;
        try {
            category = ClassificationResult.Category.valueOf(categoryStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            category = ClassificationResult.Category.NON_RICONOSCIUTA;
        }

        // confidenza
        if (confidence < 0) confidence = 0;
        if (confidence > 1) confidence = 1;

        return new ClassificationResult(category, confidence, explanation);
    }

    // serve a evitare se il modello restituisce valore vuoto, che ci sia eccezione e da ""
    private String safe(String s) { return s == null ? "" : s; }
}
