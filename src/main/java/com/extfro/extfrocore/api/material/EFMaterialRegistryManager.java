package com.extfro.extfrocore.api.material;

import com.extfro.extfrocore.api.material.registry.EFMaterialRegistry;

import net.minecraft.resources.ResourceLocation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;

public interface EFMaterialRegistryManager {

    @NotNull
    EFMaterialRegistry createRegistry(@NotNull String modId);

    @NotNull
    EFMaterialRegistry getRegistry(@NotNull String modId);

    @NotNull
    EFMaterialRegistry getRegistry(int networkId);

    @NotNull
    Collection<EFMaterialRegistry> getRegistries();

    @NotNull
    Collection<EFMaterial> getRegisteredMaterials();

    @Nullable
    EFMaterial getMaterial(String name);

    @Nullable
    EFMaterial getMaterial(ResourceLocation resourceLocation);

    ResourceLocation getKey(EFMaterial material);

    @NotNull
    Phase getPhase();

    default boolean canModifyMaterials() {
        return this.getPhase() != Phase.FROZEN && this.getPhase() != Phase.PRE;
    }

    default Codec<EFMaterial> codec() {
        return ResourceLocation.CODEC.flatXmap(
                id -> Optional.ofNullable(this.getRegistry(id.getNamespace()).get(id.getPath()))
                        .map(DataResult::success)
                        .orElseGet(() -> DataResult.error(() -> "Unknown material registry key: " + id)),
                material -> DataResult.success(material.getResourceLocation()));
    }

    enum Phase {
        PRE,
        OPEN,
        CLOSED,
        FROZEN
    }
}
