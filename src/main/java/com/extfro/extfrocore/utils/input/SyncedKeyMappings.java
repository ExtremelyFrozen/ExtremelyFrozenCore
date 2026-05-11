package com.extfro.extfrocore.utils.input;

import com.extfro.extfrocore.ExtForCore;

import net.minecraft.client.Minecraft;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.common.NeoForge;

public final class SyncedKeyMappings {

    public static final SyncedKeyMapping VANILLA_JUMP = SyncedKeyMapping
            .createFromMC(() -> () -> Minecraft.getInstance().options.keyJump);
    public static final SyncedKeyMapping VANILLA_SNEAK = SyncedKeyMapping
            .createFromMC(() -> () -> Minecraft.getInstance().options.keyShift);
    public static final SyncedKeyMapping VANILLA_FORWARD = SyncedKeyMapping
            .createFromMC(() -> () -> Minecraft.getInstance().options.keyUp);
    public static final SyncedKeyMapping VANILLA_BACKWARD = SyncedKeyMapping
            .createFromMC(() -> () -> Minecraft.getInstance().options.keyDown);
    public static final SyncedKeyMapping VANILLA_LEFT = SyncedKeyMapping
            .createFromMC(() -> () -> Minecraft.getInstance().options.keyLeft);
    public static final SyncedKeyMapping VANILLA_RIGHT = SyncedKeyMapping
            .createFromMC(() -> () -> Minecraft.getInstance().options.keyRight);

    public static void init() {
        if (ExtForCore.isClientSide()) {
            NeoForge.EVENT_BUS.register(SyncedKeyMapping.class);
        }
        ModLoader.postEvent(new SyncedKeyMappingEvent());
    }

    private SyncedKeyMappings() {}
}
