package com.extfro.extfrocore.api.machine.multiblock.part;

import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.capability.recipe.IO;
import com.extfro.extfrocore.api.machine.feature.IControllable;
import com.extfro.extfrocore.api.sync_system.annotations.RerenderOnChanged;
import com.extfro.extfrocore.api.sync_system.annotations.SaveField;
import com.extfro.extfrocore.api.sync_system.annotations.SyncToClient;

import lombok.Getter;

public class TieredIOPartMachine extends TieredPartMachine implements IControllable {

    @Getter
    protected final IO io;
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
        syncDataHolder.resyncAllFields();
        markAsChanged();
    }
}
