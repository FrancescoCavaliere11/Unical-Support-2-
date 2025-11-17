package unical_support.unicalsupport2.service.interfaces;

import unical_support.unicalsupport2.data.dto.classifier.ClassificationEmailDto;
import unical_support.unicalsupport2.data.dto.classifier.ClassificationResultDto;

import java.util.List;

public interface EmailClassifier {
    List<ClassificationResultDto> classifyEmail(List<ClassificationEmailDto> classificationEmailDtos);
}