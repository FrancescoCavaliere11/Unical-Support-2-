package unical_support.unicalsupport2.service.interfaces;

import unical_support.unicalsupport2.data.dto.email.SingleEmailRequestDto;
import unical_support.unicalsupport2.data.dto.email.SingleEmailResponseDto;

public interface OrchestratorService {
    void start(boolean sequentialMode);
    SingleEmailResponseDto processSingleEmail(SingleEmailRequestDto request);
}
