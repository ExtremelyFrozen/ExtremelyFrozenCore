package com.extfro.extfrocore.api.pattern;

public class BlockPattern {

    public static final BlockPattern EMPTY = new BlockPattern(new int[0][0]);

    private final int[][] aisleRepetitions;

    public BlockPattern(int[][] aisleRepetitions) {
        this.aisleRepetitions = aisleRepetitions;
    }

    public int[][] aisleRepetitions() {
        return aisleRepetitions;
    }

    public BlockInfo[][][] getPreview(int[] repetition) {
        return new BlockInfo[0][0][0];
    }
}
