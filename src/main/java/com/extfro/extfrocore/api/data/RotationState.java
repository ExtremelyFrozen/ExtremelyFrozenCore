package com.extfro.extfrocore.api.data;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

import java.util.function.Predicate;

public enum RotationState implements Predicate<Direction> {

    ALL(dir -> true, Direction.NORTH, BlockStateProperties.FACING),
    NONE(dir -> false, Direction.NORTH, DirectionProperty.create("north_only_facing", Direction.NORTH)),
    Y_AXIS(dir -> dir.getAxis() == Direction.Axis.Y, Direction.UP,
            DirectionProperty.create("vertical_facing", Direction.UP, Direction.DOWN)),
    NON_Y_AXIS(dir -> dir.getAxis() != Direction.Axis.Y, Direction.NORTH, BlockStateProperties.HORIZONTAL_FACING);

    private final Predicate<Direction> predicate;
    public final Direction defaultDirection;
    public final DirectionProperty property;

    RotationState(Predicate<Direction> predicate, Direction defaultDirection, DirectionProperty property) {
        this.predicate = predicate;
        this.defaultDirection = defaultDirection;
        this.property = property;
    }

    @Override
    public boolean test(Direction direction) {
        return predicate.test(direction);
    }
}
