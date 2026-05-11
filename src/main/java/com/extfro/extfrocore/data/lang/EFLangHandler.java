package com.extfro.extfrocore.data.lang;

import com.tterrag.registrate.providers.RegistrateLangProvider;

import java.util.ArrayList;
import java.util.List;

public final class EFLangHandler {

    private static final List<LangEntry> ENTRIES = new ArrayList<>();

    private EFLangHandler() {
    }

    public static void init(RegistrateLangProvider provider) {
        for (LangEntry entry : ENTRIES) {
            provider.add(entry.key(), entry.value());
        }
    }

    public static void add(String key, String value) {
        ENTRIES.add(new LangEntry(key, value));
    }

    public static void multiLang(RegistrateLangProvider provider, String key, String... values) {
        for (int i = 0; i < values.length; i++) {
            provider.add(key + "." + i, values[i]);
        }
    }

    public static void multilineLang(RegistrateLangProvider provider, String key, String multiline) {
        multiLang(provider, key, multiline.split("\\n"));
    }

    private record LangEntry(String key, String value) {
    }
}
