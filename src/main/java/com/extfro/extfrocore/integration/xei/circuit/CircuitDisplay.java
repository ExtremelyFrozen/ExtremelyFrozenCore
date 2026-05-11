package com.extfro.extfrocore.integration.xei.circuit;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public record CircuitDisplay(ResourceLocation id,
                             Component title,
                             ItemStack icon,
                             List<CircuitStackEntry> stacks,
                             int columns) {

    public CircuitDisplay {
        stacks = List.copyOf(stacks);
        icon = icon.copy();
    }

    public static CircuitDisplay of(ResourceLocation id,
                                    Component title,
                                    ItemStack icon,
                                    List<CircuitStackEntry> stacks) {
        return new CircuitDisplay(id, title, icon, stacks, CircuitViewerPage.DEFAULT_COLUMNS);
    }

    public boolean isEmpty() {
        return stacks.isEmpty();
    }

    public int rows() {
        if (stacks.isEmpty()) {
            return 0;
        }
        return Math.ceilDiv(stacks.size(), columns);
    }

    public List<ItemStack> outputStacks() {
        return stacks.stream()
                .filter(CircuitStackEntry::isOutput)
                .map(CircuitStackEntry::stack)
                .map(ItemStack::copy)
                .toList();
    }

    @Nullable
    public ItemStack firstOutput() {
        return stacks.stream()
                .filter(CircuitStackEntry::isOutput)
                .findFirst()
                .map(CircuitStackEntry::stack)
                .map(ItemStack::copy)
                .orElse(null);
    }
}
