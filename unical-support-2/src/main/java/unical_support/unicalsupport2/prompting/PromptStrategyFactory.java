package unical_support.unicalsupport2.prompting;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory responsible for generating prompt strings for configured modules.
 *
 * <p>The factory reads template files from resources (via {@link ResourceLoader}),
 * caches template contents to avoid repeated IO, and fills placeholders using a provided
 * map. Non-string placeholder values are serialized to JSON using the injected
 * {@link ObjectMapper}.</p>
 *
 * <p>Dependencies are injected through constructor (Lombok {@code @RequiredArgsConstructor}).</p>
 */
@Component
@RequiredArgsConstructor
public class PromptStrategyFactory {

    private final PromptProperties promptProperties;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;

    /**
     * Cache mapping resource path -> template content.
     * <p>Uses a thread-safe {@link ConcurrentHashMap} to avoid re-reading files.</p>
     */
    private final Map<String, String> promptsCache = new ConcurrentHashMap<>();


    /**
     * Generic method to load a template for the given module and replace placeholders.
     *
     * <p>Procedure:
     * <ol>
     *   <li>Resolve module configuration from {@code promptProperties} by {@code moduleName}.</li>
     *   <li>Obtain the default strategy and corresponding resource path.</li>
     *   <li>Load the template (cached) and replace placeholders using {@link #fillPromptTemplate}.</li>
     * </ol></p>
     *
     * @param moduleName  the name of the module configured in {@code PromptProperties}
     * @param placeholders map of placeholder keys to values; placeholder format is {@code {{key}}}
     * @return the generated prompt with placeholders substituted
     * @throws IllegalArgumentException if the module or the selected strategy is not configured
     */
    public String generate(String moduleName, Map<String, Object> placeholders) {

        var moduleConfig = promptProperties.getModules().get(moduleName);
        if (moduleConfig == null) {
            throw new IllegalArgumentException("Modulo non configurato: " + moduleName);
        }

        String actualStrategy =  moduleConfig.getDefaultStrategy();

        String resourcePath = moduleConfig.getStrategies().get(actualStrategy);
        if (resourcePath == null) {
            throw new IllegalArgumentException("Strategia '" + actualStrategy + "' non trovata nel modulo " + moduleName);
        }

        String template = promptsCache.computeIfAbsent(resourcePath, this::loadResourceAsString);

        return fillPromptTemplate(template, placeholders);
    }

    /**
     * Load the content of a resource file as a UTF-8 string.
     *
     * <p>The method wraps IO errors in a {@link RuntimeException} to simplify callers.</p>
     *
     * @param path resource path understood by {@link ResourceLoader}
     * @return file content as a string
     * @throws RuntimeException if the resource cannot be read
     */
    private String loadResourceAsString(String path) {
        try {
            Resource resource = resourceLoader.getResource(path);
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Impossibile caricare il prompt dal path: " + path, e);
        }
    }

    /**
     * Replace placeholders in the template with provided values.
     *
     * <p>Placeholder format: {@code {{key}}} for each entry in the {@code placeholders} map.
     * If a placeholder value is a {@link String}, it is inserted as-is. For other object types,
     * the value is serialized to a pretty-printed JSON string using the injected
     * {@link ObjectMapper}. On serialization failure a fixed error string is inserted.</p>
     *
     * <p>Replacements are performed using simple string replacement for each map entry.</p>
     *
     * @param template     the template text containing placeholder tokens
     * @param placeholders map of keys to values to substitute
     * @return the template with all placeholders replaced
     */
    private String fillPromptTemplate(String template, Map<String, Object> placeholders) {
        String result = template;
        for (Map.Entry<String, Object> entry : placeholders.entrySet()) {
            String key = "{{" + entry.getKey() + "}}";
            String value;

            if (entry.getValue() instanceof String s) {
                value = s;
            } else {
                try {
                    value = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(entry.getValue());
                } catch (JsonProcessingException e) {
                    value = "Error serializing data";
                }
            }
            result = result.replace(key, value);
        }
        return result;
    }
}
