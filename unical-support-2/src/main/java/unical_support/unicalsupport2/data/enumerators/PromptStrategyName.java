package unical_support.unicalsupport2.data.enumerators;

import lombok.Getter;

@Getter
public enum PromptStrategyName {
    FEW_SHOT("fewShot"),
    ZERO_SHOT_CHAIN_OF_THOUGHT("zeroShotCoT");
    //TODO inserire i nomi delle strategy man mano che vengono introdotte

    private final String beanName;

    PromptStrategyName(String beanName) {
        this.beanName = beanName;
    }
}
