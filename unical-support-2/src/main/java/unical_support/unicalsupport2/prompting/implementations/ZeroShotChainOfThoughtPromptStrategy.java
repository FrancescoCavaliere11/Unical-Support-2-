package unical_support.unicalsupport2.prompting.implementations;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import unical_support.unicalsupport2.data.dto.ClassificationEmailDto;
import unical_support.unicalsupport2.data.dto.ClassificationResultDto;
import unical_support.unicalsupport2.data.repositories.CategoryRepository;
import unical_support.unicalsupport2.prompting.PromptStrategy;

import java.util.List;

@Component("zeroShotCoT")
@RequiredArgsConstructor
public class ZeroShotChainOfThoughtPromptStrategy implements PromptStrategy {

    private final CategoryRepository categoryRepository;
    private final ObjectMapper mapper = new ObjectMapper();


    @Override
    public String buildClassifyPrompt(List<ClassificationEmailDto> classificationEmailDtos) {
        List<String> categories = categoryRepository.findAll()
                .stream()
                .map(cat -> "name: \"" + cat.getName() + "\", description: \"" + cat.getDescription() + "\"")
                .toList();

        StringBuilder prompt = new StringBuilder();

        prompt.append("Sei un classificatore di email per una segreteria universitaria.\n")
                .append("Devi assegnare UNA o PIÙ categorie per ciascuna email tra le seguenti:\n")
                .append(String.join(", ", categories))
                .append("\n\n")
                .append("Ragiona passo per passo per ciascuna email:\n")
                .append("1. Analizza il contenuto e individua le parole chiave rilevanti.\n")
                .append("2. Rifletti sullo scopo del messaggio e sull'intento del mittente.\n")
                .append("3. Identifica le categorie più appropriate.\n")
                .append("4. Fornisci un livello di confidenza (0–1) e il testo giustificativo.\n")
                .append("5. Scrivi una breve spiegazione del tuo ragionamento.\n\n")
                .append("Rispondi SOLO con un JSON array nel seguente formato:\n")
                .append("[\n")
                .append("  {\n")
                .append("    \"id\": number,\n")
                .append("    \"categories\": [\n")
                .append("      { \"name\": string, \"confidence\": number, \"text\": string }\n")
                .append("    ],\n")
                .append("    \"explanation\": string\n")
                .append("  }\n")
                .append("]\n\n")
                .append("Non aggiungere testo fuori dal JSON.\n\n")
                .append("Analizza e classifica le seguenti email passo per passo SENZA AGGIUNGERE NE COMMENTI NE MARKDOWN :\n\n");

        for (int i = 0; i < classificationEmailDtos.size(); i++) {
            ClassificationEmailDto e = classificationEmailDtos.get(i);
            prompt.append("ID: ").append(i).append("\n")
                    .append("OGGETTO: ").append(e.getSubject()).append("\n")
                    .append("CORPO: ").append(e.getBody()).append("\n")
                    .append("Ragiona passo per passo e poi restituisci solo il JSON SENZA MARKDOWN O COMMENTI.\n\n");
        }

        return prompt.toString();
    }

    // ==========================
    // 2) PROMPT PER JUDGER
    // ==========================
    @Override
    public String buildJudgePrompt(List<ClassificationEmailDto> emails,
                                   List<ClassificationResultDto> results) {

        StringBuilder prompt = new StringBuilder();

        // ==== PARTE "SYSTEM" con istruzioni CoT ====
        prompt.append("""
            Sei un modello di revisione esperto che valuta classificazioni generate da un altro modello di intelligenza artificiale.

            Per ogni email ricevi:
            - OGGETTO e CORPO dell'email;
            - CATEGORIE_PROPOSTE: un array JSON di oggetti { "category", "confidence", "text" };
            - SPIEGAZIONE DEL CLASSIFICATORE.

            Per ogni email, segui mentalmente questi passi (senza scriverli esplicitamente in output):

            1. Analizza attentamente il contenuto dell'email (oggetto + corpo).
            2. Per ogni categoria proposta:
               - confronta il nome della categoria con il testo dell'email e con il testo estratto fornito;
               - valuta quanto il testo supporta davvero quella categoria.
            3. Se la categoria è chiaramente supportata -> assegna un punteggio vicino a 1.
               Se è parzialmente o debolmente supportata -> punteggio intermedio (circa 0.3–0.7).
               Se non è supportata o è fuorviante -> punteggio vicino a 0.
            4. In base a questa valutazione assegna anche un campo "verdict":
               - "CORRETTO" se la categoria è chiaramente supportata dal testo;
               - "AMBIGUO" se il supporto è parziale o dubbio;
               - "ERRATO" se la categoria non è supportata dal testo.
            5. Formula una breve spiegazione formale in italiano ("explanation") che giustifichi il punteggio.
            6. Calcola un "overallConfidence" complessivo per l'intera classificazione dell'email.
            7. Scrivi una breve "summary" che descriva in modo sintetico il tuo giudizio sull'intera email.

            IMPORTANTE: i tuoi ragionamenti passo per passo devono restare impliciti.
            NELL'OUTPUT devi restituire SOLO il risultato finale nel seguente formato JSON SENZA MARKDOWN O COMMENTI:

            [
              {
                "id": number,
                "categoriesEvaluation": [
                  {
                    "category": "string",
                    "confidence": number,
                    "explanation": "string",
                    "verdict": "CORRETTO" | "AMBIGUO" | "ERRATO"
                  }
                ],
                "overallConfidence": number,
                "summary": "string"
              }
            ]

            Regole:
            - Rispondi SOLO con il JSON array descritto.
            - NON aggiungere testo fuori dal JSON.
            - NON usare markdown, NON usare ```json, NON usare backtick.
            - Mantieni esattamente gli ID forniti in input.

            Ora valuta le seguenti email e relative classificazioni proposte
            (ragiona passo per passo ma stampa solo il JSON finale SENZA MARKDOWN O COMMENTI:).
            """);

        // ==== PARTE "USER": elenco email + classificazioni ====
        prompt.append("\n\nEMAIL E CLASSIFICAZIONI DA VALUTARE:\n\n");

        for (int i = 0; i < emails.size(); i++) {
            ClassificationEmailDto email = emails.get(i);
            ClassificationResultDto classification = results.get(i);

            String categoriesJson;
            try {
                categoriesJson = mapper.writeValueAsString(classification.getCategories());
            } catch (Exception ex) {
                categoriesJson = "[]";
            }

            prompt.append("ID: ").append(i).append("\n")
                    .append("OGGETTO: ").append(ns(email.getSubject())).append("\n")
                    .append("CORPO: ").append(ns(email.getBody())).append("\n")
                    .append("CATEGORIE_PROPOSTE:\n")
                    .append(categoriesJson).append("\n")
                    .append("SPIEGAZIONE DEL CLASSIFICATORE:\n")
                    .append(ns(classification.getExplanation())).append("\n\n");
        }

        prompt.append("Restituisci ORA esclusivamente il JSON array, senza testo esterno senza MARKDOWN O COMMENTI.");

        return prompt.toString();
    }

    private String ns(String s) {
        return s == null ? "" : s;
    }
}
