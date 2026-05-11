package com.extfro.extfrocore.api.pattern;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public record BlockInfo(BlockState blockState) {

    public static final BlockInfo EMPTY = new BlockInfo(Blocks.AIR.defaultBlockState());

    public static BlockInfo fromBlockState(BlockState state) {
        return new BlockInfo(state);
    }

    public static BlockInfo fromBlock(Block block) {
        return fromBlockState(block.defaultBlockState());
    }
}
