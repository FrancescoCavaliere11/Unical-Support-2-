package unical_support.unicalsupport2.configurations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import unical_support.unicalsupport2.service.implementation.GeminiApiClientImpl;
import unical_support.unicalsupport2.service.implementation.GroqLlmClient;
import unical_support.unicalsupport2.service.interfaces.LlmClient;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class LlmClientFactory {

    @Value("${llm.provider:gemini}")
    private String provider;

    private final GeminiApiClientImpl geminiApiClient;
    private final GroqLlmClient groqLlmClient;

    @Bean
    @Primary
    public LlmClient llmClient() {
        LlmClient selected = switch (provider.toLowerCase()) {
            case "groq" -> groqLlmClient;
            case "gemini" -> geminiApiClient;
            default -> geminiApiClient;
        };
        log.info("LLM provider attivo: {}", provider);
        return selected;
    }
}
