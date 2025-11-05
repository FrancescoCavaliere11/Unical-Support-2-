package unical_support.unicalsupport2.service.interfaces;

public interface LlmClient {
    String chat(String systemMessage, String userMessage) throws Exception;
}
