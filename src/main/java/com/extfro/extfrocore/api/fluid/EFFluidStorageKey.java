package com.extfro.extfrocore.api.fluid;

import com.extfro.extfrocore.api.material.EFMaterial;
import com.extfro.extfrocore.api.material.info.EFMaterialIconType;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

public final class EFFluidStorageKey {

    private static final Map<ResourceLocation, EFFluidStorageKey> KEYS = new Object2ObjectOpenHashMap<>();

    @Getter
    private final ResourceLocation resourceLocation;
    @Getter
    private final TagKey<Fluid> extraTag;
    @Getter
    private final EFMaterialIconType iconType;
    private final Function<EFMaterial, String> registryNameFunction;
    private final Function<EFMaterial, String> translationKeyFunction;
    @Getter
    private final EFFluidState defaultFluidState;
    @Getter
    private final int registrationPriority;

    public EFFluidStorageKey(@NotNull ResourceLocation resourceLocation, @Nullable TagKey<Fluid> extraTag,
                             @NotNull EFMaterialIconType iconType,
                             @NotNull Function<@NotNull EFMaterial, @NotNull String> registryNameFunction,
                             @NotNull Function<@NotNull EFMaterial, @NotNull String> translationKeyFunction,
                             @Nullable EFFluidState defaultFluidState, int registrationPriority) {
        this.resourceLocation = resourceLocation;
        this.extraTag = extraTag;
        this.iconType = iconType;
        this.registryNameFunction = registryNameFunction;
        this.translationKeyFunction = translationKeyFunction;
        this.defaultFluidState = defaultFluidState;
        this.registrationPriority = registrationPriority;
        if (KEYS.containsKey(resourceLocation)) {
            throw new IllegalArgumentException("Cannot create duplicate fluid storage key " + resourceLocation);
        }
        KEYS.put(resourceLocation, this);
    }

    public static @Nullable EFFluidStorageKey getByName(@NotNull ResourceLocation location) {
        return KEYS.get(location);
    }

    public static Collection<EFFluidStorageKey> allKeys() {
        return KEYS.values();
    }

    public @NotNull String getRegistryNameFor(@NotNull EFMaterial material) {
        return registryNameFunction.apply(material);
    }

    public @NotNull String getTranslationKeyFor(@NotNull EFMaterial material) {
        return translationKeyFunction.apply(material);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof EFFluidStorageKey key &&
                resourceLocation.equals(key.resourceLocation);
    }

    @Override
    public int hashCode() {
        return resourceLocation.hashCode();
    }

    @Override
    public String toString() {
        return "EFFluidStorageKey{" + resourceLocation + '}';
    }
}
