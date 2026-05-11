package com.extfro.extfrocore.common.network;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.utils.input.SyncedKeyMapping;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2BooleanMap;
import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;
import org.jetbrains.annotations.NotNull;

public class KeyDownPayload implements CustomPacketPayload {

    public static final ResourceLocation ID = ExtForCore.id("key_down");
    public static final Type<KeyDownPayload> TYPE = new Type<>(ID);
    public static final StreamCodec<ByteBuf, KeyDownPayload> CODEC = ByteBufCodecs
            .map(size -> (Int2BooleanMap) new Int2BooleanOpenHashMap(size), ByteBufCodecs.VAR_INT, ByteBufCodecs.BOOL)
            .map(KeyDownPayload::new, packet -> packet.updateKeys);

    private final Int2BooleanMap updateKeys;

    public KeyDownPayload(Int2BooleanMap updateKeys) {
        this.updateKeys = updateKeys;
    }

    public static void execute(KeyDownPayload packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        for (var entry : packet.updateKeys.int2BooleanEntrySet()) {
            SyncedKeyMapping keyMapping = SyncedKeyMapping.getFromSyncId(entry.getIntKey());
            if (keyMapping != null) {
                keyMapping.serverActivate(entry.getBooleanValue(), player);
            }
        }
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
