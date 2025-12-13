package unical_support.unicalsupport2.configurations.factory;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Configuration properties holder for LLM (Large Language Model) provider selection.
 *
 * <p>Binds to properties with prefix {@code llm} and exposes a default provider
 * as well as a per-module mapping that selects which provider to use for each
 * application module.</p>
 *
 * <p>Example YAML structure:
 * <pre>
 * llm:
 *   default-provider: gpt4
 *   modules:
 *     chat: gpt4
 *     search: gpt3.5
 * </pre>
 * </p>
 *
 * <p>Lombok's {@code @Data} generates getters, setters, {@code toString()},
 * {@code equals()} and {@code hashCode()}.</p>
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "llm")
public class LlmProperties {

    /**
     * The key/name of the default LLM provider to use when no provider is configured
     * for a specific module.
     *
     * <p>Example: {@code "gpt4"}. If a module does not have an explicit entry in
     * {@link #modules}, the application should fall back to this provider.</p>
     */
    private String defaultProvider;

    /**
     * Mapping from module name to provider identifier.
     *
     * <p>Key: module identifier used by the application (for example {@code "chat"}
     * or {@code "search"}). Value: provider key/name (for example {@code "gpt4"}
     * or {@code "gpt3.5"}).</p>
     *
     * <p>Use this to override the global {@link #defaultProvider} on a per-module
     * basis.</p>
     */
    private Map<String, String> modules;
}

