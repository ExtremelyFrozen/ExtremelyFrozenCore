package com.extfro.extfrocore.api.pattern;

import com.extfro.extfrocore.api.block.MetaMachineBlock;
import com.extfro.extfrocore.api.data.RotationState;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class MultiblockShapeInfo {

    private final BlockInfo[][][] blocks;

    public MultiblockShapeInfo(BlockInfo[][][] blocks) {
        this.blocks = blocks;
    }

    public BlockInfo[][][] getBlocks() {
        return blocks;
    }

    public static ShapeInfoBuilder builder() {
        return new ShapeInfoBuilder();
    }

    public static class ShapeInfoBuilder {

        private final List<String[]> shape = new ArrayList<>();
        private final BlockInfo[] symbolMap = new BlockInfo[Character.MAX_VALUE + 1];

        public ShapeInfoBuilder aisle(String... data) {
            shape.add(data);
            return this;
        }

        public ShapeInfoBuilder where(char symbol, BlockState blockState) {
            return where(symbol, BlockInfo.fromBlockState(blockState));
        }

        public ShapeInfoBuilder where(char symbol, BlockInfo blockInfo) {
            symbolMap[symbol] = blockInfo;
            return this;
        }

        public ShapeInfoBuilder where(char symbol, Supplier<? extends Block> block) {
            return where(symbol, block.get());
        }

        public ShapeInfoBuilder where(char symbol, Block block) {
            return where(symbol, block.defaultBlockState());
        }

        public ShapeInfoBuilder where(char symbol, Supplier<? extends MetaMachineBlock> machine, Direction facing) {
            return where(symbol, machine.get(), facing);
        }

        public ShapeInfoBuilder where(char symbol, MetaMachineBlock machine, Direction facing) {
            return where(symbol, machine.getRotationState() == RotationState.NONE ?
                    machine.defaultBlockState() :
                    machine.defaultBlockState().setValue(machine.getRotationState().property, facing));
        }

        private BlockInfo[][][] bake() {
            BlockInfo[][][] result = (BlockInfo[][][]) Array.newInstance(BlockInfo.class, shape.size(), 0, 0);
            for (int z = 0; z < shape.size(); z++) {
                String[] aisle = shape.get(z);
                result[z] = (BlockInfo[][]) Array.newInstance(BlockInfo.class, aisle.length, 0);
                for (int y = 0; y < aisle.length; y++) {
                    result[z][y] = Arrays.stream(aisle[y].split(""))
                            .map(symbol -> {
                                char key = symbol.charAt(0);
                                BlockInfo blockInfo = symbolMap[key];
                                return blockInfo == null ? BlockInfo.EMPTY : blockInfo;
                            })
                            .toArray(BlockInfo[]::new);
                }
            }
            return result;
        }

        public MultiblockShapeInfo build() {
            return new MultiblockShapeInfo(bake());
        }
    }
}
