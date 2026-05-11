package com.extfro.extfrocore.api.data.worldgen.generator.indicators;

import com.extfro.extfrocore.api.data.worldgen.WorldGeneratorUtils;
import com.extfro.extfrocore.api.data.worldgen.generator.IndicatorGenerator;
import com.extfro.extfrocore.api.data.worldgen.ores.GeneratedVeinMetadata;
import com.extfro.extfrocore.api.data.worldgen.ores.OreIndicatorPlacer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.levelgen.Heightmap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@NoArgsConstructor
public class SurfaceIndicatorGenerator extends IndicatorGenerator {

    public static final MapCodec<SurfaceIndicatorGenerator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(
                    BlockState.CODEC.fieldOf("block").forGetter(generator -> generator.block),
                    IntProvider.codec(1, 32).fieldOf("radius").forGetter(generator -> generator.radius),
                    FloatProvider.codec(0.0f, 2.0f).fieldOf("density").forGetter(generator -> generator.density),
                    IndicatorPlacement.CODEC.fieldOf("placement").forGetter(generator -> generator.placement))
            .apply(instance, SurfaceIndicatorGenerator::new));

    private BlockState block = Blocks.AIR.defaultBlockState();
    private IntProvider radius = ConstantInt.of(5);
    private FloatProvider density = ConstantFloat.of(0.2f);
    private IndicatorPlacement placement = IndicatorPlacement.SURFACE;

    public SurfaceIndicatorGenerator(BlockState block, IntProvider radius, FloatProvider density,
                                     IndicatorPlacement placement) {
        this.block = block;
        this.radius = radius;
        this.density = density;
        this.placement = placement;
    }

    public SurfaceIndicatorGenerator block(Block block) {
        return state(block.defaultBlockState());
    }

    public SurfaceIndicatorGenerator state(BlockState state) {
        this.block = state;
        return this;
    }

    public SurfaceIndicatorGenerator radius(int radius) {
        return radius(ConstantInt.of(radius));
    }

    public SurfaceIndicatorGenerator radius(IntProvider provider) {
        this.radius = provider;
        return this;
    }

    public SurfaceIndicatorGenerator density(float density) {
        return density(ConstantFloat.of(density));
    }

    public SurfaceIndicatorGenerator density(FloatProvider provider) {
        this.density = provider;
        return this;
    }

    public SurfaceIndicatorGenerator placement(IndicatorPlacement placement) {
        this.placement = placement;
        return this;
    }

    @Override
    public Map<ChunkPos, OreIndicatorPlacer> generate(WorldGenLevel level, RandomSource random,
                                                      GeneratedVeinMetadata metadata) {
        BlockState blockState = placement.stateTransformer.apply(block);
        int radius = this.radius.sample(random);
        float density = this.density.sample(random);
        BlockPos center = metadata.center();

        var positions = BlockPos.betweenClosedStream(
                center.getX() - radius, center.getY(), center.getZ() - radius,
                center.getX() + radius, center.getY(), center.getZ() + radius)
                .map(BlockPos::immutable)
                .filter(pos -> pos.equals(center) || random.nextFloat() <= density)
                .filter(pos -> Math.sqrt(pos.distSqr(center)) <= radius)
                .toList();

        return WorldGeneratorUtils.groupByChunks(positions).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> createPlacer(level, entry.getValue(), blockState)));
    }

    private OreIndicatorPlacer createPlacer(WorldGenLevel level, List<BlockPos> positionsWithoutY,
                                            BlockState blockState) {
        return access -> {
            var positions = positionsWithoutY.stream()
                    .map(pos -> placement.resolver.apply(level, access, pos))
                    .filter(pos -> !level.isOutsideBuildHeight(pos))
                    .toList();

            for (BlockPos pos : positions) {
                var section = Objects.requireNonNull(access.getSection(pos));

                int sectionX = SectionPos.sectionRelative(pos.getX());
                int sectionY = SectionPos.sectionRelative(pos.getY());
                int sectionZ = SectionPos.sectionRelative(pos.getZ());

                if (!section.getBlockState(sectionX, sectionY, sectionZ).isAir()) {
                    return;
                }
                if (!blockState.canSurvive(level, pos)) {
                    return;
                }
                section.setBlockState(sectionX, sectionY, sectionZ, blockState, false);
            }
        };
    }

    @Nullable
    @Override
    public BlockState block() {
        return block;
    }

    @Override
    public int getSearchRadiusModifier(int veinRadius) {
        return Math.max(0, radius.getMaxValue() - veinRadius);
    }

    @Override
    public MapCodec<? extends IndicatorGenerator> codec() {
        return CODEC;
    }

    @AllArgsConstructor
    public enum IndicatorPlacement implements StringRepresentable {

        SURFACE(
                (level, access, pos) -> pos.atY(Math.max(
                        level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, pos.getX(), pos.getZ()),
                        pos.getY())),
                Function.identity()),

        ABOVE(
                (level, access, initialPos) -> WorldGeneratorUtils.findBlockPos(
                        initialPos,
                        pos -> access.getBlockState(pos).isAir() &&
                                access.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), net.minecraft.core.Direction.UP),
                        pos -> pos.move(net.minecraft.core.Direction.UP, 1),
                        level.getMaxBuildHeight() - initialPos.getY()).orElse(initialPos),
                Function.identity()),

        BELOW(
                (level, access, initialPos) -> WorldGeneratorUtils.findBlockPos(
                        initialPos,
                        pos -> access.getBlockState(pos).isAir() &&
                                access.getBlockState(pos.above()).isFaceSturdy(level, pos.above(), net.minecraft.core.Direction.DOWN),
                        pos -> pos.move(net.minecraft.core.Direction.DOWN, 1),
                        initialPos.getY() - level.getMinBuildHeight()).orElse(initialPos),
                Function.identity());

        public static final Codec<IndicatorPlacement> CODEC = StringRepresentable.fromEnum(IndicatorPlacement::values);

        public final TriFunction<WorldGenLevel, BulkSectionAccess, BlockPos, BlockPos> resolver;
        public final Function<BlockState, BlockState> stateTransformer;

        @Override
        public @NotNull String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }

        public static @NotNull IndicatorPlacement getByName(String name) {
            return IndicatorPlacement.valueOf(name.toUpperCase(Locale.ROOT));
        }
    }
}
