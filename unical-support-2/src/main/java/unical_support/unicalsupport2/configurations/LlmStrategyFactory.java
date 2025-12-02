package unical_support.unicalsupport2.configurations;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import unical_support.unicalsupport2.runtime.ActiveLlmRegistry;
import unical_support.unicalsupport2.service.implementation.LlmClientBridge; // Importante per il filtro
import unical_support.unicalsupport2.service.interfaces.LlmClient;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class LlmStrategyFactory {

    private final Map<String, LlmClient> strategies;
    private final ActiveLlmRegistry activeLlmRegistry;
    private final String defaultProvider;

    public LlmStrategyFactory(List<LlmClient> clients,
                              ActiveLlmRegistry activeLlmRegistry,
                              @Value("${llm.provider:gemini}") String defaultProvider) {

        this.activeLlmRegistry = activeLlmRegistry;
        this.defaultProvider = defaultProvider;

        // *** FIX CRITICO ***
        // Filtriamo via il Bridge dalla lista dei provider.
        // Spring inietta TUTTI gli LlmClient (incluso il Bridge), ma noi vogliamo
        // solo le implementazioni reali (Gemini, Groq) nella mappa.
        this.strategies = clients.stream()
                .filter(client -> !(client instanceof LlmClientBridge))
                .collect(Collectors.toMap(
                        client -> client.getProviderName().toLowerCase(),
                        Function.identity()
                ));

        log.info("LlmStrategyFactory initialized with providers: {}", strategies.keySet());
    }

    /**
     * Recupera l'LLM attivo basandosi sul registro dinamico o sul default.
     */
    public LlmClient getLlmClient() {
        // 1. Cerca selezione runtime (modificata da comando shell)
        String currentSelection = activeLlmRegistry.get();

        // 2. Se null, usa default da properties
        if (currentSelection == null || currentSelection.isBlank()) {
            currentSelection = defaultProvider;
        }

        // 3. Recupera dalla mappa
        LlmClient client = strategies.get(currentSelection.toLowerCase());

        if (client != null) {
            return client;
        }

        log.warn("Provider '{}' non trovato. Uso fallback.", currentSelection);
        // Fallback sul default, o sul primo disponibile se il default è errato
        return strategies.getOrDefault(defaultProvider, strategies.values().iterator().next());
    }

    /**
     * Recupera un LLM specifico per nome (es. usato dal Judger che vuole un modello dedicato).
     */
    public LlmClient getLlmClient(String providerName) {
        if (providerName == null || providerName.isBlank()) {
            return getLlmClient(); // Fallback sul default globale
        }

        LlmClient client = strategies.get(providerName.toLowerCase());

        if (client != null) {
            return client;
        }

        log.warn("Provider specifico '{}' non trovato. Uso default.", providerName);
        return getLlmClient();
    }
}