package com.extfro.extfrocore.api.data.worldgen;

import com.extfro.extfrocore.api.data.worldgen.generator.IndicatorGenerator;
import com.extfro.extfrocore.api.data.worldgen.generator.VeinGenerator;
import com.extfro.extfrocore.api.data.worldgen.generator.indicators.SurfaceIndicatorGenerator;
import com.extfro.extfrocore.api.data.worldgen.generator.veins.NoopVeinGenerator;
import com.extfro.extfrocore.api.data.worldgen.generator.veins.StandardVeinGenerator;
import com.extfro.extfrocore.api.registry.EFRegistries;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.Tolerate;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("UnusedReturnValue")
@Accessors(chain = true, fluent = true)
public class OreDefinition {

    public static final Codec<OreDefinition> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            IntProvider.NON_NEGATIVE_CODEC.fieldOf("cluster_size").forGetter(OreDefinition::clusterSize),
            Codec.floatRange(0.0F, 1.0F).fieldOf("density").forGetter(OreDefinition::density),
            Codec.INT.fieldOf("weight").forGetter(OreDefinition::weight),
            IWorldGenLayer.CODEC.fieldOf("layer").forGetter(OreDefinition::layer),
            ResourceKey.codec(Registries.DIMENSION).listOf().fieldOf("dimension_filter")
                    .forGetter(definition -> new ArrayList<>(definition.dimensionFilter)),
            HeightRangePlacement.CODEC.fieldOf("height_range").forGetter(OreDefinition::heightRange),
            Codec.floatRange(0.0F, 1.0F).fieldOf("discard_chance_on_air_exposure")
                    .forGetter(OreDefinition::discardChanceOnAirExposure),
            RegistryCodecs.homogeneousList(Registries.BIOME).lenientOptionalFieldOf("biomes", HolderSet.empty())
                    .forGetter(OreDefinition::biomes),
            BiomeWeightModifier.CODEC.optionalFieldOf("weight_modifier", BiomeWeightModifier.EMPTY)
                    .forGetter(definition -> definition.biomeWeightModifier),
            VeinGenerator.DIRECT_CODEC.fieldOf("generator").forGetter(definition -> definition.veinGenerator),
            Codec.list(IndicatorGenerator.DIRECT_CODEC).fieldOf("indicators")
                    .forGetter(definition -> definition.indicatorGenerators))
            .apply(instance, OreDefinition::new));

    public static final Codec<Holder<OreDefinition>> CODEC = RegistryFixedCodec.create(EFRegistries.ORE_VEIN_REGISTRY);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<OreDefinition>> STREAM_CODEC = ByteBufCodecs.holderRegistry(EFRegistries.ORE_VEIN_REGISTRY);

    private final InferredProperties inferredProperties = new InferredProperties();

    @Getter
    private IntProvider clusterSize;
    @Getter
    private float density;
    @Getter
    private int weight;
    @Getter
    private IWorldGenLayer layer;
    @Getter
    @Setter
    private Set<ResourceKey<Level>> dimensionFilter;
    @Getter
    @Setter
    private HeightRangePlacement heightRange;
    @Getter
    @Setter
    private float discardChanceOnAirExposure;
    @Getter
    private HolderSet<Biome> biomes;
    @Getter
    @Setter
    private BiomeWeightModifier biomeWeightModifier;
    @Getter
    @Setter
    private VeinGenerator veinGenerator;
    @Getter
    @Setter
    private List<IndicatorGenerator> indicatorGenerators;

    @ApiStatus.Internal
    @Nullable
    @Setter
    private HolderGetter<Biome> biomeLookup;

    public OreDefinition(OreDefinition other) {
        this(other.clusterSize, other.density, other.weight, other.layer,
                Set.copyOf(other.dimensionFilter), other.heightRange, other.discardChanceOnAirExposure,
                other.biomes, other.biomeWeightModifier, other.veinGenerator, List.copyOf(other.indicatorGenerators),
                other.biomeLookup);
    }

    public OreDefinition(IntProvider clusterSize, float density, int weight, IWorldGenLayer layer,
                         List<ResourceKey<Level>> dimensionFilter, HeightRangePlacement heightRange,
                         float discardChanceOnAirExposure, HolderSet<Biome> biomes,
                         BiomeWeightModifier biomeWeightModifier, @Nullable VeinGenerator veinGenerator,
                         @Nullable List<IndicatorGenerator> indicatorGenerators) {
        this(clusterSize, density, weight, layer, new HashSet<>(dimensionFilter), heightRange,
                discardChanceOnAirExposure, biomes, biomeWeightModifier, veinGenerator, indicatorGenerators, null);
    }

    public OreDefinition(IntProvider clusterSize, float density, int weight, IWorldGenLayer layer,
                         Set<ResourceKey<Level>> dimensionFilter, HeightRangePlacement heightRange,
                         float discardChanceOnAirExposure, HolderSet<Biome> biomes,
                         BiomeWeightModifier biomeWeightModifier, @Nullable VeinGenerator veinGenerator,
                         @Nullable List<IndicatorGenerator> indicatorGenerators,
                         @Nullable HolderGetter<Biome> biomeLookup) {
        this.clusterSize = clusterSize;
        this.density = density;
        this.weight = weight;
        this.layer = layer;
        this.dimensionFilter = dimensionFilter;
        this.heightRange = heightRange;
        this.discardChanceOnAirExposure = discardChanceOnAirExposure;
        this.biomes = biomes;
        this.biomeWeightModifier = biomeWeightModifier;
        this.veinGenerator = Objects.requireNonNullElse(veinGenerator, NoopVeinGenerator.INSTANCE);
        this.indicatorGenerators = Objects.requireNonNullElseGet(indicatorGenerators, ArrayList::new);
        this.biomeLookup = biomeLookup;
    }

    public boolean isForBiome(Holder<Biome> biome) {
        return biomes == null || biomes.size() == 0 || biomes.contains(biome);
    }

    public int weightForBiome(Holder<Biome> biome) {
        return weight + biomeWeightModifier.applyAsInt(biome);
    }

    public OreDefinition clusterSize(IntProvider clusterSize) {
        this.clusterSize = clusterSize;
        return this;
    }

    public OreDefinition clusterSize(int clusterSize) {
        this.clusterSize = ConstantInt.of(clusterSize);
        return this;
    }

    public OreDefinition density(float density) {
        this.density = density;
        return this;
    }

    public OreDefinition weight(int weight) {
        this.weight = weight;
        return this;
    }

    public OreDefinition layer(IWorldGenLayer layer) {
        this.layer = layer;
        if (dimensionFilter == null || dimensionFilter.isEmpty()) {
            dimensions(layer.getLevels());
        }
        return this;
    }

    public OreDefinition dimensions(Set<ResourceKey<Level>> dimensions) {
        this.dimensionFilter = dimensions;
        return this;
    }

    public OreDefinition biomes(TagKey<Biome> biomes) {
        if (biomeLookup == null) {
            throw new IllegalStateException("Cannot set biome tag after biome lookup is unavailable");
        }
        this.biomes = biomeLookup.getOrThrow(biomes);
        return this;
    }

    public OreDefinition biomes(HolderSet<Biome> biomes) {
        this.biomes = Objects.requireNonNullElseGet(biomes, HolderSet::empty);
        return this;
    }

    public OreDefinition heightRangeUniform(int min, int max) {
        heightRange(HeightRangePlacement.uniform(VerticalAnchor.absolute(min), VerticalAnchor.absolute(max)));
        inferredProperties.heightRange = IntIntPair.of(min, max);
        return this;
    }

    public OreDefinition heightRangeTriangle(int min, int max) {
        heightRange(HeightRangePlacement.triangle(VerticalAnchor.absolute(min), VerticalAnchor.absolute(max)));
        inferredProperties.heightRange = IntIntPair.of(min, max);
        return this;
    }

    public OreDefinition standardVeinGenerator(Consumer<StandardVeinGenerator> config) {
        var generator = new StandardVeinGenerator();
        config.accept(generator);
        veinGenerator = generator;
        return this;
    }

    public OreDefinition surfaceIndicatorGenerator(Consumer<SurfaceIndicatorGenerator> config) {
        config.accept(getOrCreateIndicatorGenerator(SurfaceIndicatorGenerator.class, SurfaceIndicatorGenerator::new));
        return this;
    }

    @Tolerate
    @Nullable
    public VeinGenerator veinGenerator(net.minecraft.resources.ResourceLocation id) {
        if (veinGenerator == null && WorldGeneratorUtils.VEIN_GENERATOR_FUNCTIONS.containsKey(id)) {
            veinGenerator = WorldGeneratorUtils.VEIN_GENERATOR_FUNCTIONS.get(id).get();
        }
        return veinGenerator;
    }

    private <T extends IndicatorGenerator> T getOrCreateIndicatorGenerator(Class<T> indicatorClass,
                                                                           Supplier<T> constructor) {
        var existing = indicatorGenerators.stream()
                .filter(indicatorClass::isInstance)
                .map(indicatorClass::cast)
                .findFirst()
                .orElse(null);
        if (existing != null) {
            return existing;
        }
        var generator = constructor.get();
        indicatorGenerators.add(generator);
        return generator;
    }

    public boolean canGenerate() {
        return !(veinGenerator() instanceof NoopVeinGenerator) &&
                (weight() > 0 || !biomeWeightModifier().isEmpty());
    }

    private static class InferredProperties {

        public IntIntPair heightRange = null;
    }
}
