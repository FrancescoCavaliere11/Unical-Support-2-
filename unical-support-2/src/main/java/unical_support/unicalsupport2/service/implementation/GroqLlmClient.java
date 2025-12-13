package unical_support.unicalsupport2.service.implementation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import unical_support.unicalsupport2.configurations.GroqLlmConfig;
import unical_support.unicalsupport2.service.interfaces.LlmClient;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroqLlmClient implements LlmClient {

    private final WebClient groqWebClient;
    private final GroqLlmConfig.GroqProperties groqProperties;
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String chat(String prompt) throws Exception {
        log.info("Invoco Groq /chat/completions, model={}", groqProperties.model());

        ObjectNode req = mapper.createObjectNode();
        req.put("model", groqProperties.model());
        req.put("temperature", 0.0);

        ArrayNode messages = mapper.createArrayNode();

        ObjectNode sys = mapper.createObjectNode();
        sys.put("role", "system"); //TODO lasiare system?
        sys.put("content", prompt);
        messages.add(sys);

//        ObjectNode user = mapper.createObjectNode();
//        user.put("role", "user");
//        user.put("content", userMessage);
//        messages.add(user);

        req.set("messages", messages);

        /*
        String body = groqWebClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(mapper.writeValueAsString(req))
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(groqProperties.timeoutSeconds()))
                .onErrorResume(ex -> Mono.error(new RuntimeException("Errore API Groq: " + ex.getMessage(), ex)))
                .block();

         */

        ResponseEntity<String> responseEntity = groqWebClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(mapper.writeValueAsString(req))
                .retrieve()
                .toEntity(String.class)
                .block();

        // Header quota token
        String remainingTokens = responseEntity.getHeaders().getFirst("x-ratelimit-remaining-tokens");
        String totalTokens = responseEntity.getHeaders().getFirst("x-ratelimit-limit-tokens");
        log.info("Token rimanenti: {} / {}", remainingTokens, totalTokens);
        String body = responseEntity.getBody();

        JsonNode root = mapper.readTree(body);
        JsonNode content = root.path("choices").get(0).path("message").path("content");

        if (content.isMissingNode() || content.asText().isBlank()) {
            throw new RuntimeException("Risposta vuota/non valida: " + body);
        }
        return content.asText();
    }

    @Override
    public float[] embed(String text) {
        return new float[0];    // TODO implementare embedding con Groq
    }
    @Override
    public String getProviderName() {
        return "groq";
    }
}

