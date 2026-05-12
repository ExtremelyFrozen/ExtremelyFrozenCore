package com.extfro.extfrocore.integration.map;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.EFValues;
import com.extfro.extfrocore.config.ConfigHolder;
import com.extfro.extfrocore.integration.map.ftbchunks.FTBChunksWaypointHandler;
import com.extfro.extfrocore.integration.map.journeymap.JourneymapWaypointHandler;
import com.extfro.extfrocore.integration.map.xaeros.XaeroWaypointHandler;
import com.extfro.extfrocore.utils.GTMath;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

public class WaypointManager {

    public static ResourceKey<Level> currentDimension;

    @Getter
    private static boolean active = false;

    public static boolean isActive() {
        return active;
    }

    public static void init() {
        var toggle = ConfigHolder.INSTANCE.compat.minimap.toggle;
        if (toggle.xaerosMapIntegration && ExtForCore.isModLoaded(EFValues.MODID_XAEROS_MINIMAP)) {
            WaypointManager.registerWaypointHandler(new XaeroWaypointHandler());
            active = true;
        }
        if (toggle.journeyMapIntegration && ExtForCore.isModLoaded(EFValues.MODID_JOURNEYMAP)) {
            WaypointManager.registerWaypointHandler(new JourneymapWaypointHandler());
            active = true;
        }
        if (toggle.ftbChunksIntegration && ExtForCore.isModLoaded(EFValues.MODID_FTB_CHUNKS)) {
            WaypointManager.registerWaypointHandler(new FTBChunksWaypointHandler());
            active = true;
        }
    }

    private static final Set<IWaypointHandler> handlers = new HashSet<>();
    private static final Object2ObjectMap<String, WaypointKey> waypoints = new Object2ObjectArrayMap<>();

    public static void updateDimension(LevelAccessor dim) {
        if (dim instanceof ClientLevel level) {
            currentDimension = level.dimension();
        }
    }

    public static void setWaypoint(String key, String name, int color, ResourceKey<Level> dim, int x, int y, int z) {
        if (dim == null) dim = currentDimension;
        for (IWaypointHandler handler : handlers) {
            handler.setWaypoint(key, name, color, dim, x, y, z);
        }
        waypoints.put(key, new WaypointKey(dim, x, y, z));
    }

    public static void removeWaypoint(String key) {
        for (IWaypointHandler handler : handlers) {
            handler.removeWaypoint(key);
        }
        waypoints.remove(key);
    }

    public static boolean toggleWaypoint(String key, String name, int color, ResourceKey<Level> dim, int x, int y,
                                         int z) {
        if (dim == null) dim = currentDimension;
        if ((new WaypointKey(dim, x, y, z)).equals(waypoints.get(key))) {
            removeWaypoint(key);
            return false;
        }
        setWaypoint(key, name, color, dim, x, y, z);
        return true;
    }

    public static void registerWaypointHandler(IWaypointHandler handler) {
        handlers.add(handler);
    }

    private static class WaypointKey {

        ResourceKey<Level> dim;
        int x, y, z;

        public WaypointKey(ResourceKey<Level> dim, int x, int y, int z) {
            this.dim = dim;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            WaypointKey that = (WaypointKey) o;
            return dim == that.dim && x == that.x && y == that.y && z == that.z;
        }

        @Override
        public int hashCode() {
            return GTMath.hashInts(dim.hashCode(), x, y, z);
        }
    }
}
