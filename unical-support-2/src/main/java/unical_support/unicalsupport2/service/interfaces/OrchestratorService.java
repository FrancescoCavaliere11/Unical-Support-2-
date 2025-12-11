package unical_support.unicalsupport2.service.interfaces;

import unical_support.unicalsupport2.data.dto.responder.SingleEmailRequestDto;
import unical_support.unicalsupport2.data.dto.responder.SingleEmailResponseDto;

public interface OrchestratorService {
    void start(boolean sequentialMode);
    SingleEmailResponseDto processSingleEmail(SingleEmailRequestDto request);
}
