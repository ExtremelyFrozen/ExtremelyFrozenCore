package com.extfro.extfrocore.api.sync_system;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.sync_system.holder.SyncDataHolder;
import com.extfro.extfrocore.api.sync_system.network.ClientBlockEntitySyncPayload;
import com.extfro.extfrocore.api.sync_system.network.ServerBlockEntitySyncPayload;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A BlockEntity that manages sync and save data via the {@code ISyncManaged} sync data system.
 *
 * @see ISyncManaged
 */
public abstract class ManagedSyncBlockEntity extends BlockEntity implements ISyncManaged {

    @Getter
    protected final SyncDataHolder syncDataHolder = new SyncDataHolder(this);
    @Getter
    @Setter
    private boolean isDirty;

    public ManagedSyncBlockEntity(BlockEntityCreationInfo info) {
        super(info.type(), info.pos(), info.state());
    }

    public ManagedSyncBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    protected final void saveAdditional(net.minecraft.nbt.CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        SyncTagMap syncData = SyncTagMap.empty();
        getSyncDataHolder().writeSaveData(registries, syncData);
        getSyncDataHolder().writeItemData(registries, syncData);
        syncData.values().forEach(tag::put);
    }

    @Override
    @MustBeInvokedByOverriders
    public void loadAdditional(net.minecraft.nbt.CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        boolean clientSide = getLevel() == null ? ExtForCore.isClientThread() : getLevel().isClientSide;
        SyncTagMap data = SyncTagMap.empty();
        for (String key : tag.getAllKeys()) {
            Tag value = tag.get(key);
            if (value != null) {
                data.put(key, value);
            }
        }
        getSyncDataHolder().deserializeData(registries, data, clientSide);
        if (!clientSide) {
            getSyncDataHolder().deserializeItemData(registries, data);
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        var level = Objects.requireNonNull(getLevel());
        SyncTagMap data = getSyncDataHolder().serializeToItemData(level.registryAccess());
        if (!data.isEmpty()) {
            components.set(SyncedComponents.BLOCK_ITEM_DATA.get(), data);
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput components) {
        super.applyImplicitComponents(components);
        var data = components.get(SyncedComponents.BLOCK_ITEM_DATA.get());
        if (data != null && getLevel() != null) {
            getSyncDataHolder().deserializeItemData(getLevel().registryAccess(), data);
        }
    }

    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        getSyncDataHolder().resyncAllFields();
        var tag = new net.minecraft.nbt.CompoundTag();
        getSyncDataHolder().serializeFullClientSyncData(registries).values().forEach(tag::put);
        return tag;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this,
                (blockEntity, registries) -> ((ManagedSyncBlockEntity) blockEntity).syncDataHolder.getPendingChanges()
                        .toVanillaTag());
    }

    @Override
    public final void markAsChanged() {
        isDirty = true;
    }

    public final void updateTick() {
        if (getLevel() instanceof ServerLevel serverLevel) {
            boolean hasSyncChanges = syncDataHolder.scanAndMarkChanges(serverLevel.registryAccess());
            if (hasSyncChanges) {
                setChanged();
                byte[] data = syncDataHolder.collectClientNetworkChanges(serverLevel.registryAccess(), false);
                if (data.length > 0) {
                    PacketDistributor.sendToPlayersTrackingChunk(serverLevel,
                            new ChunkPos(getBlockPos().getX() >> 4, getBlockPos().getZ() >> 4),
                            ServerBlockEntitySyncPayload.of(this, data));
                }
            }
            if (isDirty) {
                setChanged();
                serverLevel.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
                isDirty = false;
            }
        }
    }

    public final void handleClientUpdate(net.minecraft.core.RegistryAccess registries, byte[] data) {
        syncDataHolder.applyServerNetworkUpdate(registries, data);
    }

    public final void pushClientChangesToServer() {
        if (getLevel() instanceof ClientLevel clientLevel) {
            byte[] changes = syncDataHolder.collectServerNetworkChanges(clientLevel.registryAccess());
            if (changes.length > 0) {
                PacketDistributor.sendToServer(new ClientBlockEntitySyncPayload(getBlockPos().asLong(), changes));
            }
        }
    }
}
