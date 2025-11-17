package unical_support.unicalsupport2.prompting.implementations;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import unical_support.unicalsupport2.data.dto.classifier.ClassificationEmailDto;
import unical_support.unicalsupport2.data.dto.classifier.ClassificationResultDto;
import unical_support.unicalsupport2.data.dto.classifier.SingleCategoryDto;
import unical_support.unicalsupport2.data.entities.Template;
import unical_support.unicalsupport2.data.repositories.CategoryRepository;
import unical_support.unicalsupport2.data.repositories.TemplateRepository;
import unical_support.unicalsupport2.prompting.PromptStrategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("zeroShotCoT")
@RequiredArgsConstructor
public class ZeroShotChainOfThoughtPromptStrategy implements PromptStrategy {

    private final CategoryRepository categoryRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    private final TemplateRepository templateRepository;

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


    @Override
    public String buildResponderPrompt(List<ClassificationResultDto> emails) {
        StringBuilder sb = new StringBuilder();
        sb.append("""
            Sei un assistente AI che genera risposte email per una segreteria universitaria.
            
            OBIETTIVO
            Riceverai:
            - Una lista di email
            - Per ogni email: una lista di categorie già classificate (con confidence e testo estratto)
            - Una lista di template disponibili
            
            Per OGNI email:
            - Per OGNI categoria assegnata all'email:
              - Determina se esiste un template corrispondente
              - Se sì, compila il template sostituendo {{parametri}} usando il testo dell’email e/o della categoria
              - Se no, segnala che non esiste un template applicabile
             
            Ragiona passo passo su ogni risposta che dai:
            
            1. analizza il testo dell'email e il testo associato alla categoria per estrarre i parametri necessari
            2. seleziona il template più adatto per la categoria
            3. compila il template con i parametri estratti
            4. valuta se tutti i parametri richiesti sono stati compilati
            5. genera la risposta finale in formato JSON
            
            STRUTTURA OUTPUT OBBLIGATORIA
            Rispondi solo con un JSON array, in cui ogni elemento dell'array ha la seguente struttura. Rispondi nello stesso ordine in cui ti arrivano le email
            
            Formato di ogni elemento dell'array:
            
            {
              "email_id": number,
              "responses": [
                {
                  "category": "nome_categoria",
                  "template": "nome_template" | null,
                  "content": "testo compilato" | null,
                  "parameters": {
                    "param1": "valore" | null,
                    "param2": "valore" | null
                  },
                  "reason": "OK" | "NO_TEMPLATE_MATCH" | "MISSING_REQUIRED_PARAMETER"
                }
              ]
            }
            
            IL CAMPO email_id DEVE corrispondere all'id dell'email nell'input.
            Devi cercare di riempire tutti i parametri con required = true dei template, altrimenti metti a NULL il parametro e metti MISSING_REQUIRED_PARAMETER NEL CAMPO reason, seguito da tutti i parametri mancanti separati da virgola. Se riesci riempi anche i parametri non required.
            - Se un parametro NON required non può essere estratto:
              - nel campo "parameters" il valore deve essere null
              - nel campo "content" devi lasciare invariato il placeholder {{nome_parametro}}
            REGOLE IMPORTANTI
            - SEMPRE restituisci una risposta per ogni categoria dell'email
            - I parametri possono essere estratti:
              - dal testo della mail, oppure
              - dal testo associato alla categoria
            - NON inventare parametri che il template non contiene
            - Se un parametro richiesto non può essere estratto → mettilo a null
            - Non aggiungere testo fuori dal JSON
            - Nessun commento, nessun markdown
            - Ordine dell’output = ordine input email
            
            ESEMPI SEMANTICI (NON REALI)
            
            [
              {
                "email_id": 0,
                "responses": [
                  {
                    "category": "IMMATRICOLAZIONE",
                    "template": "RICHIESTA_IMMATRICOLAZIONE",
                    "content": "Gentile Mario, per immatricolarti devi...",
                    "parameters": { "nome": "Mario" },
                    "reason": "OK"
                  },
                  {
                    "category": "TASSE",
                    "template": null,
                    "content": null,
                    "parameters": {},
                    "reason": "NO_TEMPLATE_MATCH"
                  }
                ]
              }
            ]
            
            Fine istruzioni.
            """
        );

        Map<String, List<Template>> templatesCache = new HashMap<>();

        sb.append("Di seguito le email classificate e i template disponibili.\n");
        sb.append("Genera la risposta nel formato JSON richiesto.\n\n");

        for (ClassificationResultDto email : emails) {
            sb.append("EMAIL ID: ").append(email.getId()).append("\n");
            sb.append("CATEGORIE:\n");

            for (SingleCategoryDto category : email.getCategories()) {
                String categoryName = category.getCategory();

                List<Template> templates = templatesCache.computeIfAbsent(
                        categoryName,
                        k -> templateRepository.findByCategoryNameIgnoreCase(categoryName)
                );

                sb.append(" - Categoria: ").append(category.getCategory()).append("\n");
                sb.append("   Confidence: ").append(category.getConfidence()).append("\n");
                sb.append("   Testo: ").append(category.getText()).append("\n");

                if (!templates.isEmpty()) {
                    sb.append("   Template disponibili:\n");
                    for (Template t : templates) {
                        sb.append("     * Nome: ").append(t.getName()).append("\n");
                        sb.append("       Parametri: ").append(
                                t.getParameters().stream()
                                        .map(p -> p.getName() + " (required=" + p.isRequired() + ")")
                                        .toList()
                        ).append("\n");
                        sb.append("       Contenuto template: ").append(
                                t.getContent().replace("\n", " ").replace("\r", "")
                        ).append("\n");
                    }
                } else {
                    sb.append("   Template disponibili: nessuno\n");
                }
            }
            sb.append("\n");
        }

        sb.append("Genera ora SOLO l'array JSON di risposta, seguendo ESATTAMENTE il formato definito nel system prompt, senza testo aggiuntivo.\n");

        return sb.toString();
    }
}
