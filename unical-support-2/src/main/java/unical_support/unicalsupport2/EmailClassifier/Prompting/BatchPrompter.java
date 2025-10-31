package unical_support.unicalsupport2.EmailClassifier.Prompting;

import org.springframework.stereotype.Component;
import unical_support.unicalsupport2.EmailClassifier.Model.EmailData;

import java.util.List;

@Component
public class BatchPrompter {

    public String buildSystemMessage() {
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
        // NOTA: ho uniformato "RICONOSCIMENTO_ESAMI_CFU" (nel tuo batch c'era un refuso: "RICONOSCMENTO")
    }

    public String buildUserMessage(List<EmailData> emails) {
        StringBuilder sb = new StringBuilder();
        sb.append("Classifica le seguenti email. Rispondi con un JSON array come descritto.\n\n");
        for (int i = 0; i < emails.size(); i++) {
            EmailData e = emails.get(i);
            sb.append("ID: ").append(i).append("\n");
            sb.append("OGGETTO: ").append(e.getSubject()).append("\n");
            sb.append("CORPO: ").append(e.getBody()).append("\n\n");
        }
        return sb.toString();
    }
}