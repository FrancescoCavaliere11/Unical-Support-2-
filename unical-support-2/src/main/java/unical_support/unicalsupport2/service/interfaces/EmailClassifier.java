package unical_support.unicalsupport2.service.interfaces;

import unical_support.unicalsupport2.data.dto.ClassificationResultDto;
import unical_support.unicalsupport2.data.dto.ClassificationEmailDto;

import java.util.List;

public interface EmailClassifier {
    List<ClassificationResultDto> classifyEmail(List<ClassificationEmailDto> classificationEmailDtos);
}