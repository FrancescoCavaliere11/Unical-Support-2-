package unical_support.unicalsupport2.service.interfaces;

import unical_support.unicalsupport2.data.dto.classifier.ClassificationResultDto;
import unical_support.unicalsupport2.data.dto.responder.ResponderResultDto;

import java.util.List;

public interface EmailResponder {
    List<ResponderResultDto> generateEmailResponse(List<ClassificationResultDto> emails);
}
