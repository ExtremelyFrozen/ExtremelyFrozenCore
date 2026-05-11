package com.extfro.extfrocore.api.data.worldgen.bedrockore;

import com.extfro.extfrocore.api.material.EFMaterial;
import com.extfro.extfrocore.common.material.EFMaterialRegistryManager;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record WeightedMaterial(EFMaterial material, int weight) {

    public static final Codec<WeightedMaterial> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            EFMaterialRegistryManager.getInstance().codec().fieldOf("material").forGetter(WeightedMaterial::material),
            Codec.INT.fieldOf("weight").forGetter(WeightedMaterial::weight))
            .apply(instance, WeightedMaterial::new));
}
