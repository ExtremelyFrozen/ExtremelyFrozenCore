package com.extfro.extfrocore.integration.jei.category;

import com.extfro.extfrocore.integration.xei.entry.fluid.FluidEntryList;
import com.extfro.extfrocore.integration.xei.entry.item.ItemEntryList;

import net.minecraft.core.component.DataComponentPatch;
import net.neoforged.neoforge.fluids.FluidStack;

import mezz.jei.api.gui.builder.IRecipeSlotBuilder;

final class XEIJeiIngredientHelper {

    private XEIJeiIngredientHelper() {}

    static void addItemEntries(IRecipeSlotBuilder slot, ItemEntryList entries) {
        var stacks = entries.getStacks().stream()
                .filter(stack -> !stack.isEmpty())
                .map(stack -> stack.copy())
                .toList();
        if (!stacks.isEmpty()) {
            slot.addItemStacks(stacks);
        }
    }

    static void addFluidEntries(IRecipeSlotBuilder slot, FluidEntryList entries) {
        for (FluidStack stack : entries.getStacks()) {
            addFluidStack(slot, stack);
        }
    }

    static void addFluidStack(IRecipeSlotBuilder slot, FluidStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        DataComponentPatch patch = stack.getComponentsPatch();
        if (patch.isEmpty()) {
            slot.addFluidStack(stack.getFluid(), stack.getAmount());
        } else {
            slot.addFluidStack(stack.getFluid(), stack.getAmount(), patch);
        }
    }
}
