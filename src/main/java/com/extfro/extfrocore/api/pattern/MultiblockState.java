package com.extfro.extfrocore.api.pattern;

import com.extfro.extfrocore.api.pattern.error.PatternError;
import com.extfro.extfrocore.api.pattern.error.PatternStringError;
import com.extfro.extfrocore.api.pattern.util.PatternMatchContext;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

public class MultiblockState {

    public static final PatternError UNLOAD_ERROR = new PatternStringError("multiblock.pattern.error.chunk");
    public static final PatternError UNINIT_ERROR = new PatternStringError("multiblock.pattern.error.init");

    @Getter
    private final @Nullable Level level;
    @Getter
    public final BlockPos controllerPos;
    @Getter
    private final PatternMatchContext matchContext = new PatternMatchContext();
    @Getter
    private final LongOpenHashSet posCache = new LongOpenHashSet();
    @Getter
    private @Nullable PatternError error = UNINIT_ERROR;
    @Getter
    private @Nullable BlockPos pos;
    @Getter
    private @Nullable BlockState blockState;
    @Getter
    private @Nullable BlockEntity blockEntity;
    @Getter
    private @Nullable TraceabilityPredicate predicate;
    @Getter
    private boolean neededFlip;

    public MultiblockState(@Nullable Level level, BlockPos controllerPos) {
        this.level = level;
        this.controllerPos = controllerPos;
    }

    public boolean update(BlockPos pos, TraceabilityPredicate predicate) {
        this.pos = pos;
        this.predicate = predicate;
        if (level == null || !level.isLoaded(pos)) {
            setError(UNLOAD_ERROR);
            return false;
        }
        this.blockState = level.getBlockState(pos);
        this.blockEntity = level.getBlockEntity(pos);
        return true;
    }

    public void clean() {
        matchContext.reset();
        posCache.clear();
        pos = null;
        blockState = null;
        blockEntity = null;
        predicate = null;
        error = null;
    }

    public boolean hasError() {
        return error != null;
    }

    public void setError(@Nullable PatternError error) {
        this.error = error;
    }

    public void setNeededFlip(boolean neededFlip) {
        this.neededFlip = neededFlip;
    }

    public void addPosCache(BlockPos pos) {
        posCache.add(pos.asLong());
    }
}
