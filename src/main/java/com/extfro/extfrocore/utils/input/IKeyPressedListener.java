package com.extfro.extfrocore.utils.input;

import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface IKeyPressedListener {

    void onKeyPressed(ServerPlayer player, SyncedKeyMapping keyPressed, boolean isDown);
}
