package com.extfro.extfrocore.api.machine.multiblock.part;

import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.capability.IControllable;
import com.extfro.extfrocore.api.capability.recipe.IO;
import com.extfro.extfrocore.api.sync_system.annotations.RerenderOnChanged;
import com.extfro.extfrocore.api.sync_system.annotations.SaveField;
import com.extfro.extfrocore.api.sync_system.annotations.SyncToClient;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

public class TieredIOPartMachine extends TieredPartMachine implements IControllable {

    protected final IO io;

    /**
     * AUTO IO working?
     */
    @Getter
    @SaveField
    @SyncToClient
    @RerenderOnChanged
    protected boolean workingEnabled;

    public TieredIOPartMachine(BlockEntityCreationInfo info, int tier, IO io) {
        super(info, tier);
        this.io = io;
        this.workingEnabled = true;
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        this.workingEnabled = workingEnabled;
        syncDataHolder.markClientSyncFieldDirty("workingEnabled");
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////

    @Nullable
    @Override
    public PageGroupingData getPageGroupingData() {
        return switch (this.io) {
            case IN -> new PageGroupingData("gtceu.multiblock.page_switcher.io.import", 1);
            case OUT -> new PageGroupingData("gtceu.multiblock.page_switcher.io.export", 2);
            case BOTH -> new PageGroupingData("gtceu.multiblock.page_switcher.io.both", 3);
            case NONE -> null;
        };
    }
}
