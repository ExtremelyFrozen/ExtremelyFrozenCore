package com.extfro.extfrocore.integration.jei;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.integration.jei.category.XEIJeiCategories;

import net.minecraft.resources.ResourceLocation;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import org.jetbrains.annotations.NotNull;

@JeiPlugin
public class EFJEIPlugin implements IModPlugin {

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ExtForCore.id("jei_plugin");
    }

    @Override
    public void registerCategories(@NotNull IRecipeCategoryRegistration registration) {
        if (ExtForCore.Mods.isJEILoaded()) {
            XEIJeiCategories.registerCategories(registration);
        }
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        if (ExtForCore.Mods.isJEILoaded()) {
            XEIJeiCategories.registerRecipes(registration);
        }
    }

    @Override
    public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
        if (ExtForCore.Mods.isJEILoaded()) {
            XEIJeiCategories.registerRecipeCatalysts(registration);
        }
    }
}
