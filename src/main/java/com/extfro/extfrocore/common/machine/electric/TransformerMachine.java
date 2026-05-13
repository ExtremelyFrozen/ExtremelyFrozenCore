package com.extfro.extfrocore.common.machine.electric;

import com.extfro.extfrocore.api.EFValues;
import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.capability.IControllable;
import com.extfro.extfrocore.api.machine.TieredEnergyMachine;
import com.extfro.extfrocore.api.machine.property.GTMachineModelProperties;
import com.extfro.extfrocore.api.machine.trait.NotifiableEnergyContainer;
import com.extfro.extfrocore.api.sync_system.annotations.ClientFieldChangeListener;
import com.extfro.extfrocore.api.sync_system.annotations.SaveField;
import com.extfro.extfrocore.api.sync_system.annotations.SyncToClient;
import com.extfro.extfrocore.utils.ExtendedUseOnContext;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import lombok.Getter;
import lombok.Setter;

public class TransformerMachine extends TieredEnergyMachine implements IControllable {

    public static final BooleanProperty TRANSFORM_UP_PROPERTY = GTMachineModelProperties.IS_TRANSFORM_UP;

    @SaveField
    @SyncToClient
    @Getter
    private boolean isTransformUp;
    @SaveField
    @Getter
    @Setter
    private boolean isWorkingEnabled;
    @Getter
    private final int baseAmp;

    public TransformerMachine(BlockEntityCreationInfo info, int tier, int amps) {
        super(info, tier, getEnergyContainer(tier, amps));

        energyContainer.setSideInputCondition(s -> s == getFrontFacing() && isWorkingEnabled());
        energyContainer.setSideOutputCondition(s -> s != getFrontFacing() && isWorkingEnabled());
        this.isWorkingEnabled = true;
        this.baseAmp = amps;
    }

    private static NotifiableEnergyContainer getEnergyContainer(int tier, int amps) {
        NotifiableEnergyContainer energyContainer;
        long tierVoltage = EFValues.V[tier];
        energyContainer = new NotifiableEnergyContainer(tierVoltage * 8L, tierVoltage * 4, amps,
                tierVoltage,
                4L * amps);
        return energyContainer;
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////

    @SuppressWarnings("unused")
    @ClientFieldChangeListener(fieldName = "isTransformUp")
    private void onTransformUpdated() {
        updateEnergyContainer(isTransformUp);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        updateEnergyContainer(isTransformUp);
    }

    public void updateEnergyContainer(boolean isTransformUp) {
        long tierVoltage = EFValues.V[getTier()];
        int lowAmperage = baseAmp * 4;
        if (isTransformUp) {
            // storage = n amp high; input = tier / 4; amperage = 4n; output = tier; amperage = n
            this.energyContainer.resetBasicInfo(tierVoltage * 8L * lowAmperage, tierVoltage, lowAmperage,
                    tierVoltage * 4, baseAmp);
            energyContainer.setSideInputCondition(s -> s != getFrontFacing() && isWorkingEnabled());
            energyContainer.setSideOutputCondition(s -> s == getFrontFacing() && isWorkingEnabled());
        } else {
            // storage = n amp high; input = tier; amperage = n; output = tier / 4; amperage = 4n
            this.energyContainer.resetBasicInfo(tierVoltage * 8L * lowAmperage, tierVoltage * 4, baseAmp, tierVoltage,
                    lowAmperage);
            energyContainer.setSideInputCondition(s -> s == getFrontFacing() && isWorkingEnabled());
            energyContainer.setSideOutputCondition(s -> s != getFrontFacing() && isWorkingEnabled());
        }
    }

    @Override
    public int tintColor(int index) {
        if (index == 2) { // frontTexture
            return EFValues.VC[getTier() + 1];
        } else if (index == 3) { // otherTexture
            return EFValues.VC[getTier()];
        }
        return super.tintColor(index);
    }

    //////////////////////////////////////
    // ****** Interaction *******//
    //////////////////////////////////////

    public void setTransformUp(boolean isTransformUp) {
        if (this.isTransformUp != isTransformUp && !isRemote()) {
            this.isTransformUp = isTransformUp;
            syncDataHolder.markClientSyncFieldDirty("isTransformUp");
            updateEnergyContainer(isTransformUp);
            setRenderState(getRenderState().setValue(GTMachineModelProperties.IS_TRANSFORM_UP, isTransformUp));
        }
    }

    @Override
    protected InteractionResult onScrewdriverClick(ExtendedUseOnContext context) {
        if (!isRemote()) {
            setTransformUp(!isTransformUp());
            context.getPlayer().sendSystemMessage(Component.translatable(
                    isTransformUp() ? "gtceu.machine.transformer.message_transform_up" :
                            "gtceu.machine.transformer.message_transform_down",
                    energyContainer.getInputVoltage(), energyContainer.getInputAmperage(),
                    energyContainer.getOutputVoltage(), energyContainer.getOutputAmperage()));
        }
        return InteractionResult.CONSUME;
    }
}
