package unical_support.unicalsupport2.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import unical_support.unicalsupport2.configurations.LlmClientFactory;
import unical_support.unicalsupport2.service.interfaces.LlmClient;

@Service
@Primary
@RequiredArgsConstructor
public class LlmClientBridge implements LlmClient {

    private final LlmClientFactory factory;

    @Override
    public String chat(String systemMessage, String userMessage) throws Exception {
        LlmClient dynamic = factory.current();
        return dynamic.chat(systemMessage, userMessage);
    }
}
