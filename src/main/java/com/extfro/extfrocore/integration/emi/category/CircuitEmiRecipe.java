package com.extfro.extfrocore.integration.emi.category;

import com.extfro.extfrocore.integration.xei.circuit.CircuitDisplay;
import com.extfro.extfrocore.integration.xei.circuit.CircuitStackEntry;
import com.extfro.extfrocore.integration.xei.circuit.CircuitViewerPage;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class CircuitEmiRecipe implements EmiRecipe {

    private final EmiRecipeCategory category;
    private final CircuitDisplay display;
    private final CircuitViewerPage page;
    private final ResourceLocation id;
    private final List<EmiIngredient> inputs;
    private final List<EmiStack> outputs;

    public CircuitEmiRecipe(EmiRecipeCategory category, CircuitDisplay display) {
        this.category = category;
        this.display = display;
        this.page = CircuitViewerPage.of(display);
        this.id = EmiXEIHelper.syntheticId("circuit", display.id());
        this.inputs = display.stacks().stream()
                .filter(CircuitStackEntry::isInput)
                .map(CircuitStackEntry::stack)
                .map(EmiXEIHelper::item)
                .filter(stack -> !stack.isEmpty())
                .map(stack -> (EmiIngredient) stack)
                .toList();
        this.outputs = display.outputStacks().stream()
                .map(EmiXEIHelper::item)
                .filter(stack -> !stack.isEmpty())
                .toList();
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
        return Math.max(134, page.width());
    }

    @Override
    public int getDisplayHeight() {
        return 18 + page.height();
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addText(display.title(), 0, 0, 0x404040, false);
        for (CircuitViewerPage.Slot slot : page.slots()) {
            var stack = EmiXEIHelper.item(slot.entry().stack());
            var widget = widgets.addSlot(stack, slot.x(), 18 + slot.y());
            if (slot.entry().isOutput()) {
                widget.recipeContext(this);
            }
            for (Component tooltip : slot.entry().tooltip()) {
                widget.appendTooltip(tooltip);
            }
            widget.appendTooltip(Component.literal("#" + slot.entry().configuration()));
        }
    }
}
