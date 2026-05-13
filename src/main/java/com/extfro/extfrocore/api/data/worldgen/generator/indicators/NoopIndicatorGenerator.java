package com.extfro.extfrocore.api.data.worldgen.generator.indicators;

import com.extfro.extfrocore.api.data.chemical.material.Material;
import com.extfro.extfrocore.api.data.worldgen.generator.IndicatorGenerator;
import com.extfro.extfrocore.api.data.worldgen.ores.GeneratedVeinMetadata;
import com.extfro.extfrocore.api.data.worldgen.ores.OreIndicatorPlacer;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;

@NoArgsConstructor
public class NoopIndicatorGenerator extends IndicatorGenerator {

    public static final NoopIndicatorGenerator INSTANCE = new NoopIndicatorGenerator();
    public static final MapCodec<NoopIndicatorGenerator> CODEC = MapCodec.unit(() -> INSTANCE);

    @Override
    public Map<ChunkPos, OreIndicatorPlacer> generate(WorldGenLevel level, RandomSource random,
                                                      GeneratedVeinMetadata metadata) {
        // Nothing to do here
        return Collections.emptyMap();
    }

    @Override
    public int getSearchRadiusModifier(int veinRadius) {
        return 0;
    }

    @Nullable
    @Override
    public Either<BlockState, Material> block() {
        return null;
    }

    @Override
    public MapCodec<? extends IndicatorGenerator> codec() {
        return CODEC;
    }
}
