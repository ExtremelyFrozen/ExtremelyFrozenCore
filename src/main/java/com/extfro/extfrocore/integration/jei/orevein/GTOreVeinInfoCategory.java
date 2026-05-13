package com.extfro.extfrocore.integration.jei.orevein;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.data.chemical.ChemicalHelper;
import com.extfro.extfrocore.api.data.tag.TagPrefix;
import com.extfro.extfrocore.api.data.worldgen.GTOreDefinition;
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
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class GTOreVeinInfoCategory extends ModularUIRecipeCategory<Holder<GTOreDefinition>> {

    public final static RecipeType<Holder<GTOreDefinition>> RECIPE_TYPE = new RecipeType(ExtForCore.id("ore_vein_diagram"),
            Holder.class);
    private final IDrawable background;
    private final IDrawable icon;

    public GTOreVeinInfoCategory(IJeiHelpers helpers) {
        super(definition -> ModularUI.of(UI.of(new GTOreVeinWidget(definition))));
        IGuiHelper guiHelper = helpers.getGuiHelper();
        this.background = guiHelper.createBlankDrawable(GTOreVeinWidget.width, 120);
        this.icon = helpers.getGuiHelper()
                .createDrawableItemStack(ChemicalHelper.get(TagPrefix.rawOre, GTMaterials.Iron));
    }

    public static void registerRecipes(IRecipeRegistration registry) {
        var ores = Minecraft.getInstance().level.registryAccess()
                .registryOrThrow(GTRegistries.ORE_VEIN_REGISTRY);
        registry.addRecipes(RECIPE_TYPE, ores.holders()
                .filter(ore -> ore.value().canGenerate())
                .<Holder<GTOreDefinition>>map(Function.identity())
                .toList());
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Holder<GTOreDefinition> definition, IFocusGroup focuses) {
        super.setRecipe(builder, definition, focuses);
        builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT)
                .addItemStacks(GTOreVeinWidget.getContainedOresAndBlocks(definition.value()));
    }

    public static void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(GTItems.PROSPECTOR_LV.asStack(), RECIPE_TYPE);
        registration.addRecipeCatalyst(GTItems.PROSPECTOR_HV.asStack(), RECIPE_TYPE);
        registration.addRecipeCatalyst(GTItems.PROSPECTOR_LuV.asStack(), RECIPE_TYPE);
    }

    @NotNull
    @Override
    public RecipeType<Holder<GTOreDefinition>> getRecipeType() {
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
        return Component.translatable("gtceu.jei.ore_vein_diagram");
    }

    @Override
    public int getWidth() {
        return GTOreVeinWidget.width;
    }

    @Override
    public int getHeight() {
        return 160;
    }
}
