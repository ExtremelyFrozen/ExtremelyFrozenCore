package com.extfro.extfrocore.api.material.property;

import com.extfro.extfrocore.api.material.EFMaterial;

import lombok.Getter;

public class EFWireProperties implements EFMaterialProperty {

    @Getter
    private final long voltage;
    @Getter
    private final int amperage;
    @Getter
    private final int lossPerBlock;
    @Getter
    private final boolean superconductor;
    @Getter
    private final int criticalTemperature;

    public EFWireProperties(long voltage, int amperage, int lossPerBlock, boolean superconductor,
                            int criticalTemperature) {
        this.voltage = voltage;
        this.amperage = amperage;
        this.lossPerBlock = lossPerBlock;
        this.superconductor = superconductor;
        this.criticalTemperature = criticalTemperature;
    }

    public EFWireProperties(long voltage, int amperage, int lossPerBlock, boolean superconductor) {
        this(voltage, amperage, lossPerBlock, superconductor, 0);
    }

    public EFWireProperties() {
        this(0, 0, 0, false);
    }

    @Override
    public void verifyProperty(EFMaterialProperties properties) {
        EFMaterial material = properties.getMaterial();
        if (!material.hasProperty(EFMaterialPropertyKey.INGOT)) {
            properties.ensureSet(EFMaterialPropertyKey.DUST, true);
        }
    }
}
