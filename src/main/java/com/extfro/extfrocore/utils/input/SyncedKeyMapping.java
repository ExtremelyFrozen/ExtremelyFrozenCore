package com.extfro.extfrocore.utils.input;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.common.network.KeyDownPayload;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.network.PacketDistributor;

import com.mojang.blaze3d.platform.InputConstants;
import it.unimi.dsi.fastutil.ints.Int2BooleanMap;
import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Supplier;

public final class SyncedKeyMapping {

    private static final Int2ObjectMap<SyncedKeyMapping> KEY_MAPPINGS = new Int2ObjectOpenHashMap<>();
    private static int syncIndex = 0;

    @OnlyIn(Dist.CLIENT)
    private KeyMapping keyMapping;
    @OnlyIn(Dist.CLIENT)
    private Supplier<Supplier<KeyMapping>> keyMappingGetter;
    private final boolean needsRegister;
    @OnlyIn(Dist.CLIENT)
    private int keyCode;
    @OnlyIn(Dist.CLIENT)
    private boolean isKeyDown;

    private static final Int2BooleanMap UPDATING_KEY_DOWN = new Int2BooleanOpenHashMap();

    private final WeakHashMap<ServerPlayer, Boolean> serverMapping = new WeakHashMap<>();
    private final WeakHashMap<ServerPlayer, Set<IKeyPressedListener>> playerListeners = new WeakHashMap<>();
    private final Set<IKeyPressedListener> globalListeners = Collections.newSetFromMap(new WeakHashMap<>());

    private SyncedKeyMapping(Supplier<Supplier<KeyMapping>> mcKeyMapping) {
        if (ExtForCore.isClientSide()) {
            this.keyMappingGetter = mcKeyMapping;
        }
        this.needsRegister = false;
        KEY_MAPPINGS.put(syncIndex++, this);
    }

    private SyncedKeyMapping(int keyCode) {
        if (ExtForCore.isClientSide() && !ExtForCore.isDataGen()) {
            this.keyCode = keyCode;
        }
        this.needsRegister = false;
        KEY_MAPPINGS.put(syncIndex++, this);
    }

    private SyncedKeyMapping(String nameKey, IKeyConflictContext ctx, int keyCode, String category) {
        if (ExtForCore.isClientSide() && !ExtForCore.isDataGen()) {
            this.keyMapping = createKeyMapping(nameKey, ctx, keyCode, category);
        }
        this.needsRegister = true;
        KEY_MAPPINGS.put(syncIndex++, this);
    }

    public static @NotNull SyncedKeyMapping createFromMC(@NotNull Supplier<Supplier<KeyMapping>> mcKeyMapping) {
        return new SyncedKeyMapping(mcKeyMapping);
    }

    public static @NotNull SyncedKeyMapping create(int keyCode) {
        return new SyncedKeyMapping(keyCode);
    }

    public static @NotNull SyncedKeyMapping createConfigurable(@NotNull String nameKey,
                                                               @NotNull IKeyConflictContext ctx,
                                                               int keyCode) {
        return createConfigurable(nameKey, ctx, keyCode, ExtForCore.MOD_NAME);
    }

    public static @NotNull SyncedKeyMapping createConfigurable(@NotNull String nameKey,
                                                               @NotNull IKeyConflictContext ctx,
                                                               int keyCode,
                                                               @NotNull String category) {
        return new SyncedKeyMapping(nameKey, ctx, keyCode, category);
    }

    @OnlyIn(Dist.CLIENT)
    private @NotNull KeyMapping createKeyMapping(@NotNull String nameKey, @NotNull IKeyConflictContext ctx, int keyCode,
                                                 @NotNull String category) {
        return new KeyMapping(nameKey, ctx, InputConstants.Type.KEYSYM, keyCode, category);
    }

    public boolean isKeyDown() {
        if (ExtForCore.isClientSide()) {
            return isKeyDownClient();
        }
        return false;
    }

    public boolean isKeyDown(@NotNull Player player) {
        if (player.level().isClientSide()) {
            return isKeyDownClient();
        }
        Boolean isKeyDown = serverMapping.get((ServerPlayer) player);
        return isKeyDown != null && isKeyDown;
    }

    private boolean isKeyDownClient() {
        if (keyMapping != null) {
            return keyMapping.isDown();
        }
        long id = Minecraft.getInstance().getWindow().getWindow();
        return InputConstants.isKeyDown(id, keyCode);
    }

    public @NotNull SyncedKeyMapping registerPlayerListener(@NotNull ServerPlayer player,
                                                            @NotNull IKeyPressedListener listener) {
        Set<IKeyPressedListener> listenerSet = playerListeners
                .computeIfAbsent(player, $ -> Collections.newSetFromMap(new WeakHashMap<>()));
        listenerSet.add(listener);
        return this;
    }

    @ApiStatus.Internal
    public static void onRegisterKeyBinds(@NotNull RegisterKeyMappingsEvent event) {
        for (SyncedKeyMapping value : KEY_MAPPINGS.values()) {
            if (value.keyMappingGetter != null) {
                value.keyMapping = value.keyMappingGetter.get().get();
                value.keyMappingGetter = null;
            }
            if (value.keyMapping != null && value.needsRegister) {
                event.register(value.keyMapping);
            }
        }
    }

    public void removePlayerListener(@NotNull ServerPlayer player, @NotNull IKeyPressedListener listener) {
        Set<IKeyPressedListener> listenerSet = playerListeners.get(player);
        if (listenerSet != null) {
            listenerSet.remove(listener);
        }
    }

    public @NotNull SyncedKeyMapping registerGlobalListener(@NotNull IKeyPressedListener listener) {
        globalListeners.add(listener);
        return this;
    }

    public void removeGlobalListener(@NotNull IKeyPressedListener listener) {
        globalListeners.remove(listener);
    }

    @ApiStatus.Internal
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onClientTick(ClientTickEvent.Pre event) {
        UPDATING_KEY_DOWN.clear();
        for (var entry : KEY_MAPPINGS.int2ObjectEntrySet()) {
            SyncedKeyMapping keyMapping = entry.getValue();
            boolean previousKeyDown = keyMapping.isKeyDown;

            if (keyMapping.keyMapping != null) {
                keyMapping.isKeyDown = keyMapping.keyMapping.isDown();
            } else {
                long id = Minecraft.getInstance().getWindow().getWindow();
                keyMapping.isKeyDown = InputConstants.isKeyDown(id, keyMapping.keyCode);
            }

            if (previousKeyDown != keyMapping.isKeyDown) {
                UPDATING_KEY_DOWN.put(entry.getIntKey(), keyMapping.isKeyDown);
            }
        }
        if (!UPDATING_KEY_DOWN.isEmpty()) {
            PacketDistributor.sendToServer(new KeyDownPayload(UPDATING_KEY_DOWN));
        }
    }

    @ApiStatus.Internal
    public void serverActivate(boolean keyDown, ServerPlayer player) {
        this.serverMapping.put(player, keyDown);

        Set<IKeyPressedListener> listenerSet = playerListeners.get(player);
        if (listenerSet != null && !listenerSet.isEmpty()) {
            for (IKeyPressedListener listener : listenerSet) {
                listener.onKeyPressed(player, this, keyDown);
            }
        }
        for (IKeyPressedListener listener : globalListeners) {
            listener.onKeyPressed(player, this, keyDown);
        }
    }

    @ApiStatus.Internal
    public static SyncedKeyMapping getFromSyncId(int id) {
        return KEY_MAPPINGS.get(id);
    }
}
