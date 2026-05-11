package com.extfro.extfrocore.api.data.worldgen.generator;

import com.extfro.extfrocore.api.data.worldgen.WorldGeneratorUtils;
import com.extfro.extfrocore.api.data.worldgen.ores.GeneratedVeinMetadata;
import com.extfro.extfrocore.api.data.worldgen.ores.OreIndicatorPlacer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public abstract class IndicatorGenerator {

    public static final Codec<MapCodec<? extends IndicatorGenerator>> REGISTRY_CODEC = ResourceLocation.CODEC
            .flatXmap(id -> Optional.ofNullable(WorldGeneratorUtils.INDICATOR_GENERATORS.get(id))
                    .map(DataResult::success)
                    .orElseGet(() -> DataResult.error(() -> "No IndicatorGenerator with id " + id + " registered")),
                    codec -> Optional.ofNullable(WorldGeneratorUtils.INDICATOR_GENERATORS.inverse().get(codec))
                            .map(DataResult::success)
                            .orElseGet(() -> DataResult.error(() -> "IndicatorGenerator " + codec + " not registered")));

    public static final Codec<IndicatorGenerator> DIRECT_CODEC =
            REGISTRY_CODEC.dispatchStable(IndicatorGenerator::codec, Function.identity());

    public abstract Map<ChunkPos, OreIndicatorPlacer> generate(WorldGenLevel level, RandomSource random,
                                                               GeneratedVeinMetadata metadata);

    @Nullable
    public abstract BlockState block();

    public abstract MapCodec<? extends IndicatorGenerator> codec();

    public abstract int getSearchRadiusModifier(int veinRadius);
}
