package com.extfro.extfrocore.api.material.registry;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.material.EFMaterial;
import com.extfro.extfrocore.api.registry.EFRegistry;
import com.extfro.extfrocore.api.registry.registrate.EFRegistrate;
import com.extfro.extfrocore.common.registry.EFRegistration;

import net.minecraft.resources.ResourceLocation;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public abstract class EFMaterialRegistry extends EFRegistry.StringKey<EFMaterial> {

    @Getter
    private final EFRegistrate registrate;

    public EFMaterialRegistry(String modId) {
        super(ResourceLocation.fromNamespaceAndPath(modId, "material"));
        this.registrate = ExtForCore.MOD_ID.equals(modId) ? EFRegistration.REGISTRATE : EFRegistrate.create(modId);
    }

    public abstract void register(EFMaterial material);

    @NotNull
    public abstract Collection<EFMaterial> getAllMaterials();

    public abstract void setFallbackMaterial(@NotNull EFMaterial material);

    @NotNull
    public abstract EFMaterial getFallbackMaterial();

    public abstract int getNetworkId();

    @NotNull
    public abstract String getModId();
}
