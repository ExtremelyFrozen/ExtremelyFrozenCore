package com.extfro.extfrocore.common.worldgen.modifier;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.registry.GTRegistries;
import com.extfro.extfrocore.config.ConfigHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.placement.RepeatingPlacement;

import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.NotNull;

public class RubberTreeChancePlacement extends RepeatingPlacement {

    public static final PlacementModifierType<RubberTreeChancePlacement> RUBBER_TREE_CHANCE_PLACEMENT = GTRegistries
            .register(BuiltInRegistries.PLACEMENT_MODIFIER_TYPE, ExtForCore.id("rubber_tree_chance"),
                    () -> RubberTreeChancePlacement.CODEC);

    public static final RubberTreeChancePlacement INSTANCE = new RubberTreeChancePlacement();
    public static final MapCodec<RubberTreeChancePlacement> CODEC = MapCodec.unit(INSTANCE);

    @Override
    protected int count(RandomSource random, @NotNull BlockPos pos) {
        return random.nextFloat() < ConfigHolder.INSTANCE.worldgen.rubberTreeSpawnChance ? 1 : 0;
    }

    @Override
    public PlacementModifierType<?> type() {
        return RUBBER_TREE_CHANCE_PLACEMENT;
    }
}
