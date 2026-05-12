package com.extfro.extfrocore.api.data.worldgen.generator;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.addon.AddonFinder;
import com.extfro.extfrocore.api.addon.IGTAddon;
import com.extfro.extfrocore.api.data.worldgen.WorldGeneratorUtils;
import com.extfro.extfrocore.api.data.worldgen.generator.veins.*;

import net.minecraft.resources.ResourceLocation;

import com.mojang.serialization.MapCodec;

import java.util.function.Supplier;

@SuppressWarnings("unused")
public class VeinGenerators {

    public static final MapCodec<NoopVeinGenerator> NO_OP = register(ExtForCore.id("no_op"), NoopVeinGenerator.CODEC,
            () -> NoopVeinGenerator.INSTANCE);

    public static final MapCodec<StandardVeinGenerator> STANDARD = register(ExtForCore.id("standard"),
            StandardVeinGenerator.CODEC, StandardVeinGenerator::new);
    public static final MapCodec<LayeredVeinGenerator> LAYER = register(ExtForCore.id("layer"), LayeredVeinGenerator.CODEC,
            LayeredVeinGenerator::new);
    public static final MapCodec<GeodeVeinGenerator> GEODE = register(ExtForCore.id("geode"), GeodeVeinGenerator.CODEC,
            GeodeVeinGenerator::new);
    public static final MapCodec<DikeVeinGenerator> DIKE = register(ExtForCore.id("dike"), DikeVeinGenerator.CODEC,
            DikeVeinGenerator::new);
    public static final MapCodec<VeinedVeinGenerator> VEINED = register(ExtForCore.id("veined"), VeinedVeinGenerator.CODEC,
            VeinedVeinGenerator::new);
    public static final MapCodec<ClassicVeinGenerator> CLASSIC = register(ExtForCore.id("classic"),
            ClassicVeinGenerator.CODEC, ClassicVeinGenerator::new);
    public static final MapCodec<CuboidVeinGenerator> CUBOID = register(ExtForCore.id("cuboid"), CuboidVeinGenerator.CODEC,
            CuboidVeinGenerator::new);

    public static <
            T extends VeinGenerator> MapCodec<T> register(ResourceLocation id, MapCodec<T> codec,
                                                          Supplier<? extends VeinGenerator> function) {
        WorldGeneratorUtils.VEIN_GENERATORS.put(id, codec);
        WorldGeneratorUtils.VEIN_GENERATOR_FUNCTIONS.put(id, function);
        return codec;
    }

    public static void registerAddonGenerators() {
        AddonFinder.getAddonList().forEach(IGTAddon::registerVeinGenerators);
    }
}
