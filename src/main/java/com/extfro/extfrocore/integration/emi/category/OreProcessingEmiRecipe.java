package com.extfro.extfrocore.integration.emi.category;

import com.extfro.extfrocore.integration.xei.oreprocessing.OreProcessingDisplay;
import com.extfro.extfrocore.integration.xei.oreprocessing.OreProcessingStep;

import net.minecraft.resources.ResourceLocation;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class OreProcessingEmiRecipe implements EmiRecipe {

    private final EmiRecipeCategory category;
    private final OreProcessingDisplay display;
    private final ResourceLocation id;
    private final List<EmiIngredient> inputs;
    private final List<EmiStack> outputs;

    public OreProcessingEmiRecipe(EmiRecipeCategory category, OreProcessingDisplay display) {
        this.category = category;
        this.display = display;
        this.id = EmiXEIHelper.syntheticId("ore_processing", display.id());
        this.inputs = inputs(display);
        this.outputs = outputs(display);
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return category;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return outputs;
    }

    @Override
    public int getDisplayWidth() {
        return Math.max(display.category().width(), 134);
    }

    @Override
    public int getDisplayHeight() {
        return Math.max(display.category().height(), 42 + display.steps().size() * 22);
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addText(display.title(), 0, 0, 0x404040, false);
        widgets.addSlot(EmiXEIHelper.itemIngredient(display.primaryInput()), 0, 18);
        int y = 18;
        for (OreProcessingStep step : display.steps()) {
            widgets.addTexture(EmiTexture.EMPTY_ARROW, 24, y + 1);
            widgets.addText(step.title(), 50, y + 5, step.optional() ? 0x808080 : 0x404040, false);
            int x = 96;
            for (var stack : step.itemOutputs()) {
                widgets.addSlot(EmiXEIHelper.item(stack), x, y).recipeContext(this);
                x += 18;
            }
            for (var stack : step.fluidOutputs()) {
                widgets.addSlot(EmiXEIHelper.fluid(stack), x, y).recipeContext(this);
                x += 18;
            }
            y += 22;
        }
    }

    private static List<EmiIngredient> inputs(OreProcessingDisplay display) {
        List<EmiIngredient> inputs = new ArrayList<>();
        display.itemInputs().stream()
                .map(EmiXEIHelper::itemIngredient)
                .filter(ingredient -> !ingredient.isEmpty())
                .forEach(inputs::add);
        display.fluidInputs().stream()
                .map(EmiXEIHelper::fluidIngredient)
                .filter(ingredient -> !ingredient.isEmpty())
                .forEach(inputs::add);
        return List.copyOf(inputs);
    }

    private static List<EmiStack> outputs(OreProcessingDisplay display) {
        List<EmiStack> outputs = new ArrayList<>();
        for (OreProcessingStep step : display.steps()) {
            step.itemOutputs().stream()
                    .map(EmiXEIHelper::item)
                    .filter(stack -> !stack.isEmpty())
                    .forEach(outputs::add);
            step.fluidOutputs().stream()
                    .map(EmiXEIHelper::fluid)
                    .filter(stack -> !stack.isEmpty())
                    .forEach(outputs::add);
        }
        return List.copyOf(outputs);
    }
}
