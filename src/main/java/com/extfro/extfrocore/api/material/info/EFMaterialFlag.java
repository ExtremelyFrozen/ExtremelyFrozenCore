package com.extfro.extfrocore.api.material.info;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.material.EFMaterial;
import com.extfro.extfrocore.api.material.property.EFMaterialPropertyKey;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class EFMaterialFlag {

    private static final Set<EFMaterialFlag> FLAG_REGISTRY = new HashSet<>();

    private final String name;
    private final Set<EFMaterialFlag> requiredFlags;
    private final Set<EFMaterialPropertyKey<?>> requiredProperties;

    private EFMaterialFlag(String name, Set<EFMaterialFlag> requiredFlags,
                           Set<EFMaterialPropertyKey<?>> requiredProperties) {
        this.name = name;
        this.requiredFlags = requiredFlags;
        this.requiredProperties = requiredProperties;
        FLAG_REGISTRY.add(this);
    }

    protected Set<EFMaterialFlag> verifyFlag(EFMaterial material) {
        requiredProperties.forEach(key -> {
            if (!material.hasProperty(key)) {
                ExtForCore.LOGGER.warn("Material {} does not have required property {} for flag {}",
                        material.getResourceLocation(), key, this.name);
            }
        });

        Set<EFMaterialFlag> dependencies = new HashSet<>(requiredFlags);
        requiredFlags.stream()
                .map(flag -> flag.verifyFlag(material))
                .forEach(dependencies::addAll);
        return dependencies;
    }

    public static EFMaterialFlag getByName(String name) {
        return FLAG_REGISTRY.stream()
                .filter(flag -> flag.toString().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof EFMaterialFlag flag && name.equals(flag.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public static class Builder {

        private final String name;
        private final Set<EFMaterialFlag> requiredFlags = new ObjectOpenHashSet<>();
        private final Set<EFMaterialPropertyKey<?>> requiredProperties = new ObjectOpenHashSet<>();

        public Builder(String name) {
            this.name = name;
        }

        public Builder requireFlags(EFMaterialFlag... flags) {
            requiredFlags.addAll(Arrays.asList(flags));
            return this;
        }

        public Builder requireProps(EFMaterialPropertyKey<?>... propertyKeys) {
            requiredProperties.addAll(Arrays.asList(propertyKeys));
            return this;
        }

        public EFMaterialFlag build() {
            return new EFMaterialFlag(name, requiredFlags, requiredProperties);
        }
    }
}
