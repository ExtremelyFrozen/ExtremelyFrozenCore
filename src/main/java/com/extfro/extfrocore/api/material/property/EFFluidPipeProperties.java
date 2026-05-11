package com.extfro.extfrocore.api.material.property;

import com.extfro.extfrocore.api.fluid.EFFluidAttribute;
import com.extfro.extfrocore.api.fluid.EFFluidAttributes;
import com.extfro.extfrocore.api.fluid.EFFluidState;
import com.extfro.extfrocore.api.fluid.EFPropertyFluidFilter;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Objects;

public class EFFluidPipeProperties implements EFMaterialProperty, EFPropertyFluidFilter {

    public static final int MAX_PIPE_CHANNELS = 9;

    @Getter
    @Setter
    private int throughput;
    @Getter
    @Setter
    private int channels;
    @Getter
    @Setter
    private int maxFluidTemperature;
    @Getter
    @Setter
    private boolean gasProof;
    @Getter
    @Setter
    private boolean cryoProof;
    @Getter
    @Setter
    private boolean plasmaProof;

    private final Object2BooleanMap<EFFluidAttribute> containmentPredicate = new Object2BooleanOpenHashMap<>();

    public EFFluidPipeProperties(int maxFluidTemperature, int throughput, boolean gasProof, boolean acidProof,
                                 boolean cryoProof, boolean plasmaProof, int channels) {
        this.maxFluidTemperature = maxFluidTemperature;
        this.throughput = throughput;
        this.gasProof = gasProof;
        if (acidProof) {
            setCanContain(EFFluidAttributes.ACID, true);
        }
        this.cryoProof = cryoProof;
        this.plasmaProof = plasmaProof;
        this.channels = channels;
    }

    public EFFluidPipeProperties(int maxFluidTemperature, int throughput, boolean gasProof, boolean acidProof,
                                 boolean cryoProof, boolean plasmaProof) {
        this(maxFluidTemperature, throughput, gasProof, acidProof, cryoProof, plasmaProof, 1);
    }

    public EFFluidPipeProperties() {
        this(0, 0, false, false, false, false);
    }

    @Override
    public void verifyProperty(EFMaterialProperties properties) {
        if (!properties.hasProperty(EFMaterialPropertyKey.WOOD)) {
            properties.ensureSet(EFMaterialPropertyKey.INGOT, true);
        }
        if (properties.hasProperty(EFMaterialPropertyKey.ITEM_PIPE)) {
            throw new IllegalStateException("Material " + properties.getMaterial() +
                    " has both Fluid and Item Pipe Property, which is not allowed");
        }
    }

    @Override
    public boolean canContain(@NotNull EFFluidState state) {
        return switch (state) {
            case LIQUID -> true;
            case GAS -> gasProof;
            case PLASMA -> plasmaProof;
        };
    }

    public boolean isAcidProof() {
        return canContain(EFFluidAttributes.ACID);
    }

    @Override
    public boolean canContain(@NotNull EFFluidAttribute attribute) {
        return containmentPredicate.getBoolean(attribute);
    }

    @Override
    public void setCanContain(@NotNull EFFluidAttribute attribute, boolean canContain) {
        containmentPredicate.put(attribute, canContain);
    }

    @Override
    public @NotNull @UnmodifiableView Collection<@NotNull EFFluidAttribute> getContainedAttributes() {
        return containmentPredicate.keySet();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof EFFluidPipeProperties that &&
                maxFluidTemperature == that.maxFluidTemperature &&
                throughput == that.throughput &&
                gasProof == that.gasProof &&
                channels == that.channels;
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxFluidTemperature, throughput, gasProof, channels);
    }
}
