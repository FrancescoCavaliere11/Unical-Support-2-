package unical_support.unicalsupport2.runtime;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

@Component
public class ActiveJudgerPromptStrategyRegistry {

    private final AtomicReference<String> strategy = new AtomicReference<>("few"); // default

    public String get() {
        return strategy.get();
    }

    public void set(String s) {
        strategy.set(s == null ? "few" : s.trim().toLowerCase());
    }

    public void clear() {
        strategy.set("few");
    }
}
