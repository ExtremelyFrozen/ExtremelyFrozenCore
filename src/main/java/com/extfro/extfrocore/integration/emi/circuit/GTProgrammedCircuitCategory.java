package com.extfro.extfrocore.integration.emi.circuit;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.common.data.GTItems;
import com.extfro.extfrocore.common.item.behavior.IntCircuitBehaviour;
import com.extfro.extfrocore.integration.xei.widgets.GTProgrammedCircuitWidget;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.integration.xei.emi.ModularUIEMIRecipe;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.IntStream;

public class GTProgrammedCircuitCategory extends EmiRecipeCategory {

    public static final GTProgrammedCircuitCategory CATEGORY = new GTProgrammedCircuitCategory();

    public GTProgrammedCircuitCategory() {
        super(ExtForCore.id("programmed_circuit"), EmiStack.of(GTItems.PROGRAMMED_CIRCUIT.asItem()));
    }

    public static void registerDisplays(EmiRegistry registry) {
        registry.addRecipe(new GTProgrammedCircuitWrapper());
    }

    @Override
    public Component getName() {
        return Component.translatable("gtceu.jei.programmed_circuit");
    }

    public static class GTProgrammedCircuitWrapper extends ModularUIEMIRecipe {

        public GTProgrammedCircuitWrapper() {
            super(recipe -> ModularUI.of(UI.of(new GTProgrammedCircuitWidget())));
        }

        @Override
        public EmiRecipeCategory getCategory() {
            return CATEGORY;
        }

        @Override
        public int getDisplayWidth() {
            return 150;
        }

        @Override
        public int getDisplayHeight() {
            return 80;
        }

        @Override
        public @Nullable ResourceLocation getId() {
            return ExtForCore.id("/programmed_circuit");
        }

        @Override
        public List<EmiStack> getOutputs() {
            return IntStream.range(0, 33)
                    .mapToObj(IntCircuitBehaviour::stack)
                    .map(EmiStack::of)
                    .toList();
        }

        @Override
        public boolean supportsRecipeTree() {
            return false;
        }

        @Override
        public boolean hideCraftable() {
            return true;
        }
    }
}
