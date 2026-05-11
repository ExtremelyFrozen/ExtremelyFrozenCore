package com.extfro.extfrocore.api.data.worldgen.ores;

import com.extfro.extfrocore.api.data.worldgen.OreDefinition;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

import java.util.function.Function;

public final class OreVeinUtil {

    private OreVeinUtil() {}

    public static boolean canPlaceOre(BlockState state, Function<BlockPos, BlockState> adjacentStateAccessor,
                                      RandomSource random, OreDefinition definition,
                                      OreConfiguration.TargetBlockState targetState,
                                      BlockPos mutablePos) {
        if (!targetState.target.test(state, random)) {
            return false;
        }
        if (shouldSkipAirCheck(random, definition.discardChanceOnAirExposure())) {
            return true;
        }
        return !Feature.isAdjacentToAir(adjacentStateAccessor, mutablePos);
    }

    public static boolean canPlaceOre(BlockState state, Function<BlockPos, BlockState> adjacentStateAccessor,
                                      RandomSource random, OreDefinition definition,
                                      BlockPos mutablePos) {
        if (!definition.layer().getTarget().test(state, random)) {
            return false;
        }
        if (shouldSkipAirCheck(random, definition.discardChanceOnAirExposure())) {
            return true;
        }
        return !Feature.isAdjacentToAir(adjacentStateAccessor, mutablePos);
    }

    protected static boolean shouldSkipAirCheck(RandomSource random, float chance) {
        return chance <= 0 || (!(chance >= 1) && random.nextFloat() >= chance);
    }
}
