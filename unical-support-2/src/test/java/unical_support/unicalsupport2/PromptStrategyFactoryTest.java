package unical_support.unicalsupport2;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import unical_support.unicalsupport2.configurations.factory.PromptProperties;
import unical_support.unicalsupport2.configurations.factory.PromptStrategyFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromptStrategyFactoryTest {

    @Mock
    private ResourceLoader resourceLoader;

    @Mock
    private Resource mockResource;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private PromptStrategyFactory factory;

    private PromptProperties promptProperties;

    @BeforeEach
    void setup() {
        promptProperties = new PromptProperties();
        Map<String, PromptProperties.ModuleConfig> modules = new HashMap<>();

        PromptProperties.ModuleConfig testModuleConfig = new PromptProperties.ModuleConfig();
        testModuleConfig.setDefaultStrategy("simple");
        testModuleConfig.setStrategies(Map.of(
                "simple", "classpath:prompts/test_simple.txt",
                "complex", "classpath:prompts/test_complex.txt"
        ));
        modules.put("testModule", testModuleConfig);

        promptProperties.setModules(modules);

        factory = new PromptStrategyFactory(promptProperties, resourceLoader, objectMapper);
    }

    @Test
    @DisplayName("Deve caricare il template corretto e sostituire i placeholder stringa")
    void generate_simpleReplacement() throws IOException {
        String path = "classpath:prompts/test_simple.txt";
        String templateContent = "Ciao {{NAME}}, benvenuto!";
        mockResourceContent(path, templateContent);

        Map<String, Object> placeholders = Map.of("NAME", "Mario");

        String result = factory.generate("testModule", placeholders);

        assertThat(result).isEqualTo("Ciao Mario, benvenuto!");
    }

    @Test
    @DisplayName("Deve usare la strategia di DEFAULT se strategyName è null")
    void generate_useDefaultStrategy() throws IOException {
        String path = "classpath:prompts/test_simple.txt";
        String templateContent = "Default Template: {{VAL}}";
        mockResourceContent(path, templateContent);

        Map<String, Object> placeholders = Map.of("VAL", "123");

        String result = factory.generate("testModule", placeholders);

        assertThat(result).isEqualTo("Default Template: 123");
    }

    @Test
    @DisplayName("Deve serializzare oggetti complessi in JSON quando inseriti nel template")
    void generate_complexJsonSerialization() throws IOException {
        promptProperties.getModules().get("testModule").setDefaultStrategy("complex");

        String path = "classpath:prompts/test_complex.txt";
        String templateContent = "Dati: {{DATA}}";
        mockResourceContent(path, templateContent);

        List<Map<String, String>> complexData = List.of(
                Map.of("id", "1", "val", "A"),
                Map.of("id", "2", "val", "B")
        );
        Map<String, Object> placeholders = Map.of("DATA", complexData);

        String result = factory.generate("testModule", placeholders);

        assertThat(result).contains("Dati:");
        assertThat(result).contains("\"id\" : \"1\"");
        assertThat(result).contains("\"val\" : \"A\"");
    }

    @Test
    @DisplayName("Deve lanciare eccezione se il modulo non esiste")
    void generate_throwsOnMissingModule() {
        assertThatThrownBy(() ->
                factory.generate("moduloInesistente", Map.of())
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Modulo non configurato");
    }

    private void mockResourceContent(String path, String content) throws IOException {
        when(resourceLoader.getResource(path)).thenReturn(mockResource);
        when(mockResource.getInputStream()).thenReturn(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
    }
}