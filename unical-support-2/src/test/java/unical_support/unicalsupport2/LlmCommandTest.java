package unical_support.unicalsupport2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import unical_support.unicalsupport2.commands.LlmCommand;
import unical_support.unicalsupport2.configurations.factory.LlmProperties;
import unical_support.unicalsupport2.configurations.factory.LlmStrategyFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LlmCommandTest {

    @Mock
    private LlmProperties llmProperties;

    @Mock
    private LlmStrategyFactory llmStrategyFactory;

    @InjectMocks
    private LlmCommand llmCommand;

    private Map<String, String> modulesMap;

    @BeforeEach
    void setUp() {
        modulesMap = new HashMap<>();
        modulesMap.put("classifier", "gemini");
        modulesMap.put("judger", "gemini");
    }

    @Test
    @DisplayName("Set Provider: Successo quando modulo e provider sono validi")
    void setProvider_validInputs_updatesConfig() {
        when(llmProperties.getModules()).thenReturn(modulesMap);
        when(llmStrategyFactory.getAvailableProviders()).thenReturn(Set.of("gemini", "groq"));
        lenient().when(llmProperties.getDefaultProvider()).thenReturn("gemini");

        String result = llmCommand.setProvider("classifier", "groq");

        assertTrue(result.contains("SUCCESSO"), "Dovrebbe restituire un messaggio di successo");
        assertEquals("groq", modulesMap.get("classifier"), "La mappa dovrebbe essere aggiornata con il nuovo provider");
    }

    @Test
    @DisplayName("Set Provider: Errore se il provider non esiste")
    void setProvider_invalidProvider_returnsError() {
        when(llmProperties.getModules()).thenReturn(modulesMap);
        when(llmStrategyFactory.getAvailableProviders()).thenReturn(Set.of("gemini"));

        String result = llmCommand.setProvider("classifier", "openai");

        assertTrue(result.toLowerCase().contains("errore"));
        assertTrue(result.contains("Provider 'openai' non disponibile"));

        assertEquals("gemini", modulesMap.get("classifier"));
    }

    @Test
    @DisplayName("Set Provider: Errore se il modulo non esiste")
    void setProvider_invalidModule_returnsError() {
        when(llmProperties.getModules()).thenReturn(modulesMap);

        String result = llmCommand.setProvider("modulo_inesistente", "groq");

        assertTrue(result.toLowerCase().contains("errore"));
        assertTrue(result.contains("non trovato"));
    }
}