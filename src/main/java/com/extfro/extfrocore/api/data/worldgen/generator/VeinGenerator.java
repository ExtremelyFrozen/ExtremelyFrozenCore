package com.extfro.extfrocore.api.data.worldgen.generator;

import com.extfro.extfrocore.api.data.worldgen.OreDefinition;
import com.extfro.extfrocore.api.data.worldgen.WorldGeneratorUtils;
import com.extfro.extfrocore.api.data.worldgen.ores.OreBlockPlacer;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public abstract class VeinGenerator {

    public static final Codec<MapCodec<? extends VeinGenerator>> REGISTRY_CODEC = ResourceLocation.CODEC
            .flatXmap(id -> Optional.ofNullable(WorldGeneratorUtils.VEIN_GENERATORS.get(id))
                    .map(DataResult::success)
                    .orElseGet(() -> DataResult.error(() -> "No VeinGenerator with id " + id + " registered")),
                    codec -> Optional.ofNullable(WorldGeneratorUtils.VEIN_GENERATORS.inverse().get(codec))
                            .map(DataResult::success)
                            .orElseGet(() -> DataResult.error(() -> "VeinGenerator " + codec + " not registered")));
    public static final Codec<VeinGenerator> DIRECT_CODEC =
            REGISTRY_CODEC.dispatchStable(VeinGenerator::codec, Function.identity());

    public abstract List<VeinEntry> getAllEntries();

    public List<BlockState> getAllBlocks() {
        return getAllEntries().stream()
                .map(VeinEntry::state)
                .toList();
    }

    public IntList getAllChances() {
        return IntArrayList.toList(getAllEntries().stream().mapToInt(VeinEntry::chance));
    }

    public abstract Map<BlockPos, OreBlockPlacer> generate(WorldGenLevel level, RandomSource random,
                                                           OreDefinition definition, BlockPos origin);

    public abstract VeinGenerator build();

    public abstract VeinGenerator copy();

    public abstract MapCodec<? extends VeinGenerator> codec();

    public record VeinEntry(BlockState state, int chance) {

        public static VeinEntry ofBlock(BlockState state, int chance) {
            return new VeinEntry(state, chance);
        }
    }

    public static List<VeinEntry> mapTarget(List<OreConfiguration.TargetBlockState> target, int weight) {
        return target.stream().map(state -> new VeinEntry(state.state, weight)).toList();
    }
}
