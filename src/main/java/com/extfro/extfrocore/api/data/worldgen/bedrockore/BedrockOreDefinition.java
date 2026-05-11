package com.extfro.extfrocore.api.data.worldgen.bedrockore;

import com.extfro.extfrocore.api.data.worldgen.BiomeWeightModifier;
import com.extfro.extfrocore.api.material.EFMaterial;
import com.extfro.extfrocore.api.registry.EFRegistries;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
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

@Accessors(fluent = true, chain = true)
public class BedrockOreDefinition {

    public static final Codec<BedrockOreDefinition> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("weight").forGetter(BedrockOreDefinition::weight),
            Codec.INT.fieldOf("size").forGetter(BedrockOreDefinition::size),
            IntProvider.POSITIVE_CODEC.fieldOf("yield").forGetter(BedrockOreDefinition::yield),
            Codec.INT.fieldOf("depletion_amount").forGetter(BedrockOreDefinition::depletionAmount),
            ExtraCodecs.intRange(0, 100).fieldOf("depletion_chance").forGetter(BedrockOreDefinition::depletionChance),
            Codec.INT.fieldOf("depleted_yield").forGetter(BedrockOreDefinition::depletedYield),
            WeightedMaterial.CODEC.listOf().fieldOf("materials").forGetter(BedrockOreDefinition::materials),
            BiomeWeightModifier.CODEC.optionalFieldOf("weight_modifier", BiomeWeightModifier.EMPTY)
                    .forGetter(BedrockOreDefinition::biomeWeightModifier),
            ResourceKey.codec(Registries.DIMENSION).listOf().fieldOf("dimension_filter")
                    .forGetter(definition -> new ArrayList<>(definition.dimensionFilter)))
            .apply(instance, BedrockOreDefinition::new));
    public static final Codec<Holder<BedrockOreDefinition>> CODEC =
            RegistryFixedCodec.create(EFRegistries.BEDROCK_ORE_REGISTRY);

    @Getter
    @Setter
    private int weight;
    @Getter
    @Setter
    private int size;
    @Getter
    @Setter
    private IntProvider yield;
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
    private List<WeightedMaterial> materials;
    @Getter
    @Setter
    private BiomeWeightModifier biomeWeightModifier;
    @Getter
    @Setter
    public Set<ResourceKey<Level>> dimensionFilter;

    public BedrockOreDefinition(int weight, int size, IntProvider yield, int depletionAmount, int depletionChance,
                                int depletedYield, List<WeightedMaterial> materials,
                                BiomeWeightModifier biomeWeightModifier, List<ResourceKey<Level>> dimensionFilter) {
        this(weight, size, yield, depletionAmount, depletionChance, depletedYield, materials, biomeWeightModifier,
                new HashSet<>(dimensionFilter));
    }

    public BedrockOreDefinition(int weight, int size, IntProvider yield, int depletionAmount, int depletionChance,
                                int depletedYield, List<WeightedMaterial> materials,
                                BiomeWeightModifier biomeWeightModifier, Set<ResourceKey<Level>> dimensionFilter) {
        this.weight = weight;
        this.size = size;
        this.yield = yield;
        this.depletionAmount = depletionAmount;
        this.depletionChance = depletionChance;
        this.depletedYield = depletedYield;
        this.materials = materials;
        this.biomeWeightModifier = biomeWeightModifier;
        this.dimensionFilter = dimensionFilter;
    }

    @Tolerate
    public void biomeWeightModifier(List<BiomeWeightModifier> modifiers) {
        this.biomeWeightModifier = BiomeWeightModifier.fromList(modifiers);
    }

    public IntList getAllChances() {
        return IntArrayList.toList(materials().stream().mapToInt(WeightedMaterial::weight));
    }

    public List<EFMaterial> getAllMaterials() {
        return materials().stream().map(WeightedMaterial::material).toList();
    }

    public boolean canGenerate() {
        return weight() > 0 || !biomeWeightModifier().isEmpty();
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
        builder.size(size);
        builder.yield(yield);
        builder.depletionAmount(depletionAmount).depletionChance(depletionChance);
        builder.depletedYield(depletedYield);
        builder.materials(materials);
        builder.dimensions(dimensionFilter);
        builder.biomes(getOriginalModifiers());
        return builder;
    }

    @Accessors(chain = true, fluent = true)
    public static class Builder {

        private final HolderGetter<Biome> biomeLookup;

        @Setter
        private int weight;
        @Setter
        private int size;
        @Setter
        private IntProvider yield;
        @Setter
        private int depletionAmount;
        @Setter
        private int depletionChance = 1;
        @Setter
        private int depletedYield;
        @Setter
        private List<WeightedMaterial> materials = new ArrayList<>();
        @Setter
        private Set<ResourceKey<Level>> dimensions = Collections.emptySet();
        private final List<BiomeWeightModifier> biomes = new LinkedList<>();

        private Builder(HolderGetter<Biome> biomeLookup) {
            this.biomeLookup = biomeLookup;
        }

        public Builder copy() {
            var copied = new Builder(biomeLookup);
            copied.weight = weight;
            copied.size = size;
            copied.yield = yield;
            copied.depletionAmount = depletionAmount;
            copied.depletionChance = depletionChance;
            copied.depletedYield = depletedYield;
            copied.materials = materials;
            copied.dimensions = dimensions;
            copied.biomes.addAll(biomes);
            return copied;
        }

        public Builder material(EFMaterial material, int amount) {
            this.materials.add(new WeightedMaterial(material, amount));
            return this;
        }

        public Builder yield(int min, int max) {
            return this.yield(UniformInt.of(min, max));
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

        public BedrockOreDefinition build() {
            return new BedrockOreDefinition(weight, size, yield, depletionAmount, depletionChance,
                    depletedYield, materials, BiomeWeightModifier.fromList(biomes), dimensions);
        }
    }
}
