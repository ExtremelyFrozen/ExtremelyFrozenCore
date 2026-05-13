package com.extfro.extfrocore.integration.map;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.EFValues;
import com.extfro.extfrocore.api.data.worldgen.ores.GeneratedVeinMetadata;
import com.extfro.extfrocore.api.gui.misc.ProspectorMode;
import com.extfro.extfrocore.config.ConfigHolder;
import com.extfro.extfrocore.integration.map.ftbchunks.FTBChunksRenderer;
import com.extfro.extfrocore.integration.map.journeymap.JourneymapRenderer;
import com.extfro.extfrocore.integration.map.xaeros.XaerosRenderer;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * A map renderer that groups together multiple map mods' renderers for supporting multiple simultaneous mods.
 */
public class GroupingMapRenderer extends GenericMapRenderer {

    @Getter
    private static final GroupingMapRenderer instance;

    static {
        Map<String, GenericMapRenderer> renderers = new HashMap<>();
        var toggle = ConfigHolder.INSTANCE.compat.minimap.toggle;
        if (toggle.journeyMapIntegration && ExtForCore.isModLoaded(EFValues.MODID_JOURNEYMAP)) {
            renderers.put(EFValues.MODID_JOURNEYMAP, new JourneymapRenderer());
        }
        if (toggle.xaerosMapIntegration && ExtForCore.isModLoaded(EFValues.MODID_XAEROS_MINIMAP)) {
            renderers.put(EFValues.MODID_XAEROS_MINIMAP, new XaerosRenderer());
        }
        if (toggle.ftbChunksIntegration && ExtForCore.isModLoaded(EFValues.MODID_FTB_CHUNKS)) {
            renderers.put(EFValues.MODID_FTB_CHUNKS, new FTBChunksRenderer());
        }

        instance = new GroupingMapRenderer(renderers);
    }

    @Getter
    private final Map<String, GenericMapRenderer> renderers;
    private final GenericMapRenderer[] rendererList;

    public GroupingMapRenderer(Map<String, GenericMapRenderer> renderers) {
        super(false);
        this.renderers = renderers;
        this.rendererList = renderers.values().toArray(GenericMapRenderer[]::new);
    }

    @Override
    public boolean addMarker(String name, String id, ResourceKey<Level> dim, ChunkPos pos,
                             ProspectorMode.FluidInfo fluid) {
        boolean value = false;
        for (GenericMapRenderer renderer : rendererList) {
            value |= renderer.addMarker(name, id, dim, pos, fluid);
        }
        return value;
    }

    @Override
    public boolean addMarker(String name, ResourceKey<Level> dim, GeneratedVeinMetadata vein, String id) {
        boolean value = false;
        for (GenericMapRenderer renderer : rendererList) {
            value |= renderer.addMarker(name, dim, vein, id);
        }
        return value;
    }

    @Override
    public boolean removeMarker(ResourceKey<Level> dim, String id) {
        boolean value = false;
        for (GenericMapRenderer renderer : rendererList) {
            value |= renderer.removeMarker(dim, id);
        }
        return value;
    }

    @Override
    public boolean doShowLayer(String name) {
        boolean value = false;
        for (GenericMapRenderer renderer : rendererList) {
            value |= renderer.doShowLayer(name);
        }
        return value;
    }

    @Override
    public void setLayerActive(String name, boolean active) {
        for (GenericMapRenderer renderer : rendererList) {
            renderer.setLayerActive(name, active);
        }
    }

    @Override
    public void clear() {
        for (GenericMapRenderer renderer : rendererList) {
            renderer.clear();
        }
    }
}
