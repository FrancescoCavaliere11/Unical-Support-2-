package unical_support.unicalsupport2.service.implementation;

import unical_support.unicalsupport2.service.interfaces.GeminiApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import unical_support.unicalsupport2.service.interfaces.LlmClient;


@Service
@Primary
@RequiredArgsConstructor
public class LlmClientBridge implements GeminiApiClient {

    private final LlmClient llmClient;

    @Override
    public String chat(String systemMessage, String userMessage) throws Exception {
        return llmClient.chat(systemMessage, userMessage);
    }
}
