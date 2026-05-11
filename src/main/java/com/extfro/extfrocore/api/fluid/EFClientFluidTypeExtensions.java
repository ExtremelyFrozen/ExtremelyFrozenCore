package com.extfro.extfrocore.api.fluid;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

public record EFClientFluidTypeExtensions(ResourceLocation stillTexture, ResourceLocation flowingTexture,
                                          int tintColor)
        implements IClientFluidTypeExtensions {

    @Override
    public ResourceLocation getStillTexture() {
        return stillTexture;
    }

    @Override
    public ResourceLocation getFlowingTexture() {
        return flowingTexture;
    }

    @Override
    public int getTintColor() {
        return tintColor;
    }
}
