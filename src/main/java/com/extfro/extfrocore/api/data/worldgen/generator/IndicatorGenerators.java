package com.extfro.extfrocore.api.data.worldgen.generator;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.data.worldgen.WorldGeneratorUtils;
import com.extfro.extfrocore.api.data.worldgen.generator.indicators.NoopIndicatorGenerator;
import com.extfro.extfrocore.api.data.worldgen.generator.indicators.SurfaceIndicatorGenerator;

import net.minecraft.resources.ResourceLocation;

import com.mojang.serialization.MapCodec;

import java.util.function.Supplier;

public final class IndicatorGenerators {

    public static final MapCodec<NoopIndicatorGenerator> NO_OP = register(ExtForCore.id("no_op"),
            NoopIndicatorGenerator.CODEC, () -> NoopIndicatorGenerator.INSTANCE);
    public static final MapCodec<SurfaceIndicatorGenerator> SURFACE = register(ExtForCore.id("surface"),
            SurfaceIndicatorGenerator.CODEC, SurfaceIndicatorGenerator::new);

    private IndicatorGenerators() {}

    public static <T extends IndicatorGenerator> MapCodec<T> register(ResourceLocation id, MapCodec<T> codec,
                                                                      Supplier<T> function) {
        WorldGeneratorUtils.INDICATOR_GENERATORS.put(id, codec);
        WorldGeneratorUtils.INDICATOR_GENERATOR_FUNCTIONS.put(id, function);
        return codec;
    }

    public static void init() {}
}
