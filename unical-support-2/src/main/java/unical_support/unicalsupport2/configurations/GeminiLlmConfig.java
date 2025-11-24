package unical_support.unicalsupport2.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configurazione per l'integrazione con il modello Gemini di Google.
 * Definisce i bean necessari per comunicare con l'API Gemini.
 */
@Configuration
public class GeminiLlmConfig {
    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.model}")
    private String model;

    @Value("${gemini.base-url}")
    private String baseUrl;

    @Value("${gemini.embedding-model}")
    private String embeddingModel;

    @Value("${llm.timeout-seconds}")
    private int timeoutSeconds;


    /**
     * Configura un WebClient specifico per le richieste di embedding al servizio Gemini.
     * Imposta l'URL di base e le intestazioni necessarie, inclusa la chiave API.
     */
    @Bean
    public WebClient geminiEmbeddingWebClient(WebClient.Builder builder) {

        return builder
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("x-goog-api-key", apiKey)
                .build();
    }


    /**
     * Configura un WebClient generico per le richieste al servizio Gemini.
     * Imposta l'URL di base.
     */
    @Bean
    public WebClient geminiWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(baseUrl)
                .build();
    }


    /**
     * Crea un bean GeminiProperties che incapsula tutte le proprietà di configurazione
     * necessarie per interagire con l'API Gemini.
     * Verifica che la chiave API non sia vuota e lancia un'eccezione se lo è.
     */
    @Bean
    public GeminiProperties geminiProperties() {
        if (this.apiKey == null || this.apiKey.isBlank()) {
            throw new IllegalArgumentException("La proprietà 'llm.api-key' non può essere vuota. Configurazione fallita.");
        }
        return new GeminiProperties(this.apiKey, this.model, this.embeddingModel, this.timeoutSeconds);
    }

    /**
     * Record immutabile per incapsulare tutte le proprietà di configurazione.
     * Sarà iniettato nel GeminiApiClient.
     */
    public record GeminiProperties(String apiKey, String model, String embeddingModel, int timeoutSeconds) {}
}