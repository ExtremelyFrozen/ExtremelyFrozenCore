package com.extfro.extfrocore.api.recipe;

import com.extfro.extfrocore.api.capability.recipe.IO;
import com.extfro.extfrocore.api.capability.recipe.RecipeCapability;

import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.Nullable;

public record ActionResult(boolean isSuccess, @Nullable Component reason, @Nullable RecipeCapability<?> capability,
                           @Nullable IO io) {

    public static final ActionResult SUCCESS = new ActionResult(true, null, null, null);
    public static final ActionResult FAIL_NO_REASON = new ActionResult(false, null, null, null);
    public static final ActionResult PASS_NO_CONTENTS = new ActionResult(true,
            Component.translatable("extfrocore.recipe_logic.no_contents"), null, null);
    public static final ActionResult FAIL_NO_CAPABILITIES = new ActionResult(false,
            Component.translatable("extfrocore.recipe_logic.no_capabilities"), null, null);

    public static ActionResult fail(@Nullable Component component, @Nullable RecipeCapability<?> capability,
                                    @Nullable IO io) {
        return new ActionResult(false, component, capability, io);
    }

    @Override
    public Component reason() {
        return reason == null ? Component.empty() : reason;
    }
}
