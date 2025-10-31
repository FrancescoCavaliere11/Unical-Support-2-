package unical_support.unicalsupport2.service.interfaces;

import unical_support.unicalsupport2.EmailClassifier.Model.ClassificationResult;
import unical_support.unicalsupport2.data.dto.ClassificationEmailDto;

import java.util.List;

public interface EmailClassifier {

    ClassificationResult classifyEmail(ClassificationEmailDto classificationEmailDto);

    List<ClassificationResult> classifyEmailBatch(List<ClassificationEmailDto> classificationEmailDtos);
}
