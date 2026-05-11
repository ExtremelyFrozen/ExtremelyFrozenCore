package com.extfro.extfrocore.api.data.worldgen.generator;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.data.worldgen.WorldGeneratorUtils;
import com.extfro.extfrocore.api.data.worldgen.generator.veins.NoopVeinGenerator;
import com.extfro.extfrocore.api.data.worldgen.generator.veins.StandardVeinGenerator;

import net.minecraft.resources.ResourceLocation;

import com.mojang.serialization.MapCodec;

import java.util.function.Supplier;

@SuppressWarnings("unused")
public final class VeinGenerators {

    public static final MapCodec<NoopVeinGenerator> NO_OP = register(ExtForCore.id("no_op"),
            NoopVeinGenerator.CODEC, () -> NoopVeinGenerator.INSTANCE);
    public static final MapCodec<StandardVeinGenerator> STANDARD = register(ExtForCore.id("standard"),
            StandardVeinGenerator.CODEC, StandardVeinGenerator::new);

    private VeinGenerators() {}

    public static <T extends VeinGenerator> MapCodec<T> register(ResourceLocation id, MapCodec<T> codec,
                                                                 Supplier<? extends VeinGenerator> function) {
        WorldGeneratorUtils.VEIN_GENERATORS.put(id, codec);
        WorldGeneratorUtils.VEIN_GENERATOR_FUNCTIONS.put(id, function);
        return codec;
    }

    public static void init() {}
}
