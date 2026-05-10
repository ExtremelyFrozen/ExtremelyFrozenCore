package com.extfro.extfrocore.api.sync_system.network;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.sync_system.ManagedSyncBlockEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.jetbrains.annotations.NotNull;

public record ServerBlockEntitySyncPayload(long pos, byte[] data) implements CustomPacketPayload {

    public static final ResourceLocation ID = ExtForCore.id("server_be_sync");
    public static final Type<ServerBlockEntitySyncPayload> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerBlockEntitySyncPayload> CODEC = StreamCodec.ofMember(
            ServerBlockEntitySyncPayload::write,
            ServerBlockEntitySyncPayload::decode);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeLong(pos);
        buf.writeByteArray(data);
    }

    private static ServerBlockEntitySyncPayload decode(RegistryFriendlyByteBuf buf) {
        return new ServerBlockEntitySyncPayload(buf.readLong(), buf.readByteArray());
    }

    public static void execute(ServerBlockEntitySyncPayload packet, IPayloadContext context) {
        var level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        if (!(level.getBlockEntity(BlockPos.of(packet.pos)) instanceof ManagedSyncBlockEntity blockEntity)) {
            return;
        }
        blockEntity.getSyncDataHolder().applyClientNetworkUpdate(level.registryAccess(), packet.data);
    }

    public static ServerBlockEntitySyncPayload of(BlockEntity blockEntity, byte[] data) {
        return new ServerBlockEntitySyncPayload(blockEntity.getBlockPos().asLong(), data);
    }
}
