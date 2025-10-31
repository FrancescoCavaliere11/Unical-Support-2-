package unical_support.unicalsupport2.service.implementation;

import org.springframework.stereotype.Service;
import unical_support.unicalsupport2.data.dto.ClassificationEmailDto;
import unical_support.unicalsupport2.service.interfaces.PromptService;

import java.util.List;

@Service
public class PromptServiceImpl implements PromptService {
    @Override
    public String buildSystemMessage() {
        return """
            Sei un classificatore di email per una segreteria universitaria.
            Devi restituire SOLO un oggetto JSON valido con questo schema esatto:
            {
              "category": "INFORMAZIONI_GENERICHE" | "RECLAMO" | "NON_RICONOSCIUTA" |
                          "ERASMUS_E_MOBILITA_INTERNAZIONALE" | "RICONOSCIMENTO_ESAMI_CFU" |
                          "SERVIZI_CAMPUS" | "LAUREA_E_TESI" | "ESAMI_E_APPELLI" | "RICETTA_CULINARIA",
              "confidence": number,   // tra 0 e 1
              "explanation": string   // breve spiegazione in italiano
            }

            Regole:
            - Scegli UNA SOLA categoria tra quelle elencate.
            - Usa "NON_RICONOSCIUTA" se il contenuto è ambiguo, fuori dominio, o insufficiente.
            - La confidence deve essere in [0,1]. Usa valori alti (>=0.9) solo con evidenza chiara.
            - Non aggiungere testo fuori dal JSON. Non usare markdown, code fences o preamboli.
            """;
    }

    @Override
    public String buildUserMessage(ClassificationEmailDto classificationEmailDto) {
        return """
            Email da classificare.

            OGGETTO:
            %s

            CORPO:
            %s
            """.formatted(classificationEmailDto.getSubject(), classificationEmailDto.getBody());
    }

    @Override
    public String buildSystemMessageBatch() {
        return """
        Sei un classificatore di email per una segreteria universitaria.
        Restituisci SOLO un JSON array, senza testo extra, senza markdown.
        Ogni elemento dell'array deve avere questo schema:
        {
          "id": number,                                  // ID dell'email fornito nell'input
          "category": "INFORMAZIONI_GENERICHE" | "RECLAMO" | "NON_RICONOSCIUTA" | "ERASMUS_E_MOBILITA_INTERNAZIONALE" |
                       "LAUREA_E_TESI" | "SERVIZI_CAMPUS" | "ESAMI_E_APPELLI" | "RICONOSCIMENTO_ESAMI_CFU",
          "confidence": number,                          // 0..1
          "explanation": string                          // breve spiegazione in italiano
        }
        Non aggiungere testo fuori dal JSON.
        """;
        // NOTA: ho uniformato "RICONOSCIMENTO_ESAMI_xCFU" (nel tuo batch c'era un refuso: "RICONOSCMENTO")
    }

    @Override
    public String buildUserMessageBatch(List<ClassificationEmailDto> classificationEmailDtos) {
        StringBuilder sb = new StringBuilder();
        sb.append("Classifica le seguenti email. Rispondi con un JSON array come descritto.\n\n");
        for (int i = 0; i < classificationEmailDtos.size(); i++) {
            ClassificationEmailDto e = classificationEmailDtos.get(i);
            sb.append("ID: ").append(i).append("\n");
            sb.append("OGGETTO: ").append(e.getSubject()).append("\n");
            sb.append("CORPO: ").append(e.getBody()).append("\n\n");
        }
        return sb.toString();
    }
}
