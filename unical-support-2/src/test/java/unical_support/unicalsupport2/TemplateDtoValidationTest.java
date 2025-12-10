package unical_support.unicalsupport2;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import unical_support.unicalsupport2.data.dto.template.ParameterDto;
import unical_support.unicalsupport2.data.dto.template.TemplateCreateDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TemplateDtoValidationTest {

    private Validator validator;
    private ValidatorFactory validatorFactory;

    @BeforeEach
    void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterEach
    void tearDown() {
        if (validatorFactory != null) {
            validatorFactory.close();
        }
    }

    private TemplateCreateDto createValidTemplate() {
        TemplateCreateDto dto = new TemplateCreateDto();
        dto.setName("Valid Template Name");
        dto.setCategoryId("2560a7fd-bcdd-4ec3-b13a-a4a2cf4d31f0");
        dto.setContent("Hello {{user_name}}, welcome to {{service_name}}");

        List<ParameterDto> params = new ArrayList<>();
        params.add(createParam("user_name"));
        params.add(createParam("service_name"));

        dto.setParameters(params);
        return dto;
    }

    private ParameterDto createParam(String name) {
        ParameterDto param = new ParameterDto();
        param.setName(name);
        param.setRequired(true);
        return param;
    }

    @Test
    @DisplayName("Should pass when placeholders match parameters exactly")
    void testValidParameters_HappyPath() {
        TemplateCreateDto dto = createValidTemplate();

        Set<ConstraintViolation<TemplateCreateDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(), "Expected no violations for valid matching parameters");
    }

    @Test
    @DisplayName("Should fail when a parameter is defined in list but missing in content")
    void testValidParameters_ExtraParameterInList() {
        TemplateCreateDto dto = createValidTemplate();
        // Aggiungo un parametro alla lista che NON c'è nel content
        dto.getParameters().add(createParam("extra_param"));

        Set<ConstraintViolation<TemplateCreateDto>> violations = validator.validate(dto);

        // Mi aspetto 1 errore (quello della tua custom annotation sulla classe)
        assertEquals(1, violations.size());
        // Nota: Il messaggio dipenderà da quello definito nella tua annotation @ValidParameters
    }

    @Test
    @DisplayName("Should fail when a placeholder exists in content but missing in list")
    void testValidParameters_MissingParameterInList() {
        TemplateCreateDto dto = createValidTemplate();
        // Rimuovo un parametro dalla lista che però è presente nel content
        dto.getParameters().removeFirst();

        Set<ConstraintViolation<TemplateCreateDto>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
    }

    @Test
    @DisplayName("Should fail when parameter names mismatch")
    void testValidParameters_MismatchName() {
        TemplateCreateDto dto = createValidTemplate();
        dto.setContent("Hello {{wrong_name}}");

        List<ParameterDto> params = new ArrayList<>();
        params.add(createParam("correct_name")); // Size è 1 e 1, ma i nomi sono diversi
        dto.setParameters(params);

        Set<ConstraintViolation<TemplateCreateDto>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
    }

    @Test
    @DisplayName("Should handle spaces inside placeholders correctly (trimming)")
    void testValidParameters_TrimmingSpaces() {
        TemplateCreateDto dto = createValidTemplate();
        // Nel content metto spazi: {{ user_name }}
        dto.setContent("Hello {{ user_name }}");

        List<ParameterDto> params = new ArrayList<>();
        params.add(createParam("user_name")); // Nel DTO è senza spazi
        dto.setParameters(params);

        Set<ConstraintViolation<TemplateCreateDto>> violations = validator.validate(dto);

        // Dovrebbe passare perché nel tuo validatore fai matcher.group(1).trim()
        assertTrue(violations.isEmpty(), "Should trim spaces inside curly braces");
    }

    // ==========================================
    // TEST 2: @ValidParameterName (Formato Snake Case)
    // ==========================================

    @Test
    @DisplayName("Should pass for valid snake_case parameter names")
    void testParameterName_Valid() {
        ParameterDto param = createParam("valid_snake_case");

        Set<ConstraintViolation<ParameterDto>> violations = validator.validate(param);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Should fail for parameter name with spaces")
    void testParameterName_WithSpaces() {
        ParameterDto param = createParam("invalid name with spaces");

        Set<ConstraintViolation<ParameterDto>> violations = validator.validate(param);

        assertEquals(1, violations.size());
        assertEquals("Parameter name must be in snake_case (e.g., 'user_question')",
                violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Should fail for CamelCase")
    void testParameterName_CamelCase() {
        ParameterDto param = createParam("camelCaseName");

        Set<ConstraintViolation<ParameterDto>> violations = validator.validate(param);

        assertEquals(1, violations.size());
    }

    @Test
    @DisplayName("Should fail for Uppercase")
    void testParameterName_UpperCase() {
        ParameterDto param = createParam("UPPER_CASE");

        Set<ConstraintViolation<ParameterDto>> violations = validator.validate(param);

        assertEquals(1, violations.size());
    }
}
