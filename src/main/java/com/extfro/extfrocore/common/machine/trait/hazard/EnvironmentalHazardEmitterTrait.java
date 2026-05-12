package com.extfro.extfrocore.common.machine.trait.hazard;

import com.extfro.extfrocore.api.capability.GTCapabilityHelper;
import com.extfro.extfrocore.api.capability.IHazardParticleContainer;
import com.extfro.extfrocore.api.data.chemical.material.properties.HazardProperty;
import com.extfro.extfrocore.api.data.medicalcondition.MedicalCondition;
import com.extfro.extfrocore.api.machine.trait.MachineTrait;
import com.extfro.extfrocore.api.machine.trait.MachineTraitType;
import com.extfro.extfrocore.common.capability.EnvironmentalHazardSavedData;
import com.extfro.extfrocore.config.ConfigHolder;

import net.minecraft.server.level.ServerLevel;

import lombok.Getter;
import lombok.Setter;

/**
 * trait for environmental hazard (e.g. pollution) emitters like mufflers.
 */
public class EnvironmentalHazardEmitterTrait extends MachineTrait {

    public static final MachineTraitType<EnvironmentalHazardEmitterTrait> TYPE = new MachineTraitType<>(
            EnvironmentalHazardEmitterTrait.class);

    @Getter
    @Setter
    protected float emissionStrength;
    @Getter
    @Setter
    protected MedicalCondition conditionToEmit;

    public EnvironmentalHazardEmitterTrait(MedicalCondition conditionToEmit,
                                           float emissionStrength) {
        super();
        this.conditionToEmit = conditionToEmit;
        this.emissionStrength = emissionStrength;
    }

    @Override
    public MachineTraitType<EnvironmentalHazardEmitterTrait> getTraitType() {
        return TYPE;
    }

    public void emitHazard() {
        if (!ConfigHolder.INSTANCE.gameplay.environmentalHazards) {
            return;
        }

        if (getLevel() instanceof ServerLevel serverLevel) {
            IHazardParticleContainer container = GTCapabilityHelper.getHazardContainer(serverLevel,
                    getBlockPos().relative(getMachine().getFrontFacing()), getMachine().getFrontFacing().getOpposite());
            if (container != null &&
                    container.getHazardCanBeInserted(getConditionToEmit()) > getEmissionStrength()) {
                container.addHazard(getConditionToEmit(), getEmissionStrength());
                return;
            }

            var savedData = EnvironmentalHazardSavedData.getOrCreate(serverLevel);
            savedData.addZone(getBlockPos(), getEmissionStrength(), true,
                    HazardProperty.HazardTrigger.INHALATION, getConditionToEmit());
        }
    }
}
