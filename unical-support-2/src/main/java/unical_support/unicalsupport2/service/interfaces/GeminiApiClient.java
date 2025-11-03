package unical_support.unicalsupport2.service.interfaces;

public interface GeminiApiClient {
    String chat(String systemMessage, String userMessage) throws Exception;
}