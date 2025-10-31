package unical_support.unicalsupport2.EmailClassifier.Config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

// todo rimuovere

// Classe di configurazione per leggere le proprietà LLM da application.yml
@Component
@ConfigurationProperties(prefix = "llm")
@Data
public class LlmProperties {
    private String apiKey;
    private String model = "gemini-2.5-flash";
    private int timeoutSeconds = 50;


}
