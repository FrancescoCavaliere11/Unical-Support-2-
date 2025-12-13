package unical_support.unicalsupport2.data.enumerators;

import lombok.Getter;

@Getter
public enum ModuleName {
    CLASSIFIER("classifier"),
    JUDGER("judger"),
    EMBEDDER("embedder"),
    RESPONDER("responder");
    //TODO inserire i nomi dei moduli man mano che vengono introdotti

    ModuleName(String classifier) {}
}