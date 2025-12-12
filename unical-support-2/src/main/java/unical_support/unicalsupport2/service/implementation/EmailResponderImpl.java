package unical_support.unicalsupport2.service.implementation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import unical_support.unicalsupport2.data.dto.classifier.ClassificationResultDto;
import unical_support.unicalsupport2.data.dto.responder.ResponderResultDto;
import unical_support.unicalsupport2.data.dto.responder.SingleResponseDto;
import unical_support.unicalsupport2.service.interfaces.EmailResponder;
import unical_support.unicalsupport2.service.interfaces.LlmClient;
import unical_support.unicalsupport2.service.interfaces.PromptService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailResponderImpl implements EmailResponder {
    private final LlmClient llmClient; // Usa l'interfaccia generica
    private final PromptService promptService;
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public List<ResponderResultDto> generateEmailResponse(List<ClassificationResultDto> emails) {
        // Inizializza la lista di output con dei placeholder per mantenere l'ordine
        List<ResponderResultDto> out = new ArrayList<>();
        for (int i = 0; i < emails.size(); i++) {
            out.add(new ResponderResultDto(i, new ArrayList<>()));
        }

        try {
            String prompt = promptService.buildResponderPrompt(emails);
            String raw = llmClient.chat(prompt);

            // 1. Pulizia JSON
            String cleaned = sanitizeJson(raw);
            log.debug("Responder JSON Cleaned: {}", cleaned);

            JsonNode root = mapper.readTree(cleaned);
            ArrayNode arr;

            // 2. Gestione Flessibile: Se è un Oggetto, lo avvolgiamo in un Array
            if (root.isArray()) {
                arr = (ArrayNode) root;
            } else {
                arr = mapper.createArrayNode();
                arr.add(root);
            }

            for (JsonNode emailNode : arr) {
                int emailId = emailNode.path("email_id").asInt(-1);

                // 3. Fallback ID: Se l'ID manca e stiamo processando solo 1 email, assumiamo sia la 0
                if (emailId == -1 && emails.size() == 1 && arr.size() == 1) {
                    emailId = 0;
                }

                // Se l'ID non è valido o fuori range, saltiamo
                if (emailId < 0 || emailId >= emails.size()) {
                    log.warn("Responder: ID {} non valido o fuori range. Ignorato.", emailId);
                    continue;
                }

                List<SingleResponseDto> responses = new ArrayList<>();
                JsonNode respArr = emailNode.path("responses");

                if (respArr.isArray()) {
                    for (JsonNode r : respArr) {
                        String category = safe(r.path("category").asText());
                        String template = r.path("template").isNull() ? null : safe(r.path("template").asText());
                        String content = r.path("content").isNull() ? null : safe(r.path("content").asText());
                        String reason = safe(r.path("reason").asText());

                        Map<String,String> params = new HashMap<>();
                        JsonNode p = r.path("parameters");
                        if (p.isObject()) {
                            p.fieldNames().forEachRemaining(fieldName -> {
                                JsonNode valueNode = p.get(fieldName);
                                params.put(fieldName, valueNode.isNull() ? null : valueNode.asText());
                            });
                        }

                        responses.add(new SingleResponseDto(category, template, content, params, reason));
                    }
                }

                // Sovrascriviamo il placeholder con i dati veri
                out.set(emailId, new ResponderResultDto(emailId, responses));
            }

            // 4. Controllo Finale: Se dopo tutto questo un'email non ha risposte, mettiamo errore
            for (ResponderResultDto responderResultDto : out) {
                if (responderResultDto.getResponses().isEmpty()) {
                    responderResultDto.setResponses(List.of(
                            new SingleResponseDto(null, null, null, Map.of(), "NO_RESPONSE_FROM_LLM")
                    ));
                }
            }

            return out;

        } catch (Exception x) {
            log.error("Errore critico nel Responder: {}", x.getMessage());
            // In caso di crash totale del parsing, restituiamo errore per tutte
            List<ResponderResultDto> fallback = new ArrayList<>();
            for (int i = 0; i < emails.size(); i++) {
                fallback.add(new ResponderResultDto(i, List.of(
                        new SingleResponseDto(null, null, null, Map.of(), "ERROR: " + x.getMessage())
                )));
            }
            return fallback;
        }
    }

    private String safe(String s) { return s == null ? "" : s; }

    private String sanitizeJson(String raw) {
        if (raw == null) return "[]";
        String s = raw.trim();
        // Rimuove markdown ```json ... ```
        if (s.startsWith("```")) {
            int firstNewline = s.indexOf('\n');
            s = (firstNewline > 0) ? s.substring(firstNewline + 1) : s.substring(3);
            int lastFence = s.lastIndexOf("```");
            if (lastFence >= 0) s = s.substring(0, lastFence);
            s = s.trim();
        }
        // Trova l'inizio del JSON (Graffa o Quadra)
        int startObj = s.indexOf('{');
        int startArr = s.indexOf('[');

        int start = -1;
        if (startObj != -1 && startArr != -1) start = Math.min(startObj, startArr);
        else if (startObj != -1) start = startObj;
        else if (startArr != -1) start = startArr;

        if (start >= 0) {
            s = s.substring(start);
        }
        return s;
    }
}