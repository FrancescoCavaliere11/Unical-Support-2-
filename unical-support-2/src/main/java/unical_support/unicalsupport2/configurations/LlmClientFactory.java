package unical_support.unicalsupport2.configurations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import unical_support.unicalsupport2.runtime.ActiveLlmRegistry;
import unical_support.unicalsupport2.service.implementation.GeminiApiClientImpl;
import unical_support.unicalsupport2.service.implementation.GroqLlmClient;
import unical_support.unicalsupport2.service.interfaces.LlmClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class LlmClientFactory {

    @Value("${llm.provider:gemini}")
    private String providerFromProps;

    private final GeminiApiClientImpl geminiApiClient;
    private final GroqLlmClient groqLlmClient;
    private final ActiveLlmRegistry registry;


    public LlmClient current() {
        String provider = (registry.get() != null && !registry.get().isBlank())
                ? registry.get() : providerFromProps;

        LlmClient selected = switch (provider.toLowerCase()) {
            case "groq" -> groqLlmClient;
            case "gemini" -> geminiApiClient;
            default -> geminiApiClient;
        };
        log.info("LLM provider attivo: {} (registry={}, props={})",
                provider, registry.get(), providerFromProps);
        return selected;
    }
}
