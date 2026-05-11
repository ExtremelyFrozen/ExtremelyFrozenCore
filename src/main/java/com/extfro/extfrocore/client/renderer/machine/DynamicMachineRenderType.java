package com.extfro.extfrocore.client.renderer.machine;

import com.extfro.extfrocore.api.machine.feature.IMachineFeature;

import net.minecraft.resources.ResourceLocation;

import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.NotNull;

public record DynamicMachineRenderType<T extends IMachineFeature, S extends DynamicMachineRender<T, S>>(
        MapCodec<S> codec) implements Comparable<DynamicMachineRenderType<T, S>> {

    public ResourceLocation getId() {
        return DynamicMachineRenderManager.getId(this);
    }

    @Override
    public int compareTo(@NotNull DynamicMachineRenderType<T, S> other) {
        return getId().compareTo(other.getId());
    }
}
