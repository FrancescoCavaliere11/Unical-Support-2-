package unical_support.unicalsupport2.service.implementation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import unical_support.unicalsupport2.configurations.GeminiLlmConfig;
import unical_support.unicalsupport2.service.interfaces.GeminiApiClient;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class GeminiApiClientImpl implements GeminiApiClient {
    private final WebClient geminiWebClient;
    private final GeminiLlmConfig.GeminiProperties geminiProperties;


    @Override
    public String chat(String systemMessage, String userMessage) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

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

        String url = String.format("/v1beta/models/%s:generateContent?key=%s", geminiProperties.model(), geminiProperties.apiKey());

        String body = geminiWebClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(mapper.writeValueAsString(req))
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(geminiProperties.timeoutSeconds()))
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