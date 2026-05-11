package com.extfro.extfrocore.client.renderer.cover;

import com.extfro.extfrocore.api.machine.MetaMachine;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

import com.mojang.blaze3d.vertex.PoseStack;

public final class MachineCoverBlockEntityRenderer implements BlockEntityRenderer<MetaMachine>, ICoverableRenderer {

    public MachineCoverBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(MetaMachine machine, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        if (machine.getCoverContainer().hasDynamicCovers()) {
            renderDynamicCovers(machine, partialTick, poseStack, buffer, packedLight, packedOverlay);
        }
    }
}
