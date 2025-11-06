package unical_support.unicalsupport2.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;


@Configuration
public class GeminiLlmConfig {
    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.model}")
    private String model;

    @Value("${llm.timeout-seconds}")
    private int timeoutSeconds;

    @Bean
    public WebClient geminiWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
    }

    @Bean
    public GeminiProperties geminiProperties() {
        if (this.apiKey == null || this.apiKey.isBlank()) {
            throw new IllegalArgumentException("La proprietà 'llm.api-key' non può essere vuota. Configurazione fallita.");
        }
        return new GeminiProperties(this.apiKey, this.model, this.timeoutSeconds);
    }

    /**
     * Record immutabile per incapsulare tutte le proprietà di configurazione.
     * Sarà iniettato nel GeminiApiClient.
     */
    public record GeminiProperties(String apiKey, String model, int timeoutSeconds) {}
}