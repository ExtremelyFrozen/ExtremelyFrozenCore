package com.extfro.extfrocore.api.data.worldgen.bedrockore;

import com.extfro.extfrocore.api.data.chemical.material.Material;
import com.extfro.extfrocore.api.registry.GTRegistries;
import com.extfro.extfrocore.utils.WeightedEntry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record WeightedMaterial(Material material, int weight) implements WeightedEntry {

    public static final Codec<WeightedMaterial> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    GTRegistries.MATERIALS.byNameCodec().fieldOf("material").forGetter(WeightedMaterial::material),
                    Codec.INT.fieldOf("weight").forGetter(WeightedMaterial::weight))
                    .apply(instance, WeightedMaterial::new));
}
