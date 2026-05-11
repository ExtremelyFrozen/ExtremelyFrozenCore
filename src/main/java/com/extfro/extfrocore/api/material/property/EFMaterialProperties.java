package com.extfro.extfrocore.api.material.property;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.material.EFMaterial;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EFMaterialProperties {

    private static final Set<EFMaterialPropertyKey<?>> BASE_TYPES = new HashSet<>(Arrays.asList(
            EFMaterialPropertyKey.FLUID,
            EFMaterialPropertyKey.DUST,
            EFMaterialPropertyKey.INGOT,
            EFMaterialPropertyKey.GEM,
            EFMaterialPropertyKey.EMPTY));

    public static void addBaseType(EFMaterialPropertyKey<?> baseTypeKey) {
        BASE_TYPES.add(baseTypeKey);
    }

    private final Map<EFMaterialPropertyKey<? extends EFMaterialProperty>, EFMaterialProperty> propertyMap = new HashMap<>();

    @Getter
    @Setter
    private EFMaterial material;

    public boolean isEmpty() {
        return propertyMap.isEmpty();
    }

    public <T extends EFMaterialProperty> T getProperty(EFMaterialPropertyKey<T> key) {
        return key.cast(propertyMap.get(key));
    }

    public <T extends EFMaterialProperty> boolean hasProperty(EFMaterialPropertyKey<T> key) {
        return propertyMap.containsKey(key);
    }

    public <T extends EFMaterialProperty> void setProperty(EFMaterialPropertyKey<T> key, T value) {
        if (value == null) {
            throw new IllegalArgumentException("Material property must not be null");
        }
        if (!key.getType().isInstance(value)) {
            throw new IllegalArgumentException("Material property must match the property key type");
        }
        if (hasProperty(key)) {
            throw new IllegalArgumentException("Material property " + key + " already registered");
        }
        propertyMap.put(key, value);
        propertyMap.remove(EFMaterialPropertyKey.EMPTY);
    }

    public <T extends EFMaterialProperty> void removeProperty(EFMaterialPropertyKey<T> key) {
        if (!hasProperty(key)) {
            throw new IllegalArgumentException("Material property " + key + " not present");
        }
        propertyMap.remove(key);
        if (propertyMap.isEmpty()) {
            propertyMap.put(EFMaterialPropertyKey.EMPTY, EFMaterialPropertyKey.EMPTY.constructDefault());
        }
    }

    public <T extends EFMaterialProperty> void ensureSet(EFMaterialPropertyKey<T> key, boolean verify) {
        if (!hasProperty(key)) {
            propertyMap.put(key, key.constructDefault());
            propertyMap.remove(EFMaterialPropertyKey.EMPTY);
            if (verify) {
                verify();
            }
        }
    }

    public <T extends EFMaterialProperty> void ensureSet(EFMaterialPropertyKey<T> key) {
        ensureSet(key, false);
    }

    public void verify() {
        int oldSize;
        do {
            oldSize = propertyMap.size();
            for (EFMaterialProperty property : Set.copyOf(propertyMap.values())) {
                property.verifyProperty(this);
            }
        } while (oldSize != propertyMap.size());

        if (propertyMap.keySet().stream().noneMatch(BASE_TYPES::contains)) {
            if (propertyMap.isEmpty()) {
                if (ExtForCore.isDev()) {
                    ExtForCore.LOGGER.debug("Creating empty placeholder material {}", material);
                }
                propertyMap.put(EFMaterialPropertyKey.EMPTY, EFMaterialPropertyKey.EMPTY.constructDefault());
            } else {
                throw new IllegalArgumentException("Material must have at least one of: " + BASE_TYPES + " specified");
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        propertyMap.forEach((key, value) -> builder.append(key).append('\n'));
        return builder.toString();
    }
}
