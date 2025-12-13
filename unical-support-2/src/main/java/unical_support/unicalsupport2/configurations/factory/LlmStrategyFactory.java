package unical_support.unicalsupport2.configurations.factory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import unical_support.unicalsupport2.data.enumerators.ModuleName;
import unical_support.unicalsupport2.service.interfaces.LlmClient;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory component that exposes available LLM client implementations and
 * returns the appropriate client for a given application module.
 *
 * <p>The factory collects all beans implementing {@link LlmClient} and indexes
 * them by their provider name (lowercased). It resolves which provider to use
 * per module based on {@link LlmProperties} configuration and falls back to
 * the configured default provider or the first available client when needed.</p>
 */
@Component
@Slf4j
public class LlmStrategyFactory {

    private final Map<String, LlmClient> strategies;
    private final LlmProperties llmProperties;

    /**
     * Constructs the factory and indexes the provided LLM clients.
     *
     * @param clients      list of available {@link LlmClient} beans; each client is
     *                     indexed by {@code client.getProviderName().toLowerCase()}.
     * @param llmProperties configuration holder used to resolve per-module provider
     *                      and default provider values.
     */
    public LlmStrategyFactory(List<LlmClient> clients, LlmProperties llmProperties) {
        this.llmProperties = llmProperties;

        // Mappo i client disponibili (gemini e groq)
        this.strategies = clients.stream()
                .collect(Collectors.toMap(
                        client -> client.getProviderName().toLowerCase(),
                        Function.identity()
                ));

        log.info("LlmStrategyFactory pronta. Provider disponibili: {}", strategies.keySet());
    }

    /**
     * Retrieves the {@link LlmClient} implementation for the given module.
     *
     * <p>Resolution steps:
     * <ol>
     *   <li>Look up the configured provider for the module in {@link LlmProperties#getModules()} using the
     *       module name lowercased.</li>
     *   <li>If no provider is configured or the configured value is blank, use the configured default provider
     *       from {@link LlmProperties#getDefaultProvider()}.</li>
     *   <li>Return the client matching the resolved provider name (case-insensitive).</li>
     *   <li>If no matching client is found, log an error and return the first available client as a fallback.</li>
     * </ol></p>
     *
     * @param module the module for which an LLM client is required
     * @return the matching {@link LlmClient} or a fallback client if the requested provider is not available
     */
    public LlmClient getLlmClient(ModuleName module) {
        String configuredProvider = llmProperties.getModules().get(module.name().toLowerCase());

        if (configuredProvider == null || configuredProvider.isBlank()) {
            configuredProvider = llmProperties.getDefaultProvider();
        }

        LlmClient client = strategies.get(configuredProvider.toLowerCase());

        if (client != null) {
            return client;
        }

        log.error("Provider '{}' richiesto per il modulo {} non trovato! Uso fallback.", configuredProvider, module);
        return strategies.values().iterator().next();
    }

    /**
     * Returns the set of available provider keys (lowercased provider names) registered in the factory.
     *
     * @return a set containing available provider identifiers
     */
    public Set<String> getAvailableProviders() {
        return strategies.keySet();
    }
}
