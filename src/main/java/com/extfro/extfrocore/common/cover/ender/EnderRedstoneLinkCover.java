package com.extfro.extfrocore.common.cover.ender;

import com.extfro.extfrocore.api.capability.ICoverable;
import com.extfro.extfrocore.api.cover.CoverDefinition;
import com.extfro.extfrocore.api.misc.virtualregistry.EntryTypes;
import com.extfro.extfrocore.api.misc.virtualregistry.VirtualEntry;
import com.extfro.extfrocore.api.misc.virtualregistry.entries.VirtualRedstone;
import com.extfro.extfrocore.api.sync_system.SyncDataHolder;
import com.extfro.extfrocore.api.sync_system.annotations.SaveField;
import com.extfro.extfrocore.api.sync_system.annotations.SyncToClient;

import net.minecraft.core.Direction;

import com.lowdragmc.lowdraglib2.gui.widget.Widget;
import com.lowdragmc.lowdraglib2.gui.widget.WidgetGroup;
import lombok.Getter;

import java.util.UUID;

public class EnderRedstoneLinkCover extends AbstractEnderLinkCover<VirtualRedstone> {

    @Getter
    protected final SyncDataHolder syncDataHolder = new SyncDataHolder(this);

    @SaveField
    @SyncToClient
    private VirtualRedstone storage;
    @SaveField
    @SyncToClient
    private UUID uuid;

    public EnderRedstoneLinkCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
        if (!isRemote()) {
            uuid = UUID.randomUUID();
            setVirtualEntry();
        } else uuid = null;
    }

    @Override
    public boolean canAttach() {
        return true;
    }

    @Override
    protected String identifier() {
        return "ERLink#";
    }

    @Override
    protected VirtualRedstone getEntry() {
        return storage;
    }

    @Override
    protected void setEntry(VirtualEntry entry) {
        if (storage != null) storage.removeMember(uuid);
        storage = (VirtualRedstone) entry;
        storage.addMember(uuid);
        syncDataHolder.markClientSyncFieldDirty("storage");
    }

    @Override
    protected EntryTypes<VirtualRedstone> getEntryType() {
        return EntryTypes.ENDER_REDSTONE;
    }

    @Override
    protected void transfer() {
        switch (io) {
            case IN -> storage.setSignal(uuid, getSignalInput());
            case OUT -> setRedstoneSignalOutput(storage.getSignal());
        }
    }

    @Override
    protected Widget addVirtualEntryWidget(VirtualEntry entry, int x, int y, int width, int height, boolean canClick) {
        return new WidgetGroup(x, y, width, height);
    }

    @Override
    protected String getUITitle() {
        return "cover.ender_redstone_link.title";
    }

    @Override
    public boolean canConnectRedstone() {
        return true;
    }

    @Override
    public void onRemoved() {
        storage.removeMember(uuid);
        super.onRemoved();
    }

    protected int getSignalInput() {
        return coverHolder.getLevel().getSignal(coverHolder.getBlockPos().relative(attachedSide),
                attachedSide.getOpposite());
    }
}
