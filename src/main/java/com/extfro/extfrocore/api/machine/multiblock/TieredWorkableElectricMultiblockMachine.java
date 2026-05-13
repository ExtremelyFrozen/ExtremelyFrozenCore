package com.extfro.extfrocore.api.machine.multiblock;

import com.extfro.extfrocore.api.EFValues;
import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.machine.feature.IOverclockMachine;
import com.extfro.extfrocore.api.machine.feature.ITieredMachine;
import com.extfro.extfrocore.api.sync_system.annotations.SaveField;

import lombok.Getter;

/**
 * @author screret
 * @date 2023/7/11
 * @implNote TieredWorkableElectricMultiblockMachine
 */
public class TieredWorkableElectricMultiblockMachine extends WorkableElectricMultiblockMachine
                                                     implements ITieredMachine, IOverclockMachine {

    private final int tier;
    @SaveField
    @Getter
    protected int overclockTier;

    public TieredWorkableElectricMultiblockMachine(BlockEntityCreationInfo info, int tier) {
        super(info);
        this.tier = tier;
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////

    //////////////////////////////////////
    // ******** OVERCLOCK *********//
    //////////////////////////////////////
    @Override
    public int getMinOverclockTier() {
        return 0;
    }

    @Override
    public void setOverclockTier(int tier) {
        if (!isRemote() && tier >= getMinOverclockTier() && tier <= getMaxOverclockTier()) {
            this.overclockTier = tier;
            this.recipeLogic.markLastRecipeDirty();
        }
    }

    @Override
    public long getOverclockVoltage() {
        return Math.min(EFValues.V[getOverclockTier()], super.getOverclockVoltage());
    }

    //////////////////////////////////////
    // ****** RECIPE LOGIC *******//
    //////////////////////////////////////
    @Override
    public int getTier() {
        return Math.min(tier, super.getTier());
    }

    @Override
    public long getMaxVoltage() {
        return Math.min(EFValues.V[tier], super.getMaxVoltage());
    }
}
