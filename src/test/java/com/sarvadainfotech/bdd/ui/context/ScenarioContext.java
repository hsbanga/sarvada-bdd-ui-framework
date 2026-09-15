package com.sarvadainfotech.bdd.ui.context;

import java.util.HashMap;
import java.util.Map;

/**
 * Per-scenario shared state. One instance is created by PicoContainer for every scenario and injected
 * into any step-definition class that declares it as a constructor argument.
 */
public class ScenarioContext {

    private final Map<String, Object> store = new HashMap<>();

    public void put(String key, Object value) {
        store.put(key, value);
    }

    public <T> T get(String key, Class<T> type) {
        return type.cast(store.get(key));
    }

    public boolean contains(String key) {
        return store.containsKey(key);
    }

    public void clear() {
        store.clear();
    }
}
