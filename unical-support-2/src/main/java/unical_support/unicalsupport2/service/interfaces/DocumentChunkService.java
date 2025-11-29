package unical_support.unicalsupport2.service.interfaces;

import unical_support.unicalsupport2.data.dto.classifier.ClassificationResultDto;
import unical_support.unicalsupport2.data.entities.DocumentChunk;

import java.util.List;

public interface DocumentChunkService {
    List<DocumentChunk> findRelevantChunks(ClassificationResultDto classificationResult, int kPerCategory);
}
