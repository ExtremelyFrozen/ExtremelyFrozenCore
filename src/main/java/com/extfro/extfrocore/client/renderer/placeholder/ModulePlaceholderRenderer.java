package com.extfro.extfrocore.client.renderer.placeholder;

import com.extfro.extfrocore.api.item.IComponentItem;
import com.extfro.extfrocore.api.item.component.IItemComponent;
import com.extfro.extfrocore.api.item.component.IMonitorModuleItem;
import com.extfro.extfrocore.api.placeholder.IPlaceholderRenderer;
import com.extfro.extfrocore.common.machine.multiblock.electric.CentralMonitorMachine;
import com.extfro.extfrocore.common.machine.multiblock.electric.monitor.MonitorGroup;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import com.mojang.blaze3d.vertex.PoseStack;

public class ModulePlaceholderRenderer implements IPlaceholderRenderer {

    @Override
    public void render(CentralMonitorMachine machine, MonitorGroup group, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay, CompoundTag tag) {
        ItemStack stack = ItemStack.parse(machine.getLevel().registryAccess(), tag).orElse(ItemStack.EMPTY);
        if (stack.getItem() instanceof IComponentItem componentItem) {
            for (IItemComponent component : componentItem.getComponents()) {
                if (component instanceof IMonitorModuleItem module) {
                    module.getRenderer(stack).render(machine, group, partialTick, poseStack, buffer, packedLight,
                            packedOverlay);
                }
            }
        }
    }
}
