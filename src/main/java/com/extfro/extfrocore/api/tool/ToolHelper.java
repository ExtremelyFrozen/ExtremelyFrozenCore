package com.extfro.extfrocore.api.tool;

import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashSet;
import java.util.Set;

public final class ToolHelper {

    private ToolHelper() {}

    public static boolean is(ItemStack stack, EFToolType toolType) {
        return getToolTypes(stack).contains(toolType);
    }

    public static Set<EFToolType> getToolTypes(ItemStack stack) {
        LinkedHashSet<EFToolType> types = new LinkedHashSet<>();
        if (stack.isEmpty()) {
            return types;
        }
        String descriptionId = stack.getItem().getDescriptionId(stack).toLowerCase(java.util.Locale.ROOT);
        String itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem())
                .getPath()
                .toLowerCase(java.util.Locale.ROOT);
        String text = descriptionId + " " + itemId;
        EFToolType.getTypes().values().forEach(type -> {
            if (matches(text, type)) {
                types.add(type);
            }
        });
        return types;
    }

    public static boolean canUse(ItemStack stack) {
        return !getToolTypes(stack).isEmpty();
    }

    private static boolean matches(String text, EFToolType type) {
        String name = type.name();
        if (text.contains(name)) {
            return true;
        }
        if (type == EFToolType.SOFT_MALLET) {
            return text.contains("mallet") || text.contains("soft_hammer");
        }
        if (type == EFToolType.HARD_HAMMER) {
            return text.contains("hammer") && !text.contains("soft");
        }
        return switch (name) {
            case "screwdriver" -> text.contains("screw_driver");
            case "wire_cutter" -> text.contains("wirecutter") || text.contains("wire_cutters");
            default -> false;
        };
    }
}
