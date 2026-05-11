package com.extfro.extfrocore.api.data.worldgen.ores;

import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;

@FunctionalInterface
public interface OreBlockPlacer {

    void placeBlock(BulkSectionAccess access, LevelChunkSection section);
}
