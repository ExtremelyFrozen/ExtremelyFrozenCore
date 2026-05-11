package com.extfro.extfrocore.api.pattern;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;
import java.util.function.Supplier;

public final class Predicates {

    private Predicates() {}

    public static TraceabilityPredicate any() {
        return TraceabilityPredicate.any();
    }

    public static TraceabilityPredicate air() {
        return TraceabilityPredicate.air();
    }

    public static TraceabilityPredicate controller() {
        return TraceabilityPredicate.controller();
    }

    public static TraceabilityPredicate blocks(Block... blocks) {
        return states(Arrays.stream(blocks).map(Block::defaultBlockState).toArray(BlockState[]::new));
    }

    public static TraceabilityPredicate blocks(Supplier<? extends Block> block) {
        return blocks(block.get());
    }

    public static TraceabilityPredicate states(BlockState... states) {
        return TraceabilityPredicate.states(states);
    }
}
