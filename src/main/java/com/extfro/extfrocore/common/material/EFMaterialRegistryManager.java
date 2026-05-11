package com.extfro.extfrocore.common.material;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.material.EFMaterial;
import com.extfro.extfrocore.api.material.property.EFFluidProperty;
import com.extfro.extfrocore.api.material.property.EFMaterialPropertyKey;
import com.extfro.extfrocore.api.material.registry.EFMaterialRegistry;

import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class EFMaterialRegistryManager implements com.extfro.extfrocore.api.material.EFMaterialRegistryManager {

    private static EFMaterialRegistryManager instance;

    private final Map<String, EFMaterialRegistryImpl> registries = new HashMap<>();
    private final Map<Integer, EFMaterialRegistryImpl> networkIds = new HashMap<>();
    @Nullable
    private Collection<EFMaterial> registeredMaterials;
    private final EFMaterialRegistryImpl internalRegistry = createInternalRegistry();
    private Phase registrationPhase = Phase.PRE;

    private EFMaterialRegistryManager() {
        internalRegistry.unfreeze();
        internalRegistry.register(EFMaterial.EMPTY.getName(), EFMaterial.EMPTY);
        internalRegistry.freeze();
        internalRegistry.setFallbackMaterial(EFMaterial.EMPTY);
    }

    public static EFMaterialRegistryManager getInstance() {
        if (instance == null) {
            instance = new EFMaterialRegistryManager();
        }
        return instance;
    }

    @NotNull
    @Override
    public EFMaterialRegistry createRegistry(@NotNull String modId) {
        if (getPhase() != Phase.PRE) {
            throw new IllegalStateException("Cannot create material registries in phase " + getPhase());
        }
        if (registries.containsKey(modId)) {
            throw new IllegalArgumentException("Material registry already exists for mod id " + modId);
        }
        EFMaterialRegistryImpl registry = new EFMaterialRegistryImpl(modId);
        registries.put(modId, registry);
        networkIds.put(registry.getNetworkId(), registry);
        return registry;
    }

    @NotNull
    @Override
    public EFMaterialRegistry getRegistry(@NotNull String modId) {
        EFMaterialRegistry registry = registries.get(modId);
        return registry != null ? registry : internalRegistry;
    }

    @NotNull
    @Override
    public EFMaterialRegistry getRegistry(int networkId) {
        EFMaterialRegistry registry = networkIds.get(networkId);
        return registry != null ? registry : internalRegistry;
    }

    @NotNull
    @Override
    public Collection<EFMaterialRegistry> getRegistries() {
        if (getPhase() == Phase.PRE) {
            throw new IllegalStateException("Cannot get material registries during phase " + getPhase());
        }
        return Collections.unmodifiableCollection(registries.values());
    }

    @NotNull
    @Override
    public Collection<EFMaterial> getRegisteredMaterials() {
        if (registeredMaterials == null || (getPhase() != Phase.CLOSED && getPhase() != Phase.FROZEN)) {
            throw new IllegalStateException("Cannot retrieve all materials before registration");
        }
        return registeredMaterials;
    }

    @Nullable
    @Override
    public EFMaterial getMaterial(String name) {
        if (name == null || name.isEmpty()) {
            return EFMaterial.EMPTY;
        }
        String modId;
        String materialName;
        int index = name.indexOf(':');
        if (index >= 0) {
            modId = name.substring(0, index);
            materialName = name.substring(index + 1);
        } else {
            modId = ExtForCore.MOD_ID;
            materialName = name;
        }
        EFMaterial material = getRegistry(modId).get(materialName);
        return material != null ? material : getRegistry(modId).getFallbackMaterial();
    }

    @Nullable
    @Override
    public EFMaterial getMaterial(ResourceLocation resourceLocation) {
        EFMaterial material = getRegistry(resourceLocation.getNamespace()).get(resourceLocation.getPath());
        return material != null ? material : getRegistry(resourceLocation.getNamespace()).getFallbackMaterial();
    }

    @Override
    public ResourceLocation getKey(EFMaterial material) {
        return material.getResourceLocation();
    }

    @NotNull
    @Override
    public Phase getPhase() {
        return registrationPhase;
    }

    public void unfreezeRegistries() {
        registries.values().forEach(EFMaterialRegistryImpl::unfreeze);
        registrationPhase = Phase.OPEN;
    }

    public void closeRegistries() {
        registries.values().forEach(EFMaterialRegistryImpl::closeRegistry);
        Collection<EFMaterial> collection = new ArrayList<>();
        for (EFMaterialRegistry registry : registries.values()) {
            collection.addAll(registry.getAllMaterials());
        }
        registeredMaterials = Collections.unmodifiableCollection(collection);
        registrationPhase = Phase.CLOSED;
    }

    public void freezeRegistries() {
        registries.values().forEach(EFMaterialRegistryImpl::freeze);
        registrationPhase = Phase.FROZEN;
    }

    public void registerMaterialFluids() {
        if (getPhase() != Phase.CLOSED) {
            throw new IllegalStateException("Cannot register material fluids during phase " + getPhase());
        }
        for (EFMaterialRegistryImpl registry : registries.values()) {
            for (EFMaterial material : registry.getAllMaterials()) {
                EFFluidProperty property = material.getProperty(EFMaterialPropertyKey.FLUID);
                if (property != null) {
                    property.registerFluids(material, registry.getRegistrate());
                }
            }
        }
    }

    @NotNull
    private EFMaterialRegistryImpl createInternalRegistry() {
        EFMaterialRegistryImpl registry = new EFMaterialRegistryImpl(ExtForCore.MOD_ID);
        this.registries.put(ExtForCore.MOD_ID, registry);
        this.networkIds.put(registry.getNetworkId(), registry);
        return registry;
    }

    @NotNull
    public EFMaterial getDefaultFallback() {
        return internalRegistry.getFallbackMaterial();
    }
}
