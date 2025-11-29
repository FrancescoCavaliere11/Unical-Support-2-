package unical_support.unicalsupport2.prompting;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class PromptTemplateLoader {

    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public String loadTemplate(String module, String strategyKey) {
        String cacheKey = module + ":" + strategyKey;
        return cache.computeIfAbsent(cacheKey, key -> doLoad(module, strategyKey));
    }

    private String doLoad(String module, String strategyKey) {
        String filename = "prompts/" + module + "_" + strategyKey + ".txt";
        try (InputStream is = new ClassPathResource(filename).getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Errore nel caricamento del template {}", filename, e);
            throw new IllegalStateException("Impossibile caricare il template " + filename, e);
        }
    }
}
