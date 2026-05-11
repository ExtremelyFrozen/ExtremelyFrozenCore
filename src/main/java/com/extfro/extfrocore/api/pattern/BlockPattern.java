package com.extfro.extfrocore.api.pattern;

import com.extfro.extfrocore.api.machine.feature.multiblock.IMultiPart;
import com.extfro.extfrocore.api.machine.multiblock.MultiblockControllerMachine;
import com.extfro.extfrocore.api.pattern.error.PatternError;
import com.extfro.extfrocore.api.pattern.error.PatternStringError;
import com.extfro.extfrocore.api.pattern.util.RelativeDirection;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;

import java.lang.reflect.Array;
import java.util.Set;

public class BlockPattern {

    public static final BlockPattern EMPTY = new BlockPattern(new TraceabilityPredicate[0][0][0],
            new RelativeDirection[] { RelativeDirection.LEFT, RelativeDirection.UP, RelativeDirection.FRONT },
            new int[0][0], new int[5]);

    public final int[][] aisleRepetitions;
    public final RelativeDirection[] structureDir;
    protected final TraceabilityPredicate[][][] blockMatches;
    protected final int fingerLength;
    protected final int thumbLength;
    protected final int palmLength;
    protected final int[] centerOffset;
    @Getter
    protected int[] formedRepetitionCount;

    public BlockPattern(int[][] aisleRepetitions) {
        this(new TraceabilityPredicate[0][0][0],
                new RelativeDirection[] { RelativeDirection.LEFT, RelativeDirection.UP, RelativeDirection.FRONT },
                aisleRepetitions, new int[5]);
    }

    public BlockPattern(TraceabilityPredicate[][][] predicatesIn, RelativeDirection[] structureDir,
                        int[][] aisleRepetitions, int[] centerOffset) {
        this.blockMatches = predicatesIn;
        this.fingerLength = predicatesIn.length;
        this.structureDir = structureDir;
        this.aisleRepetitions = aisleRepetitions;
        this.formedRepetitionCount = new int[aisleRepetitions.length];
        this.thumbLength = fingerLength > 0 ? predicatesIn[0].length : 0;
        this.palmLength = thumbLength > 0 ? predicatesIn[0][0].length : 0;
        this.centerOffset = centerOffset;
    }

    public int[][] aisleRepetitions() {
        return aisleRepetitions;
    }

    public boolean checkPatternAt(MultiblockState worldState, boolean savePredicate) {
        var machine = com.extfro.extfrocore.api.machine.MetaMachine.getMachine(worldState.getLevel(),
                worldState.controllerPos);
        if (!(machine instanceof MultiblockControllerMachine controller)) {
            worldState.setError(new PatternStringError("no controller found"));
            return false;
        }
        Direction frontFacing = controller.getFrontFacing();
        Direction[] facings = controller.hasFrontFacing() ? new Direction[] { frontFacing } :
                new Direction[] { Direction.SOUTH, Direction.NORTH, Direction.EAST, Direction.WEST };
        Direction upwardsFacing = controller.getUpwardsFacing();
        for (Direction facing : facings) {
            if (checkPatternAt(worldState, controller.getBlockPos(), facing, upwardsFacing, false, savePredicate)) {
                return true;
            }
            if (controller.allowFlip() && checkPatternAt(worldState, controller.getBlockPos(), facing, upwardsFacing,
                    true, savePredicate)) {
                return true;
            }
        }
        return false;
    }

    public boolean checkPatternAt(MultiblockState worldState, BlockPos centerPos, Direction frontFacing,
                                  Direction upwardsFacing, boolean isFlipped, boolean savePredicate) {
        boolean foundFirstAisle = false;
        int minZ = -centerOffset[4];
        worldState.clean();
        Set<IMultiPart> parts = worldState.getMatchContext().getOrCreate("parts", ObjectOpenHashSet::new);
        for (int c = 0, z = minZ++, r; c < fingerLength; c++) {
            int validRepetitions = 0;
            loop:
            for (r = 0; foundFirstAisle ? r < aisleRepetitions[c][1] : z <= -centerOffset[3]; r++) {
                for (int b = 0, y = -centerOffset[1]; b < thumbLength; b++, y++) {
                    for (int a = 0, x = -centerOffset[0]; a < palmLength; a++, x++) {
                        TraceabilityPredicate predicate = blockMatches[c][b][a];
                        BlockPos pos = setActualRelativeOffset(x, y, z, frontFacing, upwardsFacing, isFlipped)
                                .offset(centerPos);
                        if (!worldState.update(pos, predicate)) {
                            return false;
                        }
                        if (predicate.addCache()) {
                            worldState.addPosCache(pos);
                        }
                        if (worldState.getBlockEntity() instanceof IMultiPart part && !predicate.isAny()) {
                            if (part.isFormed() && !part.canShared() && !part.hasController(worldState.controllerPos)) {
                                worldState.setError(new PatternStringError("multiblock.pattern.error.share"));
                                return false;
                            }
                            parts.add(part);
                        }
                        if (!predicate.test(worldState)) {
                            if (foundFirstAisle) {
                                if (r < aisleRepetitions[c][0]) {
                                    r = c = 0;
                                    z = minZ++;
                                    worldState.getMatchContext().reset();
                                    parts = worldState.getMatchContext().getOrCreate("parts", ObjectOpenHashSet::new);
                                    foundFirstAisle = false;
                                }
                            } else {
                                z++;
                            }
                            continue loop;
                        }
                    }
                }
                foundFirstAisle = true;
                z++;
                validRepetitions++;
            }
            if (r < aisleRepetitions[c][0] || worldState.hasError() || !foundFirstAisle) {
                if (!worldState.hasError()) {
                    worldState.setError(new PatternError());
                }
                return false;
            }
            formedRepetitionCount[c] = validRepetitions;
        }
        worldState.setError(null);
        worldState.setNeededFlip(isFlipped);
        return true;
    }

    public BlockInfo[][][] getPreview(int[] repetition) {
        int zSize = 0;
        for (int i = 0; i < fingerLength; i++) {
            zSize += repetition.length > i ? repetition[i] : aisleRepetitions[i][0];
        }
        BlockInfo[][][] result = (BlockInfo[][][]) Array.newInstance(BlockInfo.class, zSize, thumbLength, palmLength);
        int z = 0;
        for (int aisle = 0; aisle < fingerLength; aisle++) {
            int repeat = repetition.length > aisle ? repetition[aisle] : aisleRepetitions[aisle][0];
            for (int r = 0; r < repeat; r++, z++) {
                for (int y = 0; y < thumbLength; y++) {
                    for (int x = 0; x < palmLength; x++) {
                        result[z][y][x] = blockMatches[aisle][y][x].getPreview();
                    }
                }
            }
        }
        return result;
    }

    private BlockPos setActualRelativeOffset(int x, int y, int z, Direction facing, Direction upwardsFacing,
                                             boolean isFlipped) {
        int[] source = new int[] { isFlipped ? -x : x, y, z };
        int dx = 0;
        int dy = 0;
        int dz = 0;
        for (int i = 0; i < 3; i++) {
            Direction direction = structureDir[i].getActualDirection(facing, upwardsFacing);
            dx += direction.getStepX() * source[i];
            dy += direction.getStepY() * source[i];
            dz += direction.getStepZ() * source[i];
        }
        return new BlockPos(dx, dy, dz);
    }
}
