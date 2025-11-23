package unical_support.unicalsupport2.runtime;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Registry thread-safe del provider LLM usato dal JUDGER.
 * Se null o vuoto, il Judger cade in fallback su LlmClientFactory.current().
 */
@Component
public class ActiveJudgerLlmRegistry {

    private final AtomicReference<String> activeProvider = new AtomicReference<>(null);


    public String get() {
        return activeProvider.get();
    }


    public void set(String provider) {
        activeProvider.set(provider == null ? null : provider.trim().toLowerCase());
    }


    public void clear() {
        activeProvider.set(null);
    }


    public boolean isSet() {
        String p = activeProvider.get();
        return p != null && !p.isBlank();
    }
}
