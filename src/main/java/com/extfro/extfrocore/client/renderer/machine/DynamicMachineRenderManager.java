package com.extfro.extfrocore.client.renderer.machine;

import com.extfro.extfrocore.api.machine.feature.IMachineFeature;

import net.minecraft.resources.ResourceLocation;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import org.jetbrains.annotations.Nullable;

import java.util.function.UnaryOperator;

public final class DynamicMachineRenderManager {

    public static final UnaryOperator<String> MODEL_ID_FORMATTER = "/block/machine/%s/dynamic_render"::formatted;

    public static final Codec<DynamicMachineRenderType<?, ?>> TYPE_CODEC = ResourceLocation.CODEC.flatXmap(id -> {
        DynamicMachineRenderType<?, ?> type = getType(id);
        if (type != null) {
            return DataResult.success(type);
        }
        return DataResult.error(() -> "Dynamic machine render type with ID " + id + " does not exist");
    }, type -> {
        ResourceLocation id = getId(type);
        if (id != null) {
            return DataResult.success(id);
        }
        return DataResult.error(() -> "Dynamic machine render type " + type + " is not registered");
    });

    private static final BiMap<ResourceLocation, DynamicMachineRenderType<?, ?>> DYNAMIC_RENDER_TYPES =
            HashBiMap.create(5);

    public static <T extends IMachineFeature, S extends DynamicMachineRender<T, S>> DynamicMachineRenderType<T, S>
            register(ResourceLocation id, DynamicMachineRenderType<T, S> type) {
        if (DYNAMIC_RENDER_TYPES.containsKey(id)) {
            throw new IllegalArgumentException("Cannot register multiple dynamic machine render types with the same id: " + id);
        }
        DYNAMIC_RENDER_TYPES.put(id, type);
        return type;
    }

    @SuppressWarnings("unchecked")
    public static <T extends IMachineFeature, S extends DynamicMachineRender<T, S>>
            @Nullable DynamicMachineRenderType<T, S> getType(ResourceLocation id) {
        return (DynamicMachineRenderType<T, S>) DYNAMIC_RENDER_TYPES.get(id);
    }

    public static @Nullable ResourceLocation getId(DynamicMachineRenderType<?, ?> type) {
        return DYNAMIC_RENDER_TYPES.inverse().get(type);
    }

    private DynamicMachineRenderManager() {
    }
}
