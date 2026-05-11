package com.extfro.extfrocore.api.pattern.util;

import net.minecraft.core.Direction;

public enum RelativeDirection {
    UP,
    DOWN,
    LEFT,
    RIGHT,
    FRONT,
    BACK;

    public Direction getActualDirection(Direction frontFacing, Direction upwardsFacing) {
        return switch (this) {
            case FRONT -> frontFacing;
            case BACK -> frontFacing.getOpposite();
            case UP -> upwardsFacing;
            case DOWN -> upwardsFacing.getOpposite();
            case LEFT -> horizontalSide(frontFacing, upwardsFacing, true);
            case RIGHT -> horizontalSide(frontFacing, upwardsFacing, false);
        };
    }

    private static Direction horizontalSide(Direction frontFacing, Direction upwardsFacing, boolean left) {
        int fx = frontFacing.getStepX();
        int fy = frontFacing.getStepY();
        int fz = frontFacing.getStepZ();
        int ux = upwardsFacing.getStepX();
        int uy = upwardsFacing.getStepY();
        int uz = upwardsFacing.getStepZ();
        int rx = fy * uz - fz * uy;
        int ry = fz * ux - fx * uz;
        int rz = fx * uy - fy * ux;
        Direction right = Direction.getNearest(rx, ry, rz);
        return left ? right.getOpposite() : right;
    }
}
