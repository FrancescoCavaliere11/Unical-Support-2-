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
        } catch (Exception e) {
            throw new RuntimeException("Errore nel caricamento di config.yaml", e);
        }
    }

    private String getDefaultForModule(String module) {
        Map<String, Object> modules = (Map<String, Object>) config.get("modules");
        Map<String, Object> mod = (Map<String, Object>) modules.get(module);
        return (String) mod.get("default");
    }


    public PromptStrategy getStrategy(String module, String strategyName) {

        // 1) strategy esplicita
        if (strategyName != null && strategies.containsKey(strategyName)) {
            return strategies.get(strategyName);
        }

        // 2) default da config.yaml
        String moduleDefault = getDefaultForModule(module);
        if (moduleDefault != null && strategies.containsKey(moduleDefault)) {
            return strategies.get(moduleDefault);
        }

        // 3) fallback fewShot
        String fallback = PromptStrategyName.FEW_SHOT.getBeanName();
        return strategies.getOrDefault(fallback, strategies.values().iterator().next());
    }

    public PromptStrategy getStrategy(String strategyName) {
        return getStrategy("classifier", strategyName);
    }
}
