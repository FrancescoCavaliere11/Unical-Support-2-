package unical_support.unicalsupport2.security.customAnnotations.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import unical_support.unicalsupport2.data.dto.template.ParameterDto;
import unical_support.unicalsupport2.data.dto.template.TemplateAbstractDto;
import unical_support.unicalsupport2.security.customAnnotations.annotation.ValidParameters;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParametersValidator implements ConstraintValidator<ValidParameters, TemplateAbstractDto> {

    @Override
    public boolean isValid(TemplateAbstractDto value, ConstraintValidatorContext context) {
        Pattern pattern = Pattern.compile("\\{\\{(.*?)}}");
        Matcher matcher = pattern.matcher(value.getContent());

        Set<String> placeholders = new HashSet<>();
        while (matcher.find()) {
            placeholders.add(matcher.group(1).trim());
        }

        if (placeholders.size() != value.getParameters().size()) {
            return false;
        }

        for (ParameterDto p : value.getParameters()) {
            if (!placeholders.contains(p.getName())) {
                return false;
            }
        }

        return true;
    }
}
