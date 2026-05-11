package com.extfro.extfrocore.api.material.property;

import lombok.Getter;

public class EFItemPipeProperties implements EFMaterialProperty {

    @Getter
    private final int priority;
    @Getter
    private final float transferRate;

    public EFItemPipeProperties(int priority, float transferRate) {
        if (transferRate <= 0) {
            throw new IllegalArgumentException("Transfer rate must be greater than zero");
        }
        this.priority = priority;
        this.transferRate = transferRate;
    }

    public EFItemPipeProperties() {
        this(0, 0.25F);
    }

    @Override
    public void verifyProperty(EFMaterialProperties properties) {}
}
