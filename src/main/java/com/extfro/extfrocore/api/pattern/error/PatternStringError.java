package com.extfro.extfrocore.api.pattern.error;

import net.minecraft.network.chat.Component;

public class PatternStringError extends PatternError {

    private final String translationKey;

    public PatternStringError(String translationKey) {
        this.translationKey = translationKey;
    }

    @Override
    public Component getErrorInfo() {
        return Component.translatable(translationKey);
    }
}
