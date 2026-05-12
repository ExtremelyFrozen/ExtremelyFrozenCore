package com.extfro.extfrocore.common.machine.trait.hazard;

import com.extfro.extfrocore.api.capability.GTCapabilityHelper;
import com.extfro.extfrocore.api.capability.IHazardParticleContainer;
import com.extfro.extfrocore.api.data.chemical.material.properties.HazardProperty;
import com.extfro.extfrocore.api.data.medicalcondition.MedicalCondition;
import com.extfro.extfrocore.api.machine.trait.MachineTrait;
import com.extfro.extfrocore.api.machine.trait.MachineTraitType;
import com.extfro.extfrocore.common.capability.LocalizedHazardSavedData;
import com.extfro.extfrocore.config.ConfigHolder;

import net.minecraft.server.level.ServerLevel;

import lombok.Getter;
import lombok.Setter;

/**
 * trait for localized hazard (e.g. radiation) emitters like nuclear reactors.
 */
public class LocalizedHazardEmitterTrait extends MachineTrait {

    public static final MachineTraitType<LocalizedHazardEmitterTrait> TYPE = new MachineTraitType<>(
            LocalizedHazardEmitterTrait.class);

    @Getter
    @Setter
    private MedicalCondition conditionToEmit;
    @Getter
    @Setter
    private int conditionStrength;

    public LocalizedHazardEmitterTrait(MedicalCondition conditionToEmit,
                                       int defaultConditionStrength) {
        super();
        this.conditionToEmit = conditionToEmit;
        this.conditionStrength = defaultConditionStrength;
    }

    @Override
    public MachineTraitType<LocalizedHazardEmitterTrait> getTraitType() {
        return TYPE;
    }

    public void spreadHazard() {
        if (!ConfigHolder.INSTANCE.gameplay.environmentalHazards) {
            return;
        }

        if (getLevel() instanceof ServerLevel serverLevel) {
            IHazardParticleContainer container = GTCapabilityHelper.getHazardContainer(serverLevel,
                    getBlockPos().relative(getMachine().getFrontFacing()), getMachine().getFrontFacing().getOpposite());
            if (container != null &&
                    container.getHazardCanBeInserted(getConditionToEmit()) > getConditionStrength()) {
                container.addHazard(getConditionToEmit(), getConditionStrength());
                return;
            }

            var savedData = LocalizedHazardSavedData.getOrCreate(serverLevel);
            savedData.addSphericalZone(getBlockPos(), getConditionStrength(), false,
                    HazardProperty.HazardTrigger.INHALATION, getConditionToEmit());
        }
    }
}
