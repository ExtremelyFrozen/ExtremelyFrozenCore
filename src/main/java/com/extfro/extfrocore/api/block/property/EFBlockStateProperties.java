package com.extfro.extfrocore.api.block.property;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public final class EFBlockStateProperties {

    public static final DirectionProperty UPWARDS_FACING = DirectionProperty.create(
            "upwards_facing", Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);

    private EFBlockStateProperties() {}
}
