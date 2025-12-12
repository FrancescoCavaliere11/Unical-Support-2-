package unical_support.unicalsupport2.prompting;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Configuration properties holder for prompt templates.
 *
 * <p>Binds to properties with prefix {@code prompt} and exposes a map of module
 * names to their configuration. Each module defines a default strategy and a map
 * of strategy names to resource paths.</p>
 *
 * Example YAML structure:
 * <pre>
 * prompt:
 *   modules:
 *     myModule:
 *       defaultStrategy: default
 *       strategies:
 *         default: classpath:/prompts/myModule/default.txt
 * </pre>
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "prompt")
public class PromptProperties {

    /**
     * Mapping from module name to its configuration.
     * <p>Key: module identifier used by the application.
     * Value: {@link ModuleConfig} containing strategy settings for that module.</p>
     */
    private Map<String, ModuleConfig> modules;

    /**
     * Per-module configuration containing the selected default strategy and
     * the available strategies mapped to their resource paths.
     */
    @Data
    public static class ModuleConfig {

        /**
         * The name of the default strategy to use when generating prompts for this module.
         * This must match one of the keys in {@code strategies}.
         */
        private String defaultStrategy;

        /**
         * Map of strategy name -> resource path.
         * <p>The resource path is the location of the template file (for example,
         * a classpath resource) used to build the prompt for the given strategy.</p>
         */
        private Map<String, String> strategies;
    }
}