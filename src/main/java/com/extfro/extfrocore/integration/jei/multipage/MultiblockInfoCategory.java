package com.extfro.extfrocore.integration.jei.multipage;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.gui.widget.PatternPreviewWidget;
import com.extfro.extfrocore.api.machine.MultiblockMachineDefinition;
import com.extfro.extfrocore.api.registry.GTRegistries;
import com.extfro.extfrocore.common.data.machines.GTMultiMachines;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.integration.xei.jei.ModularUIRecipeCategory;
import lombok.Getter;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeRegistration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MultiblockInfoCategory extends ModularUIRecipeCategory<MultiblockMachineDefinition> {

    public final static RecipeType<MultiblockMachineDefinition> RECIPE_TYPE = new RecipeType<>(
            ExtForCore.id("multiblock_info"),
            MultiblockMachineDefinition.class);
    @Getter
    private final IDrawable background;
    @Getter
    private final IDrawable icon;

    public MultiblockInfoCategory(IJeiHelpers helpers) {
        super(definition -> ModularUI.of(UI.of(PatternPreviewWidget.getPatternWidget(definition))));
        IGuiHelper guiHelper = helpers.getGuiHelper();
        this.background = guiHelper.createBlankDrawable(160, 160);
        this.icon = helpers.getGuiHelper().createDrawableItemStack(GTMultiMachines.ELECTRIC_BLAST_FURNACE.asStack());
    }

    public static void registerRecipes(IRecipeRegistration registry) {
        registry.addRecipes(RECIPE_TYPE, GTRegistries.MACHINES.stream()
                .filter(MultiblockMachineDefinition.class::isInstance)
                .map(MultiblockMachineDefinition.class::cast)
                .filter(MultiblockMachineDefinition::isRenderXEIPreview)
                .toList());
    }

    @Override
    public @Nullable ResourceLocation getRegistryName(@NotNull MultiblockMachineDefinition recipe) {
        return recipe.getId();
    }

    @Override
    @NotNull
    public RecipeType<MultiblockMachineDefinition> getRecipeType() {
        return RECIPE_TYPE;
    }

    @NotNull
    @Override
    public Component getTitle() {
        return Component.translatable("gtceu.jei.multiblock_info");
    }

    @Override
    public int getWidth() {
        return 160;
    }

    @Override
    public int getHeight() {
        return 160;
    }
}
