package com.extfro.extfrocore.api.material.property;

import lombok.Getter;

public class EFDustProperty implements EFMaterialProperty {

    @Getter
    private int harvestLevel;
    @Getter
    private int burnTime;

    public EFDustProperty(int harvestLevel, int burnTime) {
        this.harvestLevel = harvestLevel;
        this.burnTime = burnTime;
    }

    public EFDustProperty() {
        this(2, 0);
    }

    public void setHarvestLevel(int harvestLevel) {
        if (harvestLevel <= 0) {
            throw new IllegalArgumentException("Harvest Level must be greater than zero");
        }
        this.harvestLevel = harvestLevel;
    }

    public void setBurnTime(int burnTime) {
        if (burnTime < 0) {
            throw new IllegalArgumentException("Burn Time cannot be negative");
        }
        this.burnTime = burnTime;
    }

    @Override
    public void verifyProperty(EFMaterialProperties properties) {}
}
