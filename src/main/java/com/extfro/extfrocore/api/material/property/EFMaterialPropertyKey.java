package com.extfro.extfrocore.api.material.property;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

public class EFMaterialPropertyKey<T extends EFMaterialProperty> {

    public static final EFMaterialPropertyKey<EFBlastProperty> BLAST = new EFMaterialPropertyKey<>("blast", EFBlastProperty.class);
    public static final EFMaterialPropertyKey<EFAlloyBlastProperty> ALLOY_BLAST = new EFMaterialPropertyKey<>("blast_alloy", EFAlloyBlastProperty.class);
    public static final EFMaterialPropertyKey<EFDustProperty> DUST = new EFMaterialPropertyKey<>("dust", EFDustProperty.class);
    public static final EFMaterialPropertyKey<EFFluidPipeProperties> FLUID_PIPE = new EFMaterialPropertyKey<>("fluid_pipe", EFFluidPipeProperties.class);
    public static final EFMaterialPropertyKey<EFFluidProperty> FLUID = new EFMaterialPropertyKey<>("fluid", EFFluidProperty.class);
    public static final EFMaterialPropertyKey<EFGemProperty> GEM = new EFMaterialPropertyKey<>("gem", EFGemProperty.class);
    public static final EFMaterialPropertyKey<EFIngotProperty> INGOT = new EFMaterialPropertyKey<>("ingot", EFIngotProperty.class);
    public static final EFMaterialPropertyKey<EFPolymerProperty> POLYMER = new EFMaterialPropertyKey<>("polymer", EFPolymerProperty.class);
    public static final EFMaterialPropertyKey<EFItemPipeProperties> ITEM_PIPE = new EFMaterialPropertyKey<>("item_pipe", EFItemPipeProperties.class);
    public static final EFMaterialPropertyKey<EFOreProperty> ORE = new EFMaterialPropertyKey<>("ore", EFOreProperty.class);
    public static final EFMaterialPropertyKey<EFToolProperty> TOOL = new EFMaterialPropertyKey<>("tool", EFToolProperty.class);
    public static final EFMaterialPropertyKey<EFArmorProperty> ARMOR = new EFMaterialPropertyKey<>("armor", EFArmorProperty.class);
    public static final EFMaterialPropertyKey<EFRotorProperty> ROTOR = new EFMaterialPropertyKey<>("rotor", EFRotorProperty.class);
    public static final EFMaterialPropertyKey<EFWireProperties> WIRE = new EFMaterialPropertyKey<>("wire", EFWireProperties.class);
    public static final EFMaterialPropertyKey<EFWoodProperty> WOOD = new EFMaterialPropertyKey<>("wood", EFWoodProperty.class);
    public static final EFMaterialPropertyKey<EFHazardProperty> HAZARD = new EFMaterialPropertyKey<>("hazard", EFHazardProperty.class);
    public static final EFMaterialPropertyKey<EmptyProperty> EMPTY = new EFMaterialPropertyKey<>(
            "empty", EmptyProperty.class, EmptyProperty::new);

    @Getter
    private final String key;
    @Getter
    private final Class<T> type;
    @Nullable
    private final Supplier<T> defaultFactory;

    public EFMaterialPropertyKey(String key, Class<T> type) {
        this(key, type, null);
    }

    public EFMaterialPropertyKey(String key, Class<T> type, @Nullable Supplier<T> defaultFactory) {
        this.key = key;
        this.type = type;
        this.defaultFactory = defaultFactory;
    }

    public T constructDefault() {
        if (defaultFactory != null) {
            return defaultFactory.get();
        }
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Cannot construct default material property for " + key, exception);
        }
    }

    public T cast(EFMaterialProperty property) {
        return this.type.cast(property);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof EFMaterialPropertyKey<?> propertyKey &&
                Objects.equals(this.key, propertyKey.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public String toString() {
        return key;
    }

    public static class EmptyProperty implements EFMaterialProperty {

        @Override
        public void verifyProperty(EFMaterialProperties properties) {}
    }
}
