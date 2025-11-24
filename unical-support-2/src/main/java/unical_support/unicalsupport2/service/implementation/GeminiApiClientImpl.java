package unical_support.unicalsupport2.service.implementation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import unical_support.unicalsupport2.configurations.GeminiLlmConfig;
import unical_support.unicalsupport2.service.interfaces.LlmClient;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiApiClientImpl implements LlmClient {
    private final WebClient geminiWebClient;
    private final GeminiLlmConfig.GeminiProperties geminiProperties;
    private final WebClient geminiEmbeddingWebClient;

    @Override
    public String chat(String prompt) throws Exception {
        log.info("Invoco Gemini generateContent, model={}", geminiProperties.model());

        ObjectMapper mapper = new ObjectMapper();

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

        String url = String.format("/v1beta/models/%s:generateContent?key=%s",
                geminiProperties.model(), geminiProperties.apiKey());

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


    @Override
    public float[] embed(String text) {
        String payload = """
        {
          "content": {
            "parts": [{ "text": "%s" }]
          }
        }
        """.formatted(text.replace("\"", "\\\""));

        String uri = "/v1beta/models/" + geminiProperties.embeddingModel() + ":embedContent";

        return geminiEmbeddingWebClient.post()
                .uri(uri)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(this::parseEmbedding)
                .block(Duration.ofSeconds(geminiProperties.timeoutSeconds()));
    }

    private float[] parseEmbedding(JsonNode json){
        JsonNode values = json.path("embedding").path("values");
        float[] arr = new float[values.size()];
        for (int i = 0; i < values.size(); i++) {
            arr[i] = (float) values.get(i).asDouble();
        }
        return arr;
    }
}
