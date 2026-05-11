package com.extfro.extfrocore.api.blockentity;

import com.extfro.extfrocore.api.sync_system.SyncTagMap;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface ICopyable {

    default SyncTagMap copyConfig(SyncTagMap tag) {
        return tag;
    }

    default void pasteConfig(ServerPlayer player, SyncTagMap tag) {}

    default List<ItemStack> getItemsRequiredToPaste() {
        return List.of();
    }
}
