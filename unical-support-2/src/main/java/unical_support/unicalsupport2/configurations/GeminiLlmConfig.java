package unical_support.unicalsupport2.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.client.WebClient;
import unical_support.unicalsupport2.service.implementation.GeminiApiClientImpl;
import unical_support.unicalsupport2.service.interfaces.LlmClient;

/**
 * Configurazione per l'integrazione con il modello Gemini di Google.
 * Definisce i bean necessari per comunicare con l'API Gemini.
 */
@Configuration
public class GeminiLlmConfig {
    @Value("${gemini.api-key-1}")
    private String apiKey1;

    @Value("${gemini.api-key-2}")
    private String apiKey2;

    @Value("${gemini.model}")
    private String model;

    @Value("${gemini.base-url}")
    private String baseUrl;

    @Value("${gemini.embedding-model}")

    private String embeddingModel;

    @Value("${llm.timeout-seconds}")
    private int timeoutSeconds;

    /**
     * Configura un WebClient generico per le richieste al servizio Gemini.
     * Imposta l'URL di base.
     */
    @Bean
    public WebClient geminiWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    @Primary
    public LlmClient geminiClientPrimary(WebClient geminiWebClient) {
        return new GeminiApiClientImpl(geminiWebClient, createConfig("gemini-1", apiKey1));
    }

    @Bean
    public LlmClient geminiClientSecondary(WebClient geminiWebClient) {
        return new GeminiApiClientImpl(geminiWebClient, createConfig("gemini-2", apiKey2));
    }

    public record GeminiSingleConfig(String providerName, String apiKey, String model, String embeddingModel, int timeoutSeconds) {}

    private GeminiSingleConfig createConfig(String providerName, String apiKey) {
        return new GeminiSingleConfig(providerName, apiKey, this.model, this.embeddingModel, this.timeoutSeconds);
    }
}