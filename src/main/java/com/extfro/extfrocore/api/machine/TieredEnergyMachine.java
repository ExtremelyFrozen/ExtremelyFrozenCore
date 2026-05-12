package com.extfro.extfrocore.api.machine;

import com.extfro.extfrocore.api.EFValues;
import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.gui.editor.EditableUI;
import com.extfro.extfrocore.api.machine.feature.ITieredMachine;
import com.extfro.extfrocore.api.machine.trait.EnvironmentalExplosionTrait;
import com.extfro.extfrocore.api.machine.trait.NotifiableEnergyContainer;
import com.extfro.extfrocore.api.sync_system.annotations.SaveField;
import com.extfro.extfrocore.api.sync_system.annotations.SyncToClient;

import net.minecraft.util.Mth;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib2.gui.widget.ProgressWidget;
import lombok.Getter;

import java.util.function.Function;

public class TieredEnergyMachine extends TieredMachine implements ITieredMachine {

    @SaveField
    @SyncToClient
    public final NotifiableEnergyContainer energyContainer;
    @Getter
    protected final EnvironmentalExplosionTrait environmentalExplosionTrait;

    public TieredEnergyMachine(BlockEntityCreationInfo info, int tier,
                               Function<TieredEnergyMachine, NotifiableEnergyContainer> energyContainerSupplier) {
        super(info, tier);
        energyContainer = energyContainerSupplier.apply(this);
        environmentalExplosionTrait = new EnvironmentalExplosionTrait(this, tier, tier * 10,
                () -> energyContainer.getEnergyStored() > 0);
    }

    public TieredEnergyMachine(BlockEntityCreationInfo info, int tier, NotifiableEnergyContainer energyContainer) {
        this(info, tier, machine -> machine.attachTrait(energyContainer));
    }

    public TieredEnergyMachine(BlockEntityCreationInfo info, int tier) {
        super(info, tier);

        long tierVoltage = EFValues.V[tier];
        if (isEnergyEmitter()) {
            energyContainer = NotifiableEnergyContainer.emitterContainer(this,
                    tierVoltage * 64L, tierVoltage, getMaxInputOutputAmperage());
        } else {
            energyContainer = NotifiableEnergyContainer.receiverContainer(this,
                    tierVoltage * 64L, tierVoltage, getMaxInputOutputAmperage());
        }
        environmentalExplosionTrait = new EnvironmentalExplosionTrait(this, tier, tier * 10,
                () -> energyContainer.getEnergyStored() > 0);
    }

    //////////////////////////////////////
    // ********** MISC ***********//
    //////////////////////////////////////
    @Override
    public int getAnalogOutputSignal() {
        long energyStored = energyContainer.getEnergyStored();
        long energyCapacity = energyContainer.getEnergyCapacity();
        float f = energyCapacity == 0L ? 0.0f : energyStored / (energyCapacity * 1.0f);
        return Mth.floor(f * 14.0f) + (energyStored > 0 ? 1 : 0);
    }

    /**
     * Determines max input or output amperage used by this meta tile entity
     * if emitter, it determines size of energy packets it will emit at once
     * if receiver, it determines max input energy per request
     *
     * @return max amperage received or emitted by this machine
     */
    protected long getMaxInputOutputAmperage() {
        return 1L;
    }

    /**
     * Determines if this meta tile entity is in energy receiver or emitter mode
     *
     * @return true if machine emits energy to network, false it it accepts energy from network
     */
    protected boolean isEnergyEmitter() {
        return false;
    }

    /**
     * Create an energy bar widget.
     */
    protected static EditableUI<ProgressWidget, TieredEnergyMachine> createEnergyBar() {
        return new EditableUI<>("energy_container", ProgressWidget.class, () -> {
            var progressBar = new ProgressWidget(ProgressWidget.JEIProgress, 0, 0, 18, 60,
                    new ProgressTexture(IGuiTexture.EMPTY, GuiTextures.ENERGY_BAR_BASE));
            progressBar.setFillDirection(ProgressTexture.FillDirection.DOWN_TO_UP);
            progressBar.setBackground(GuiTextures.ENERGY_BAR_BACKGROUND);
            return progressBar;
        }, (progressBar, machine) -> progressBar.setProgressSupplier(
                () -> machine.energyContainer.getEnergyStored() * 1d / machine.energyContainer.getEnergyCapacity()));
    }
}
