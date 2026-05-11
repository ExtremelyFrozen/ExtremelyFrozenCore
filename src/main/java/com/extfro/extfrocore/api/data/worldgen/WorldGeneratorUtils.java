package com.extfro.extfrocore.api.data.worldgen;

import com.extfro.extfrocore.api.data.worldgen.generator.IndicatorGenerator;
import com.extfro.extfrocore.api.data.worldgen.generator.VeinGenerator;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import com.google.common.collect.HashBiMap;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class WorldGeneratorUtils {

    public static final SortedMap<String, IWorldGenLayer> WORLD_GEN_LAYERS = new Object2ObjectLinkedOpenHashMap<>();
    public static final HashBiMap<ResourceLocation, MapCodec<? extends VeinGenerator>> VEIN_GENERATORS =
            HashBiMap.create();
    public static final HashBiMap<ResourceLocation, Supplier<? extends VeinGenerator>> VEIN_GENERATOR_FUNCTIONS =
            HashBiMap.create();
    public static final HashBiMap<ResourceLocation, MapCodec<? extends IndicatorGenerator>> INDICATOR_GENERATORS =
            HashBiMap.create();
    public static final HashBiMap<ResourceLocation, Supplier<? extends IndicatorGenerator>> INDICATOR_GENERATOR_FUNCTIONS =
            HashBiMap.create();

    private WorldGeneratorUtils() {}

    public static IWorldGenLayer registerWorldGenLayer(IWorldGenLayer layer) {
        WORLD_GEN_LAYERS.put(layer.getSerializedName(), layer);
        return layer;
    }

    public static IWorldGenLayer getWorldGenLayer(String name) {
        return WORLD_GEN_LAYERS.getOrDefault(name, IWorldGenLayer.NOWHERE);
    }

    public static Optional<String> getWorldGenLayerKey(IWorldGenLayer layer) {
        return WORLD_GEN_LAYERS.entrySet().stream()
                .filter(entry -> entry.getValue().equals(layer))
                .map(Map.Entry::getKey)
                .findFirst();
    }

    public static boolean isSameDimension(ResourceKey<Level> first, ResourceKey<Level> second) {
        return first == second;
    }

    public static <T> Map<ChunkPos, Map<BlockPos, T>> groupByChunks(Map<BlockPos, T> input) {
        return input.entrySet().stream().collect(Collectors.groupingBy(
                entry -> new ChunkPos(entry.getKey()),
                Object2ObjectOpenHashMap::new,
                Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, Object2ObjectOpenHashMap::new)));
    }

    public static Map<ChunkPos, List<BlockPos>> groupByChunks(Collection<BlockPos> positions) {
        return positions.stream().collect(Collectors.groupingBy(ChunkPos::new));
    }

    public static Collection<ChunkPos> getChunks(Collection<BlockPos> positions) {
        return positions.stream()
                .collect(Collectors.groupingBy(ChunkPos::new))
                .keySet();
    }

    public static void generateChunks(WorldGenLevel level, ChunkStatus requiredStatus, Collection<ChunkPos> chunks) {
        List<ChunkPos> previouslyUnloadedChunks = new ObjectArrayList<>();
        var chunkSource = level.getChunkSource();

        for (ChunkPos chunkPos : chunks) {
            var chunk = chunkSource.getChunk(chunkPos.x, chunkPos.z, false);

            if (chunk == null) {
                previouslyUnloadedChunks.add(chunkPos);
            }

            chunkSource.getChunk(chunkPos.x, chunkPos.z, requiredStatus, true);
        }

        if (level instanceof ServerLevel serverLevel) {
            previouslyUnloadedChunks.forEach(chunk -> serverLevel.unload(serverLevel.getChunk(chunk.x, chunk.z)));
        }
    }

    public static Optional<BlockPos> findBlockPos(BlockPos initialPos, Predicate<BlockPos> predicate,
                                                  Consumer<BlockPos.MutableBlockPos> step, int maxSteps) {
        var currentPos = initialPos.mutable();

        while (maxSteps-- >= 0) {
            step.accept(currentPos);

            if (predicate.test(currentPos)) {
                return Optional.of(currentPos.immutable());
            }
        }

        return Optional.empty();
    }
}
