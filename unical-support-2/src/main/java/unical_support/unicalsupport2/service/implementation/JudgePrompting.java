package unical_support.unicalsupport2.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import unical_support.unicalsupport2.data.dto.classifier.ClassificationEmailDto;
import unical_support.unicalsupport2.data.dto.classifier.ClassificationResultDto;
import unical_support.unicalsupport2.data.dto.JudgementResultDto;
import unical_support.unicalsupport2.service.interfaces.JudgePromptService;

import java.util.ArrayList;
import java.util.List;

@Service("judgePrompting")
@RequiredArgsConstructor
public class JudgePrompting implements JudgePromptService {

    @Override
    public List<JudgementResultDto> buildJudgePrompt(List<ClassificationEmailDto> emails, List<ClassificationResultDto> results) {

        List<JudgementResultDto> judgementResults = new ArrayList<>();

        for (int i = 0; i < emails.size(); i++) {
            ClassificationEmailDto email = emails.get(i);
            ClassificationResultDto result = results.get(i);

            String formattedCategories = result.getCategories().toString();

            String prompt = String.format("""
                Agisci come un revisore esperto incaricato di valutare la classificazione prodotta da un primo modello di intelligenza artificiale.
                La tua responsabilità è giudicare l’affidabilità e la correttezza delle categorie assegnate, basandoti esclusivamente sul contenuto dell’email.

                CATEGORIE ASSEGNATE DAL MODELLO PRIMARIO: [%s]

                EMAIL DA VALUTARE:
                OGGETTO: %s
                CORPO: %s

                La tua analisi deve essere rigorosa e obiettiva. Esamina se ciascuna categoria proposta è coerente con il contenuto dell’email e valuta la sua affidabilità.
                Non suggerire nuove categorie e non rimuovere quelle esistenti: devi solo stimare quanto ciascuna categoria sia appropriata rispetto al contenuto.

                Rispondi esclusivamente in formato JSON, senza alcun testo aggiuntivo o spiegazione fuori dal JSON.
                La risposta deve seguire esattamente questa struttura:

                {
                  "categoriesEvaluation": [
                    {
                      "category": "nome_categoria",
                      "confidence": numero_compreso_tra_0_e_1,
                      "explanation": "valutazione formale e sintetica in lingua italiana che giustifichi il punteggio di affidabilità"
                    }
                  ],
                  "overallConfidence": numero_compreso_tra_0_e_1,
                  "summary": "sintesi formale e breve della valutazione complessiva"
                }
                """, formattedCategories, email.getSubject(), email.getBody());

            JudgementResultDto dto = new JudgementResultDto();

            dto.setCategoriesEvaluation(null);
            dto.setOverallConfidence(null);
            dto.setSummary(null);

            judgementResults.add(dto);

        }

        return judgementResults;
    }
}
