package com.extfro.extfrocore.utils;

import com.extfro.extfrocore.api.capability.GTCapabilityHelper;
import com.extfro.extfrocore.api.capability.IElectricItem;
import com.extfro.extfrocore.api.item.capability.ElectricItem;
import com.extfro.extfrocore.api.item.tool.GTToolType;
import com.extfro.extfrocore.common.data.GTMaterialItems;
import com.extfro.extfrocore.common.data.GTMaterials;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ToolItemHelper {

    public static final Map<GTToolType, ItemStack> TOOL_CACHE = new HashMap<>();

    /**
     * Attempts to get an electric item variant with override of max charge
     *
     * @param maxCharge new max charge of this electric item
     * @return item stack with given max charge
     * @throws IllegalStateException if this item is not electric item or uses custom implementation
     */
    public static ItemStack getMaxChargeOverrideStack(Item item, long maxCharge) {
        ItemStack itemStack = item.getDefaultInstance();
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(itemStack);
        if (electricItem == null) {
            throw new IllegalStateException("Not an electric item.");
        }
        if (!(electricItem instanceof ElectricItem)) {
            throw new IllegalStateException("Only standard ElectricItem implementation supported, but this item uses " +
                    electricItem.getClass());
        }
        ((ElectricItem) electricItem).setMaxChargeOverride(maxCharge);
        return itemStack;
    }

    /**
     * get tool itemStack by GTToolType with default Material
     *
     * @param toolType GTToolType
     * @return the tool itemStack
     */
    public static ItemStack getToolItem(GTToolType toolType) {
        return TOOL_CACHE.computeIfAbsent(toolType, type -> {
            if (type == GTToolType.SOFT_MALLET) {
                return GTMaterialItems.TOOL_ITEMS.get(GTMaterials.Rubber, type).asStack();
            }
            return GTMaterialItems.TOOL_ITEMS.get(GTMaterials.Neutronium, type).asStack();
        });
    }
}
