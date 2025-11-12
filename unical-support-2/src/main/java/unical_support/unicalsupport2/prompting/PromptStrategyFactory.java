package unical_support.unicalsupport2.prompting;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PromptStrategyFactory {
    private final Map<String, PromptStrategy> strategies;

    public PromptStrategy getStrategy(String type) {
        return strategies.getOrDefault(type, strategies.get("fewShot"));
    }
}
