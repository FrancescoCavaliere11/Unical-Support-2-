package unical_support.unicalsupport2.service.interfaces;

import unical_support.unicalsupport2.data.dto.classifier.ClassificationEmailDto;
import unical_support.unicalsupport2.data.dto.classifier.ClassificationResultDto;
import unical_support.unicalsupport2.data.dto.JudgementResultDto;

import java.util.List;

/**
 * Esegue il giudizio sulle classificazioni proposte dal classifier.
 * Prende batch di email + risultati del classifier e ritorna un giudizio per ciascuna email.
 */
public interface JudgerService {
    List<JudgementResultDto> judge(List<ClassificationEmailDto> emails,
                                   List<ClassificationResultDto> results);
}
