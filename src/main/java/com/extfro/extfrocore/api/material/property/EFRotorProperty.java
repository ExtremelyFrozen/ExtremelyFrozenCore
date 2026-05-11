package com.extfro.extfrocore.api.material.property;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class EFRotorProperty implements EFMaterialProperty {

    @Getter
    private final int rotorPower;
    @Getter
    private final int rotorEfficiency;
    @Getter
    private final float rotorDamage;
    @Getter
    private final int rotorDurability;

    public EFRotorProperty(int rotorPower, int rotorEfficiency, float rotorDamage, int rotorDurability) {
        if (rotorPower <= 0) throw new IllegalArgumentException("Rotor Power must be greater than zero");
        if (rotorEfficiency <= 0) throw new IllegalArgumentException("Rotor Efficiency must be greater than zero");
        if (rotorDamage <= 0) throw new IllegalArgumentException("Rotor Damage must be greater than zero");
        if (rotorDurability <= 0) throw new IllegalArgumentException("Rotor Durability must be greater than zero");
        this.rotorPower = rotorPower;
        this.rotorEfficiency = rotorEfficiency;
        this.rotorDamage = rotorDamage;
        this.rotorDurability = rotorDurability;
    }

    public EFRotorProperty() {
        this(1, 1, 1.0F, 1);
    }

    @Override
    public void verifyProperty(@NotNull EFMaterialProperties properties) {
        properties.ensureSet(EFMaterialPropertyKey.INGOT, true);
    }
}
