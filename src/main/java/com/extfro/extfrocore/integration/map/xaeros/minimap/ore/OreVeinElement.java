package com.extfro.extfrocore.integration.map.xaeros.minimap.ore;

import com.extfro.extfrocore.api.data.chemical.material.Material;
import com.extfro.extfrocore.api.data.worldgen.ores.GeneratedVeinMetadata;

import net.minecraft.client.Minecraft;

import lombok.Getter;

public class OreVeinElement {

    @Getter
    private final GeneratedVeinMetadata vein;
    @Getter
    private final String name;
    @Getter
    private final int cachedNameLength;

    public OreVeinElement(GeneratedVeinMetadata vein, String name) {
        this.vein = vein;
        this.name = name;

        this.cachedNameLength = Minecraft.getInstance().font.width(this.getName());
    }

    public Material getFirstMaterial() {
        return vein.definition().value().veinGenerator().getAllMaterials().getFirst();
    }
}
