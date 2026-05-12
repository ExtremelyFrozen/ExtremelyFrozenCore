package com.extfro.extfrocore.integration.emi.recipe;

import net.minecraft.world.inventory.Slot;

import com.lowdragmc.lowdraglib2.gui.holder.ModularUIContainerMenu;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;

import java.util.List;

public class GTEmiRecipeHandler implements StandardRecipeHandler<ModularUIContainerMenu> {

    @Override
    public List<Slot> getInputSources(ModularUIContainerMenu handler) {
        var modularMenu = handler.asModularUIHolderMenu();
        return handler.slots.stream()
                .filter(slot -> modularMenu.getItemSlot(slot) != null)
                .toList();
    }

    @Override
    public List<Slot> getCraftingSlots(ModularUIContainerMenu handler) {
        var modularMenu = handler.asModularUIHolderMenu();
        return handler.slots.stream()
                .filter(slot -> modularMenu.getItemSlot(slot) != null)
                .filter(slot -> slot.container != handler.inventory)
                .toList();
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return recipe instanceof GTEmiRecipe;
    }
}
