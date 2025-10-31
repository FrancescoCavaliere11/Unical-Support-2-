package unical_support.unicalsupport2.EmailClassifier.Prompting;

import org.springframework.stereotype.Component;
import unical_support.unicalsupport2.EmailClassifier.Model.EmailData;

@Component
public class Prompter {

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

    public String buildUserMessage(EmailData email) {
        return """
            Email da classificare.

            OGGETTO:
            %s

            CORPO:
            %s
            """.formatted(email.getSubject(), email.getBody());
    }
}
