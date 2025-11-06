package unical_support.unicalsupport2.runtime;


import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Component;

@Component
public class ActiveLlmRegistry {
    private final AtomicReference<String> activeProvider = new AtomicReference<>(null);

    public String get() { return activeProvider.get(); }
    public void set(String provider) { activeProvider.set(provider); }
    public boolean isSet() { return activeProvider.get() != null && !activeProvider.get().isBlank(); }
}
