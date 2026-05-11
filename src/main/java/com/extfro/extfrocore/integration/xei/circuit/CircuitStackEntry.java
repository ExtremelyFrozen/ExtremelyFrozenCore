package com.extfro.extfrocore.integration.xei.circuit;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record CircuitStackEntry(ItemStack stack,
                                int configuration,
                                CircuitStackRole role,
                                List<Component> tooltip) {

    public CircuitStackEntry {
        stack = stack.copy();
        tooltip = List.copyOf(tooltip);
    }

    public static CircuitStackEntry both(ItemStack stack, int configuration) {
        return new CircuitStackEntry(stack, configuration, CircuitStackRole.BOTH, List.of());
    }

    public static CircuitStackEntry input(ItemStack stack, int configuration) {
        return new CircuitStackEntry(stack, configuration, CircuitStackRole.INPUT, List.of());
    }

    public static CircuitStackEntry output(ItemStack stack, int configuration) {
        return new CircuitStackEntry(stack, configuration, CircuitStackRole.OUTPUT, List.of());
    }

    public CircuitStackEntry withTooltip(List<Component> tooltip) {
        return new CircuitStackEntry(stack, configuration, role, tooltip);
    }

    public boolean isInput() {
        return role == CircuitStackRole.INPUT || role == CircuitStackRole.BOTH;
    }

    public boolean isOutput() {
        return role == CircuitStackRole.OUTPUT || role == CircuitStackRole.BOTH;
    }
}
