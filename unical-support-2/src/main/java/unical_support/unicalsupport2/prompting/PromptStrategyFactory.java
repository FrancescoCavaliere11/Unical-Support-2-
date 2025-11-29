package unical_support.unicalsupport2.prompting;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;
import unical_support.unicalsupport2.data.enumerators.PromptStrategyName;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PromptStrategyFactory {

    private final Map<String, PromptStrategy> strategies;

    private Map<String, Object> config;

    @PostConstruct
    void loadConfig() {
        try {
            Yaml yaml = new Yaml();
            InputStream is = new ClassPathResource("config.yaml").getInputStream();
            config = yaml.load(is);
            config.forEach((s, o) -> System.out.println(s + ": " + o));
        } catch (Exception e) {
            throw new RuntimeException("Errore nel caricamento di config.yaml", e);
        }
    }

    private String getDefaultForModule(String module) {
        if (config == null) return null;

        Map<String, Object> prompt = (Map<String, Object>) config.get("prompt");
        if (prompt == null) {
            System.err.println("Configurazione mancante: chiave 'prompt' non trovata.");
            return null;
        }

        Map<String, Object> modules = (Map<String, Object>) prompt.get("modules");
        if (modules == null) {
            System.err.println("Configurazione mancante: chiave 'modules' non trovata.");
            return null;
        }

        Map<String, Object> mod = (Map<String, Object>) modules.get(module);
        if (mod == null) {
            return null;
        }

        return (String) mod.get("default");
    }


    public PromptStrategy getStrategy(String module, String strategyName) {
        if (strategyName != null && strategies.containsKey(strategyName)) {
            return strategies.get(strategyName);
        }

        String moduleDefault = getDefaultForModule(module);
        if (moduleDefault != null && strategies.containsKey(moduleDefault)) {
            System.out.println("Strategia trovata da config per " + module + ": " + moduleDefault);
            return strategies.get(moduleDefault);
        }

        System.out.println("Nessuna strategia trovata. Fallback su default globale.");
        String fallback = PromptStrategyName.FEW_SHOT.getBeanName();
        return strategies.getOrDefault(fallback, strategies.values().stream().findFirst().orElse(null));
    }

    public PromptStrategy getStrategy(String strategyName) {
        return getStrategy("classifier", strategyName);
    }
}
