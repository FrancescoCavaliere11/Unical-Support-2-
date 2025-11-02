package unical_support.unicalsupport2.data.dto;


import lombok.Getter;

// Classe in cui vengono definiti i nomi delle categorie possibili, e come deve essere formattato il risultato: categoria- confidenza- spiegazione
public class ClassificationResultDto {

    public enum Category {
        INFORMAZIONI_GENERICHE, RECLAMO, NON_RICONOSCIUTA, ERASMUS_E_MOBILITA_INTERNAZIONALE,
        RICONOSCIMENTO_ESAMI_CFU, ESAMI_E_APPELLI, LAUREA_E_TESI, SERVIZI_CAMPUS
    }

    private final Category category;
    private final double confidence;
    private final String explanation;

    public ClassificationResultDto(Category category, double confidence, String explanation) {
        this.category = category;
        this.confidence = confidence;
        this.explanation = explanation == null ? "" : explanation;
    }

    @Override
    public String toString() {
        return "Category=" + category + ", confidence=" + confidence + ", explanation=" + explanation;
    }
}