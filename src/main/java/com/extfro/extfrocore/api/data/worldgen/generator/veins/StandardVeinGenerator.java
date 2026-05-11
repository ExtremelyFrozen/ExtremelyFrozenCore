package com.extfro.extfrocore.api.data.worldgen.generator.veins;

import com.extfro.extfrocore.api.data.worldgen.OreDefinition;
import com.extfro.extfrocore.api.data.worldgen.generator.VeinGenerator;
import com.extfro.extfrocore.api.data.worldgen.ores.OreBlockPlacer;
import com.extfro.extfrocore.api.data.worldgen.ores.OreVeinUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
public class StandardVeinGenerator extends VeinGenerator {

    public static final MapCodec<StandardVeinGenerator> CODEC_SEPARATE =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(generator -> generator.block.get()),
                    BuiltInRegistries.BLOCK.byNameCodec().fieldOf("deep_block")
                            .forGetter(generator -> generator.deepBlock.get()),
                    BuiltInRegistries.BLOCK.byNameCodec().fieldOf("nether_block")
                            .forGetter(generator -> generator.netherBlock.get()))
                    .apply(instance, StandardVeinGenerator::new));
    public static final MapCodec<StandardVeinGenerator> CODEC_LIST =
            OreConfiguration.TargetBlockState.CODEC.listOf()
                    .fieldOf("targets")
                    .xmap(StandardVeinGenerator::new, StandardVeinGenerator::getBlocks);
    public static final MapCodec<StandardVeinGenerator> CODEC =
            Codec.mapEither(CODEC_SEPARATE, CODEC_LIST).xmap(either -> either.map(a -> a, b -> b), generator ->
                    generator.blocks != null ? com.mojang.datafixers.util.Either.right(generator) :
                            com.mojang.datafixers.util.Either.left(generator));

    public NonNullSupplier<? extends Block> block;
    public NonNullSupplier<? extends Block> deepBlock;
    public NonNullSupplier<? extends Block> netherBlock;

    @Getter
    public List<OreConfiguration.TargetBlockState> blocks;
    private List<VeinEntry> defaultEntries = null;

    public StandardVeinGenerator(Block block, Block deepBlock, Block netherBlock) {
        this.block = NonNullSupplier.of(() -> block);
        this.deepBlock = NonNullSupplier.of(() -> deepBlock);
        this.netherBlock = NonNullSupplier.of(() -> netherBlock);
    }

    public StandardVeinGenerator(List<OreConfiguration.TargetBlockState> blocks) {
        this.blocks = blocks;
    }

    public StandardVeinGenerator withBlock(NonNullSupplier<? extends Block> block) {
        this.block = block;
        this.deepBlock = block;
        return this;
    }

    public StandardVeinGenerator withNetherBlock(NonNullSupplier<? extends Block> block) {
        this.netherBlock = block;
        return this;
    }

    private List<VeinEntry> getDefaultEntries() {
        if (defaultEntries == null) {
            defaultEntries = List.of(
                    VeinEntry.ofBlock(block.get().defaultBlockState(), 1),
                    VeinEntry.ofBlock(deepBlock.get().defaultBlockState(), 1),
                    VeinEntry.ofBlock(netherBlock.get().defaultBlockState(), 1));
        }
        return defaultEntries;
    }

    @Override
    public List<VeinEntry> getAllEntries() {
        if (blocks != null) {
            return VeinGenerator.mapTarget(blocks, 1);
        }
        return getDefaultEntries();
    }

    @Override
    public VeinGenerator build() {
        if (blocks != null) {
            return this;
        }
        List<OreConfiguration.TargetBlockState> targetStates = new ArrayList<>();
        if (block != null) {
            targetStates.add(OreConfiguration.target(new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES),
                    block.get().defaultBlockState()));
        }
        if (deepBlock != null) {
            targetStates.add(OreConfiguration.target(new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES),
                    deepBlock.get().defaultBlockState()));
        }
        if (netherBlock != null) {
            targetStates.add(OreConfiguration.target(new TagMatchTest(BlockTags.NETHER_CARVER_REPLACEABLES),
                    netherBlock.get().defaultBlockState()));
        }
        blocks = targetStates;
        return this;
    }

    @Override
    public VeinGenerator copy() {
        return new StandardVeinGenerator(new ArrayList<>(blocks));
    }

    @Override
    public MapCodec<? extends VeinGenerator> codec() {
        return CODEC;
    }

    @Override
    public Map<BlockPos, OreBlockPlacer> generate(WorldGenLevel level, RandomSource random, OreDefinition definition,
                                                  BlockPos origin) {
        Map<BlockPos, OreBlockPlacer> generatedBlocks = new Object2ObjectOpenHashMap<>();

        int size = definition.clusterSize().sample(random);
        float angle = random.nextFloat() * (float) Math.PI;
        float halfLength = size / 8.0F;
        int padding = Mth.ceil((size / 16.0F * 2.0F + 1.0F) / 2.0F);
        double minX = origin.getX() + Math.sin(angle) * halfLength;
        double maxX = origin.getX() - Math.sin(angle) * halfLength;
        double minZ = origin.getZ() + Math.cos(angle) * halfLength;
        double maxZ = origin.getZ() - Math.cos(angle) * halfLength;
        double minY = origin.getY() + random.nextInt(3) - 2;
        double maxY = origin.getY() + random.nextInt(3) - 2;
        int x = origin.getX() - Mth.ceil(halfLength) - padding;
        int y = origin.getY() - 2 - padding;
        int z = origin.getZ() - Mth.ceil(halfLength) - padding;
        int width = 2 * (Mth.ceil(halfLength) + padding);
        int height = 2 * (2 + padding);

        for (int heightmapX = x; heightmapX <= x + width; ++heightmapX) {
            for (int heightmapZ = z; heightmapZ <= z + width; ++heightmapZ) {
                doPlaceNormal(generatedBlocks, random, definition, origin, blocks, minX, maxX, minZ, maxZ, minY,
                        maxY, x, y, z, width, height);
                if (!generatedBlocks.isEmpty()) {
                    return generatedBlocks;
                }
            }
        }
        return generatedBlocks;
    }

    protected void doPlaceNormal(Map<BlockPos, OreBlockPlacer> generatedBlocks, RandomSource random,
                                 OreDefinition definition, BlockPos origin,
                                 List<OreConfiguration.TargetBlockState> targets,
                                 double minX, double maxX, double minZ, double maxZ, double minY, double maxY,
                                 int x, int y, int z, int width, int height) {
        MutableInt placedAmount = new MutableInt(1);
        BitSet placedBlocks = new BitSet(width * height * width);
        BlockPos.MutableBlockPos posCursor = new BlockPos.MutableBlockPos();
        int size = definition.clusterSize().sample(random);
        float density = definition.density();
        double[] shape = new double[size * 4];

        for (int centerOffset = 0; centerOffset < size; ++centerOffset) {
            float centerOffsetFraction = (float) centerOffset / (float) size;
            double shapeX = Mth.lerp(centerOffsetFraction, minX, maxX);
            double shapeY = Mth.lerp(centerOffsetFraction, minY, maxY);
            double shapeZ = Mth.lerp(centerOffsetFraction, minZ, maxZ);
            double randomOffsetModifier = random.nextDouble() * (double) size / 16.0D;
            double randomShapeOffset = ((double) (Mth.sin((float) Math.PI * centerOffsetFraction) + 1.0F) *
                    randomOffsetModifier + 1.0D) / 2.0D;

            int shapeIdxOffset = centerOffset * 4;
            shape[shapeIdxOffset] = shapeX;
            shape[shapeIdxOffset + 1] = shapeY;
            shape[shapeIdxOffset + 2] = shapeZ;
            shape[shapeIdxOffset + 3] = randomShapeOffset;
        }

        pruneOverlappingShapes(size, shape);

        for (int centerOffset = 0; centerOffset < size; ++centerOffset) {
            generateShape(generatedBlocks, random, definition, targets, x, y, z, width, height,
                    shape, centerOffset * 4, placedBlocks, posCursor, density, placedAmount);
        }
    }

    private static void pruneOverlappingShapes(int size, double[] shape) {
        for (int centerOffset = 0; centerOffset < size - 1; ++centerOffset) {
            int firstOffset = centerOffset * 4;
            if (shape[firstOffset + 3] <= 0.0D) {
                continue;
            }
            for (int i = centerOffset + 1; i < size; ++i) {
                int secondOffset = i * 4;
                if (shape[secondOffset + 3] <= 0.0D) {
                    continue;
                }
                double x = shape[firstOffset] - shape[secondOffset];
                double y = shape[firstOffset + 1] - shape[secondOffset + 1];
                double z = shape[firstOffset + 2] - shape[secondOffset + 2];
                double randomShapeOffset = shape[firstOffset + 3] - shape[secondOffset + 3];
                if (randomShapeOffset * randomShapeOffset <= x * x + y * y + z * z) {
                    continue;
                }
                if (randomShapeOffset > 0.0D) {
                    shape[secondOffset + 3] = -1.0D;
                } else {
                    shape[firstOffset + 3] = -1.0D;
                }
            }
        }
    }

    private static void generateShape(Map<BlockPos, OreBlockPlacer> generatedBlocks, RandomSource random,
                                      OreDefinition definition, List<OreConfiguration.TargetBlockState> targets,
                                      int x, int y, int z, int width, int height, double[] shape,
                                      int shapeIdxOffset, BitSet placedBlocks, BlockPos.MutableBlockPos posCursor,
                                      float density, MutableInt placedAmount) {
        double randomShapeOffset = shape[shapeIdxOffset + 3];
        if (randomShapeOffset < 0.0D) {
            return;
        }

        double shapeX = shape[shapeIdxOffset];
        double shapeY = shape[shapeIdxOffset + 1];
        double shapeZ = shape[shapeIdxOffset + 2];

        int minX = Math.max(Mth.floor(shapeX - randomShapeOffset), x);
        int minY = Math.max(Mth.floor(shapeY - randomShapeOffset), y);
        int minZ = Math.max(Mth.floor(shapeZ - randomShapeOffset), z);
        int maxX = Math.max(Mth.floor(shapeX + randomShapeOffset), minX);
        int maxY = Math.max(Mth.floor(shapeY + randomShapeOffset), minY);
        int maxZ = Math.max(Mth.floor(shapeZ + randomShapeOffset), minZ);

        for (int posX = minX; posX <= maxX; ++posX) {
            double radX = ((double) posX + 0.5D - shapeX) / randomShapeOffset;
            if (radX * radX >= 1.0D) {
                continue;
            }
            posCursor.setX(posX);

            for (int posY = minY; posY <= maxY; ++posY) {
                double radY = ((double) posY + 0.5D - shapeY) / randomShapeOffset;
                if (radX * radX + radY * radY >= 1.0D) {
                    continue;
                }
                posCursor.setY(posY);

                for (int posZ = minZ; posZ <= maxZ; ++posZ) {
                    double radZ = ((double) posZ + 0.5D - shapeZ) / randomShapeOffset;
                    if (radX * radX + radY * radY + radZ * radZ >= 1.0D) {
                        continue;
                    }
                    posCursor.setZ(posZ);

                    int placedIndex = posX - x + (posY - y) * width + (posZ - z) * width * height;
                    if (placedBlocks.get(placedIndex)) {
                        continue;
                    }

                    placedBlocks.set(placedIndex);
                    BlockPos pos = posCursor.immutable();
                    long randomSeed = random.nextLong();
                    generatedBlocks.put(pos, (access, section) -> placeBlock(access, randomSeed, definition, targets,
                            pos, density, placedAmount));
                }
            }
        }
    }

    private static void placeBlock(BulkSectionAccess access, long randomSeed, OreDefinition definition,
                                   List<OreConfiguration.TargetBlockState> targets, BlockPos pos, float density,
                                   MutableInt placedAmount) {
        RandomSource random = new XoroshiroRandomSource(randomSeed);
        BlockPos.MutableBlockPos posCursor = pos.mutable();
        LevelChunkSection section = access.getSection(posCursor);
        if (section == null || !(random.nextFloat() <= density)) {
            return;
        }

        int sectionX = SectionPos.sectionRelative(pos.getX());
        int sectionY = SectionPos.sectionRelative(pos.getY());
        int sectionZ = SectionPos.sectionRelative(pos.getZ());
        BlockState state = section.getBlockState(sectionX, sectionY, sectionZ);

        for (OreConfiguration.TargetBlockState targetState : targets) {
            if (OreVeinUtil.canPlaceOre(state, access::getBlockState, random, definition, targetState, posCursor)) {
                section.setBlockState(sectionX, sectionY, sectionZ, targetState.state, false);
                placedAmount.increment();
                break;
            }
        }
    }
}
