package com.extfro.extfrocore.api.pattern;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class TraceabilityPredicate implements Predicate<MultiblockState> {

    private final Predicate<MultiblockState> predicate;
    private final Supplier<BlockInfo[]> candidates;
    private final boolean any;
    private final boolean air;
    final boolean controller;

    public TraceabilityPredicate(Predicate<MultiblockState> predicate, Supplier<BlockInfo[]> candidates) {
        this(predicate, candidates, false, false, false);
    }

    private TraceabilityPredicate(Predicate<MultiblockState> predicate, Supplier<BlockInfo[]> candidates, boolean any,
                                  boolean air, boolean controller) {
        this.predicate = predicate;
        this.candidates = candidates;
        this.any = any;
        this.air = air;
        this.controller = controller;
    }

    public static TraceabilityPredicate any() {
        return new TraceabilityPredicate(state -> true, () -> new BlockInfo[] { BlockInfo.EMPTY }, true, false, false);
    }

    public static TraceabilityPredicate air() {
        return new TraceabilityPredicate(state -> state.getBlockState().isAir(), () -> new BlockInfo[] { BlockInfo.EMPTY },
                false, true, false);
    }

    public static TraceabilityPredicate controller() {
        return new TraceabilityPredicate(state -> state.getBlockEntity() instanceof
                com.extfro.extfrocore.api.machine.multiblock.MultiblockControllerMachine,
                () -> new BlockInfo[] { BlockInfo.EMPTY }, false, false, true);
    }

    public static TraceabilityPredicate states(net.minecraft.world.level.block.state.BlockState... states) {
        BlockInfo[] infos = Arrays.stream(states).map(BlockInfo::fromBlockState).toArray(BlockInfo[]::new);
        return new TraceabilityPredicate(state -> Arrays.stream(states).anyMatch(state.getBlockState()::equals),
                () -> infos);
    }

    public TraceabilityPredicate sort() {
        return this;
    }

    public boolean isAny() {
        return any;
    }

    public boolean isAir() {
        return air;
    }

    public boolean isController() {
        return controller;
    }

    public boolean addCache() {
        return !any && !air;
    }

    public BlockInfo[] getCandidates() {
        BlockInfo[] infos = candidates.get();
        return infos == null ? new BlockInfo[0] : infos;
    }

    public BlockInfo getPreview() {
        return Arrays.stream(getCandidates()).filter(Objects::nonNull).findFirst().orElse(BlockInfo.EMPTY);
    }

    @Override
    public boolean test(MultiblockState state) {
        return predicate.test(state);
    }
}
