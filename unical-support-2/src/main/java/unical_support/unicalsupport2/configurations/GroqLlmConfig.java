package unical_support.unicalsupport2.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;


@Configuration
public class GroqLlmConfig {

    @Value("${groq.api-key}")
    private String apiKey;

    @Value("${groq.model}")
    private String model;

    @Value("${groq.base-url:https://api.groq.com/openai/v1}")
    private String baseUrl;

    @Value("${llm.timeout-seconds}")
    private int timeoutSeconds;

    @Bean
    public WebClient groqWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    @Bean
    public GroqProperties groqProperties() {
        if (this.apiKey == null || this.apiKey.isBlank()) {
            throw new IllegalArgumentException("La proprietà 'groq.api-key' non può essere vuota. Configurazione fallita.");
        }
        return new GroqProperties(this.apiKey, this.model, this.timeoutSeconds, this.baseUrl);
    }


    public record GroqProperties(String apiKey, String model, int timeoutSeconds, String baseUrl) {}
}
