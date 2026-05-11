package com.extfro.extfrocore.integration.emi.category;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiRenderable;
import dev.emi.emi.api.stack.EmiStack;

final class NamedEmiRecipeCategory extends EmiRecipeCategory {

    private final Component name;

    NamedEmiRecipeCategory(ResourceLocation id, Component name, ItemStack icon) {
        this(id, name, EmiStack.of(icon));
    }

    NamedEmiRecipeCategory(ResourceLocation id, Component name, EmiRenderable icon) {
        super(id, icon);
        this.name = name;
    }

    @Override
    public Component getName() {
        return name;
    }
}
