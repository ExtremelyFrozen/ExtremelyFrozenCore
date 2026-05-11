package com.extfro.extfrocore.api.material.property;

import lombok.Getter;
import lombok.Setter;

import java.util.function.Supplier;

public class EFBlastProperty implements EFMaterialProperty {

    @Getter
    private int blastTemperature;
    @Setter
    @Getter
    private GasTier gasTier = null;
    @Setter
    @Getter
    private int durationOverride = -1;
    @Setter
    @Getter
    private int EUtOverride = -1;
    @Setter
    @Getter
    private int vacuumDurationOverride = -1;
    @Setter
    @Getter
    private int vacuumEUtOverride = -1;

    public EFBlastProperty(int blastTemperature) {
        this.blastTemperature = blastTemperature;
    }

    public EFBlastProperty(int blastTemperature, GasTier gasTier) {
        this.blastTemperature = blastTemperature;
        this.gasTier = gasTier;
    }

    public EFBlastProperty(int blastTemperature, GasTier gasTier, int eutOverride, int durationOverride,
                           int vacuumEUtOverride, int vacuumDurationOverride) {
        this.blastTemperature = blastTemperature;
        this.gasTier = gasTier;
        this.EUtOverride = eutOverride;
        this.durationOverride = durationOverride;
        this.vacuumEUtOverride = vacuumEUtOverride;
        this.vacuumDurationOverride = vacuumDurationOverride;
    }

    public EFBlastProperty() {
        this(0);
    }

    public void setBlastTemperature(int blastTemperature) {
        if (blastTemperature <= 0) {
            throw new IllegalArgumentException("Blast Temperature must be greater than zero");
        }
        this.blastTemperature = blastTemperature;
    }

    @Override
    public void verifyProperty(EFMaterialProperties properties) {
        properties.ensureSet(EFMaterialPropertyKey.INGOT, true);
    }

    public static GasTier validateGasTier(String gasTierName) {
        if (gasTierName == null) return null;
        for (GasTier tier : GasTier.VALUES) {
            if (tier.name().equalsIgnoreCase(gasTierName)) {
                return tier;
            }
        }
        throw new IllegalArgumentException("Could not find valid gas tier for name: " + gasTierName);
    }

    public enum GasTier {

        LOW,
        MID,
        HIGH,
        HIGHER,
        HIGHEST;

        public static final GasTier[] VALUES = values();
        private Supplier<?> fluid = () -> null;

        public void setFluid(Supplier<?> fluid) {
            this.fluid = fluid;
        }

        public Object getFluid() {
            return fluid.get();
        }
    }

    public static class Builder {

        private int temp;
        private GasTier gasTier;
        private int eutOverride = -1;
        private int durationOverride = -1;
        private int vacuumEUtOverride = -1;
        private int vacuumDurationOverride = -1;

        public Builder temp(int temperature) {
            this.temp = temperature;
            return this;
        }

        public Builder temp(int temperature, GasTier gasTier) {
            this.temp = temperature;
            this.gasTier = gasTier;
            return this;
        }

        public Builder blastStats(int eutOverride) {
            this.eutOverride = eutOverride;
            return this;
        }

        public Builder blastStats(int eutOverride, int durationOverride) {
            this.eutOverride = eutOverride;
            this.durationOverride = durationOverride;
            return this;
        }

        public Builder vacuumStats(int eutOverride) {
            this.vacuumEUtOverride = eutOverride;
            return this;
        }

        public Builder vacuumStats(int eutOverride, int durationOverride) {
            this.vacuumEUtOverride = eutOverride;
            this.vacuumDurationOverride = durationOverride;
            return this;
        }

        public EFBlastProperty build() {
            return new EFBlastProperty(temp, gasTier, eutOverride, durationOverride, vacuumEUtOverride,
                    vacuumDurationOverride);
        }
    }
}
