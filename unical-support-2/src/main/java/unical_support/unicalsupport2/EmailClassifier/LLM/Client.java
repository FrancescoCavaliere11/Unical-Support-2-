package unical_support.unicalsupport2.EmailClassifier.LLM;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import unical_support.unicalsupport2.EmailClassifier.Config.LlmProperties;
import java.time.Duration;
// todo rimuovere

// Classe per gestire l'interazione con gemini, costruisce il payload, invia una richiesta HTTP, e poi restituisce
// E infine valida la risposta ("risposta vuota non valida")
@Component
public class Client {

    private final WebClient webClient;           // WebClient spring-friendly
    private final ObjectMapper mapper = new ObjectMapper();
    private final String apiKey;
    private final String model;
    private final int timeoutSeconds;

    public Client(LlmProperties props, WebClient.Builder builder) {
        if (props.getApiKey() == null || props.getApiKey().isBlank()) {
            throw new IllegalArgumentException("API_KEY non impostata.");
        }
        this.apiKey = props.getApiKey();
        this.model = (props.getModel() == null || props.getModel().isBlank()) ? "gemini-2.5-flash" : props.getModel();
        this.timeoutSeconds = props.getTimeoutSeconds() <= 0 ? 50 : props.getTimeoutSeconds();

        this.webClient = builder
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
    }

    // questo è il metodo importante, il classificatore non sa che modello sta usando ma la risposta testuale del modello viene considerata qua
    public String chat(String systemMessage, String userMessage) throws Exception {
        // GEMINI usa uno schema preciso del tipo Contents - CONTENT - PARTS- TEXT
        String prompt = systemMessage + "\n\n" + userMessage;

        ObjectNode req = mapper.createObjectNode();
        ArrayNode contents = mapper.createArrayNode();

        ObjectNode part = mapper.createObjectNode();
        part.put("text", prompt);

        ObjectNode content = mapper.createObjectNode();
        ArrayNode parts = mapper.createArrayNode();
        parts.add(part);
        content.set("parts", parts);

        contents.add(content);
        req.set("contents", contents);

        String url = String.format("/v1beta/models/%s:generateContent?key=%s", model, apiKey);

        String body = webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(mapper.writeValueAsString(req))
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .onErrorResume(ex -> Mono.error(new RuntimeException("Errore API Gemini: " + ex.getMessage(), ex)))
                .block();

        JsonNode root = mapper.readTree(body);
        JsonNode textNode = root.path("candidates").get(0)
                .path("content").path("parts").get(0).path("text");

        if (textNode.isMissingNode() || textNode.asText().isBlank()) {
            throw new RuntimeException("Risposta vuota/non valida: " + body);
        }
        return textNode.asText();
    }
}
