package unical_support.unicalsupport2.EmailClassifier.LLM;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import unical_support.unicalsupport2.EmailClassifier.Model.ClassificationResult;
import unical_support.unicalsupport2.EmailClassifier.Model.EmailData;
import unical_support.unicalsupport2.EmailClassifier.Prompting.Prompter;

// calsse basata su due parametri principali System e user : per capire meglio
// system dice : sei un LLM fai questo
// User invece passa all'LLM OGGETTO: RICHIESTA XXXX, CORPO: SALVE XXX grazie

// todo si può rimuovere
@Service
public class Classifier {

    private final Client llm;
    private final Prompter prompts;
    private final ObjectMapper mapper = new ObjectMapper();

    public Classifier(Client llm, Prompter prompts) {
        this.llm = llm;
        this.prompts = prompts;
    }

    public ClassificationResult classify(EmailData email) {
        try {
            String system = prompts.buildSystemMessage();
            String user   = prompts.buildUserMessage(email);

            String raw = llm.chat(system, user);
            JsonNode json = mapper.readTree(raw); // così posso mappare in Json

            String categoryStr = safe(json.path("category").asText());
            double confidence  = json.path("confidence").isNumber() ? json.path("confidence").asDouble() : 0.0;
            String explanation = safe(json.path("explanation").asText());
            // uso .path e non .get perchè tendenzialmente funzionano uguale, ma path non lancia eccezione se è vuoto

            // lo switch serve semplicemente per mappare la stringa che il modello restituisce nell'ENUM creato in classification Result
            ClassificationResult.Category category = switch (categoryStr.toUpperCase()) {
                case "INFORMAZIONI_GENERICHE" -> ClassificationResult.Category.INFORMAZIONI_GENERICHE;
                case "RECLAMO" -> ClassificationResult.Category.RECLAMO;
                case "ERASMUS_E_MOBILITA_INTERNAZIONALE" -> ClassificationResult.Category.ERASMUS_E_MOBILITA_INTERNAZIONALE;
                case "RICONOSCIMENTO_ESAMI_CFU" -> ClassificationResult.Category.RICONOSCIMENTO_ESAMI_CFU;
                case "ESAMI_E_APPELLI" -> ClassificationResult.Category.ESAMI_E_APPELLI;
                case "LAUREA_E_TESI" -> ClassificationResult.Category.LAUREA_E_TESI;
                case "SERVIZI_CAMPUS" -> ClassificationResult.Category.SERVIZI_CAMPUS;

                default -> ClassificationResult.Category.NON_RICONOSCIUTA;
            };

            // confidenza
            if (confidence < 0) confidence = 0;
            if (confidence > 1) confidence = 1;

            return new ClassificationResult(category, confidence, explanation);

        } catch (Exception e) {
            return new ClassificationResult(
                    ClassificationResult.Category.NON_RICONOSCIUTA,
                    0.0,
                    "Errore classificazione/API: " + e.getMessage()
            );
        }
    }

    // serve a evitare se il modello restituisce valore vuoto, che ci sia eccezione e da ""
    private String safe(String s) { return s == null ? "" : s; }
}


