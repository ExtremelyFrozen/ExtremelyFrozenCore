package com.extfro.extfrocore.api.data.worldgen.bedrockfluid;

import com.extfro.extfrocore.api.data.worldgen.BiomeWeightModifier;
import com.extfro.extfrocore.api.registry.EFRegistries;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.material.Fluid;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.Tolerate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class BedrockFluidDefinition {

    public static final MapCodec<Pair<Integer, Integer>> YIELD = Codec.mapPair(Codec.INT.fieldOf("min"),
            Codec.INT.fieldOf("max"));

    public static final Codec<BedrockFluidDefinition> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("weight").forGetter(BedrockFluidDefinition::getWeight),
            YIELD.fieldOf("yield").forGetter(definition -> Pair.of(definition.minimumYield, definition.maximumYield)),
            Codec.INT.fieldOf("depletion_amount").forGetter(BedrockFluidDefinition::getDepletionAmount),
            Codec.INT.fieldOf("depletion_chance").forGetter(BedrockFluidDefinition::getDepletionChance),
            Codec.INT.fieldOf("depleted_yield").forGetter(BedrockFluidDefinition::getDepletedYield),
            BuiltInRegistries.FLUID.byNameCodec().fieldOf("fluid").forGetter(BedrockFluidDefinition::getStoredFluid),
            BiomeWeightModifier.CODEC.optionalFieldOf("weight_modifier", BiomeWeightModifier.EMPTY)
                    .forGetter(BedrockFluidDefinition::getBiomeWeightModifier),
            ResourceKey.codec(Registries.DIMENSION).listOf().fieldOf("dimension_filter")
                    .forGetter(definition -> new ArrayList<>(definition.dimensionFilter)))
            .apply(instance, BedrockFluidDefinition::new));
    public static final Codec<Holder<BedrockFluidDefinition>> CODEC = RegistryFixedCodec.create(EFRegistries.BEDROCK_FLUID_REGISTRY);

    @Getter
    @Setter
    private int weight;
    @Getter
    @Setter
    private int minimumYield;
    @Getter
    @Setter
    private int maximumYield;
    @Getter
    @Setter
    private int depletionAmount;
    @Getter
    @Setter
    private int depletionChance;
    @Getter
    @Setter
    private int depletedYield;
    @Getter
    @Setter
    private Fluid storedFluid;
    @Getter
    @Setter
    private BiomeWeightModifier biomeWeightModifier;
    @Getter
    @Setter
    public Set<ResourceKey<Level>> dimensionFilter;

    private BedrockFluidDefinition(int weight, Pair<Integer, Integer> yield,
                                   int depletionAmount, int depletionChance, int depletedYield,
                                   Fluid storedFluid, BiomeWeightModifier modifier,
                                   List<ResourceKey<Level>> dimensionFilter) {
        this(weight, yield.getFirst(), yield.getSecond(), depletionAmount, depletionChance, depletedYield,
                storedFluid, modifier, new HashSet<>(dimensionFilter));
    }

    public BedrockFluidDefinition(int weight, int minimumYield, int maximumYield,
                                  int depletionAmount, int depletionChance, int depletedYield,
                                  Fluid storedFluid, BiomeWeightModifier modifier,
                                  Set<ResourceKey<Level>> dimensionFilter) {
        this.weight = weight;
        this.minimumYield = minimumYield;
        this.maximumYield = maximumYield;
        this.depletionAmount = depletionAmount;
        this.depletionChance = depletionChance;
        this.depletedYield = depletedYield;
        this.storedFluid = storedFluid;
        this.biomeWeightModifier = modifier;
        this.dimensionFilter = dimensionFilter;
    }

    @Tolerate
    public void setBiomeWeightModifier(List<BiomeWeightModifier> modifiers) {
        this.biomeWeightModifier = BiomeWeightModifier.fromList(modifiers);
    }

    public boolean canGenerate() {
        return getWeight() > 0 || !getBiomeWeightModifier().isEmpty();
    }

    public List<BiomeWeightModifier> getOriginalModifiers() {
        if (biomeWeightModifier instanceof BiomeWeightModifier.FromList list) {
            return list.getOriginalModifiers();
        }
        return Collections.singletonList(biomeWeightModifier);
    }

    public static Builder builder(HolderGetter<Biome> biomeLookup) {
        return new Builder(biomeLookup);
    }

    public Builder asBuilder(HolderGetter<Biome> biomeLookup) {
        Builder builder = builder(biomeLookup);
        builder.weight(weight);
        builder.minimumYield(minimumYield).maximumYield(maximumYield);
        builder.depletionAmount(depletionAmount).depletionChance(depletionChance);
        builder.depletedYield(depletedYield);
        builder.fluid(storedFluid);
        builder.dimensions(dimensionFilter);
        builder.biomes(getOriginalModifiers());
        return builder;
    }

    @Accessors(chain = true, fluent = true)
    public static class Builder {

        @Setter
        private int weight;
        @Setter
        private int minimumYield;
        @Setter
        private int maximumYield;
        @Setter
        private int depletionAmount;
        @Setter
        private int depletionChance = 1;
        @Setter
        private int depletedYield;
        @Setter
        private Fluid fluid;
        @Setter
        private Set<ResourceKey<Level>> dimensions = Collections.emptySet();
        private final List<BiomeWeightModifier> biomes = new LinkedList<>();

        private final HolderGetter<Biome> biomeLookup;

        private Builder(HolderGetter<Biome> biomeLookup) {
            this.biomeLookup = biomeLookup;
        }

        public Builder yield(int min, int max) {
            return minimumYield(min).maximumYield(max);
        }

        public Builder biomes(int weight, TagKey<Biome> biomes) {
            this.biomes.add(new BiomeWeightModifier(biomeLookup.getOrThrow(biomes), weight));
            return this;
        }

        @SafeVarargs
        public final Builder biomes(int weight, ResourceKey<Biome>... biomes) {
            this.biomes.add(new BiomeWeightModifier(HolderSet.direct(biomeLookup::getOrThrow, biomes), weight));
            return this;
        }

        public Builder biomes(int weight, HolderSet<Biome> biomes) {
            this.biomes.add(new BiomeWeightModifier(biomes, weight));
            return this;
        }

        public Builder biomes(List<BiomeWeightModifier> modifiers) {
            this.biomes.addAll(modifiers);
            return this;
        }

        public BedrockFluidDefinition build() {
            return new BedrockFluidDefinition(weight, minimumYield, maximumYield, depletionAmount,
                    depletionChance, depletedYield, fluid, BiomeWeightModifier.fromList(biomes), dimensions);
        }
    }
}
