package unical_support.unicalsupport2.service.implementation;

import org.springframework.context.annotation.Lazy; // Importante
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import unical_support.unicalsupport2.configurations.LlmStrategyFactory;
import unical_support.unicalsupport2.service.interfaces.LlmClient;

@Service
@Primary
public class LlmClientBridge implements LlmClient {

    private final LlmStrategyFactory strategyFactory;


    public LlmClientBridge(@Lazy LlmStrategyFactory strategyFactory) {
        this.strategyFactory = strategyFactory;
    }

    @Override
    public String chat(String prompt) throws Exception {
        return strategyFactory.getLlmClient().chat(prompt);
    }

    @Override
    public float[] embed(String text) {
        return strategyFactory.getLlmClient().embed(text);
    }

    @Override
    public String getProviderName() {
        return strategyFactory.getLlmClient().getProviderName();
    }
}