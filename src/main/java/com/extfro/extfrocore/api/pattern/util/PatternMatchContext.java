package com.extfro.extfrocore.api.pattern.util;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PatternMatchContext {

    private final Map<String, Object> data = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T> T getOrCreate(String key, Supplier<T> supplier) {
        return (T) data.computeIfAbsent(key, ignored -> supplier.get());
    }

    @SuppressWarnings("unchecked")
    public <T> @Nullable T get(String key) {
        return (T) data.get(key);
    }

    public void put(String key, Object value) {
        data.put(key, value);
    }

    public void reset() {
        data.clear();
    }
}
