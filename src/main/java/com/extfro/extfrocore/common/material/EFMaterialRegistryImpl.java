package com.extfro.extfrocore.common.material;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.material.EFMaterial;
import com.extfro.extfrocore.api.material.registry.EFMaterialRegistry;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class EFMaterialRegistryImpl extends EFMaterialRegistry {

    private static int networkIdCounter;

    private final int networkId = networkIdCounter++;
    private final String modId;
    private boolean registryClosed = false;
    @NotNull
    private EFMaterial fallbackMaterial = EFMaterial.EMPTY;

    protected EFMaterialRegistryImpl(@NotNull String modId) {
        super(modId);
        this.modId = modId;
    }

    @Override
    public void register(EFMaterial material) {
        this.register(material.getName(), material);
    }

    @Override
    @Nullable
    public <T extends EFMaterial> T register(@NotNull String key, @NotNull T value) {
        if (registryClosed) {
            ExtForCore.LOGGER.error(
                    "Materials cannot be registered during or after post material processing. Skipping material {}.",
                    key);
            return null;
        }
        super.register(key, value);
        return value;
    }

    @NotNull
    @Override
    public Collection<EFMaterial> getAllMaterials() {
        return this.values();
    }

    @Override
    public void setFallbackMaterial(@NotNull EFMaterial material) {
        this.fallbackMaterial = material;
    }

    @NotNull
    @Override
    public EFMaterial getFallbackMaterial() {
        if (fallbackMaterial.isEmpty() && !ExtForCore.MOD_ID.equals(modId)) {
            fallbackMaterial = EFMaterialRegistryManager.getInstance().getDefaultFallback();
        }
        return fallbackMaterial;
    }

    @Override
    public int getNetworkId() {
        return networkId;
    }

    @NotNull
    @Override
    public String getModId() {
        return modId;
    }

    public void closeRegistry() {
        this.registryClosed = true;
    }
}
