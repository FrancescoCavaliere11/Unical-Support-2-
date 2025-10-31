package unical_support.unicalsupport2.EmailClassifier.LLM;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.stereotype.Service;
import unical_support.unicalsupport2.EmailClassifier.Model.ClassificationResult;
import unical_support.unicalsupport2.EmailClassifier.Model.EmailData;
import unical_support.unicalsupport2.EmailClassifier.Prompting.BatchPrompter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// versione per funzionare in batch di classifier, gestisce una list<EmailData> e non una sola EmailData,
// Il prompt è costruito con batchPrompter, e grazie a questo prompt specifico si invia una sola richiesta all'LLM
// Riceve una risposta con le informazioni per ogni email
// La trasforma in una lista di ClassificationResult in modo da uniformare i risultati

// todo si può rimuoverere questa classe e usare EmailClassifierImpl con GeminiApiClient
@Service
public class ClassifierBatch {

    private final Client llm;               // LLm che si occupa di eseguire chiamate a gemini
    private final BatchPrompter prompts;    // costruisce i messaggi system e user
    private final ObjectMapper mapper = new ObjectMapper(); // converte la risposta in json

    public ClassifierBatch(Client llm, BatchPrompter prompts) {
        this.llm = llm;
        this.prompts = prompts;
    }

    public List<ClassificationResult> classifyBatch(List<EmailData> emails) {
        try {
            String system = prompts.buildSystemMessage(); // Sei un LLM fai questo
            String user   = prompts.buildUserMessage(emails); // Stringa con Email0..EmailN

            String raw = llm.chat(system, user);      // così si invia una sola richiesta con TUTTE LE EMAIL
            ArrayNode arr = (ArrayNode) mapper.readTree(raw); // JSON navigabile, se ci sono problemi da eccezione

            // Prepara lista risultati con NON_RICONOSCIUTA di default
            List<ClassificationResult> out = new ArrayList<>(Collections.nCopies(
                    emails.size(),
                    new ClassificationResult(ClassificationResult.Category.NON_RICONOSCIUTA, 0.0, "No result")
            ));

            for (JsonNode n : arr) {
                // ogni Email ha un ID che corrisponde alla posizione dell'email
                int id = n.path("id").asInt(-1);
                if (id < 0 || id >= emails.size()) continue;

                String catStr = n.path("category").asText("");
                double conf   = n.path("confidence").isNumber() ? n.path("confidence").asDouble() : 0.0;
                String expl   = n.path("explanation").asText("");

                ClassificationResult.Category cat = switch (catStr.toUpperCase()) {
                    case "INFORMAZIONI_GENERICHE" -> ClassificationResult.Category.INFORMAZIONI_GENERICHE;
                    case "RECLAMO" -> ClassificationResult.Category.RECLAMO;
                    case "ERASMUS_E_MOBILITA_INTERNAZIONALE" -> ClassificationResult.Category.ERASMUS_E_MOBILITA_INTERNAZIONALE;
                    case "RICONOSCIMENTO_ESAMI_CFU" -> ClassificationResult.Category.RICONOSCIMENTO_ESAMI_CFU; // fix refuso
                    case "ESAMI_E_APPELLI" -> ClassificationResult.Category.ESAMI_E_APPELLI;
                    case "LAUREA_E_TESI" -> ClassificationResult.Category.LAUREA_E_TESI;
                    case "SERVIZI_CAMPUS" -> ClassificationResult.Category.SERVIZI_CAMPUS;
                    default -> ClassificationResult.Category.NON_RICONOSCIUTA;
                };
                if (conf < 0) conf = 0;
                if (conf > 1) conf = 1;

                out.set(id, new ClassificationResult(cat, conf, expl));
            }
            return out;

        } catch (Exception x) {
            // In caso di JSON non array o errore, restituisci tutti NON_RICONOSCIUTA
            return emails.stream()
                    .map(e -> new ClassificationResult(
                            ClassificationResult.Category.NON_RICONOSCIUTA, 0.0,
                            "Errore batch/API: " + x.getMessage()))
                    .toList();
        }
    }
}

