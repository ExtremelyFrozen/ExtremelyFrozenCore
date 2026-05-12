package com.extfro.extfrocore.integration.jei.orevein;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.extfro.extfrocore.api.registry.GTRegistries;
import com.extfro.extfrocore.common.data.GTItems;
import com.extfro.extfrocore.common.data.GTMaterials;
import com.extfro.extfrocore.integration.xei.widgets.GTOreVeinWidget;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.integration.xei.jei.ModularUIRecipeCategory;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class GTBedrockFluidInfoCategory extends ModularUIRecipeCategory<Holder<BedrockFluidDefinition>> {

    public final static RecipeType<Holder<BedrockFluidDefinition>> RECIPE_TYPE = new RecipeType(
            ExtForCore.id("bedrock_fluid_diagram"), Holder.class);
    private final IDrawable background;
    private final IDrawable icon;

    public GTBedrockFluidInfoCategory(IJeiHelpers helpers) {
        super(fluid -> ModularUI.of(UI.of(new GTOreVeinWidget(fluid, null))));
        IGuiHelper guiHelper = helpers.getGuiHelper();
        this.background = guiHelper.createBlankDrawable(GTOreVeinWidget.width, 120);
        this.icon = helpers.getGuiHelper()
                .createDrawableItemStack(GTMaterials.Oil.getBucket().getDefaultInstance());
    }

    public static void registerRecipes(IRecipeRegistration registry) {
        var fluids = Minecraft.getInstance().level.registryAccess()
                .registryOrThrow(GTRegistries.BEDROCK_FLUID_REGISTRY);
        registry.addRecipes(RECIPE_TYPE, fluids.holders()
                .filter(fluid -> fluid.value().canGenerate())
                .<Holder<BedrockFluidDefinition>>map(Function.identity())
                .toList());
    }

    public static void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(GTItems.PROSPECTOR_HV.asStack(), RECIPE_TYPE);
        registration.addRecipeCatalyst(GTItems.PROSPECTOR_LuV.asStack(), RECIPE_TYPE);
    }

    @NotNull
    public RecipeType<Holder<BedrockFluidDefinition>> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public @NotNull IDrawable getBackground() {
        return background;
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return icon;
    }

    @NotNull
    @Override
    public Component getTitle() {
        return Component.translatable("gtceu.jei.bedrock_fluid_diagram");
    }

    @Override
    public int getWidth() {
        return GTOreVeinWidget.width;
    }

    @Override
    public int getHeight() {
        return 140;
    }
}
