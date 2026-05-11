package com.extfro.extfrocore.api.pattern;

import com.extfro.extfrocore.api.pattern.util.RelativeDirection;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import com.google.common.base.Joiner;
import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharList;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class FactoryBlockPattern {

    private static final Joiner COMMA_JOIN = Joiner.on(",");
    private final List<String[]> depth = new ArrayList<>();
    private final List<int[]> aisleRepetitions = new ArrayList<>();
    private final Char2ObjectMap<TraceabilityPredicate> symbolMap = new Char2ObjectArrayMap<>();
    private final RelativeDirection[] structureDir;
    private int aisleHeight;
    private int rowWidth;

    private FactoryBlockPattern(RelativeDirection charDir, RelativeDirection stringDir, RelativeDirection aisleDir) {
        structureDir = new RelativeDirection[] { charDir, stringDir, aisleDir };
        int flags = 0;
        for (RelativeDirection direction : structureDir) {
            switch (direction) {
                case UP, DOWN -> flags |= 0x1;
                case LEFT, RIGHT -> flags |= 0x2;
                case FRONT, BACK -> flags |= 0x4;
            }
        }
        if (flags != 0x7) {
            throw new IllegalArgumentException("Must have 3 different axes");
        }
        symbolMap.put(' ', Predicates.any());
    }

    public static FactoryBlockPattern start() {
        return new FactoryBlockPattern(RelativeDirection.LEFT, RelativeDirection.UP, RelativeDirection.FRONT);
    }

    public static FactoryBlockPattern start(RelativeDirection charDir, RelativeDirection stringDir,
                                            RelativeDirection aisleDir) {
        return new FactoryBlockPattern(charDir, stringDir, aisleDir);
    }

    public FactoryBlockPattern aisle(String... aisle) {
        return aisleRepeatable(1, 1, aisle);
    }

    public FactoryBlockPattern aisleRepeatable(int minRepeat, int maxRepeat, String... aisle) {
        if (ArrayUtils.isEmpty(aisle) || StringUtils.isEmpty(aisle[0])) {
            throw new IllegalArgumentException("Empty pattern for aisle");
        }
        if (depth.isEmpty()) {
            aisleHeight = aisle.length;
            rowWidth = aisle[0].length();
        }
        if (aisle.length != aisleHeight) {
            throw new IllegalArgumentException("Expected aisle height " + aisleHeight + ", got " + aisle.length);
        }
        for (String row : aisle) {
            if (row.length() != rowWidth) {
                throw new IllegalArgumentException("Expected aisle row width " + rowWidth + ", got " + row.length());
            }
            for (char symbol : row.toCharArray()) {
                symbolMap.putIfAbsent(symbol, null);
            }
        }
        if (minRepeat > maxRepeat) {
            throw new IllegalArgumentException("Lower repeat bound must be smaller than upper bound");
        }
        depth.add(aisle);
        aisleRepetitions.add(new int[] { minRepeat, maxRepeat });
        return this;
    }

    public FactoryBlockPattern setRepeatable(int minRepeat, int maxRepeat) {
        if (minRepeat > maxRepeat) {
            throw new IllegalArgumentException("Lower repeat bound must be smaller than upper bound");
        }
        aisleRepetitions.set(aisleRepetitions.size() - 1, new int[] { minRepeat, maxRepeat });
        return this;
    }

    public FactoryBlockPattern setRepeatable(int repeatCount) {
        return setRepeatable(repeatCount, repeatCount);
    }

    public FactoryBlockPattern where(String symbol, TraceabilityPredicate blockMatcher) {
        return where(symbol.charAt(0), blockMatcher);
    }

    public FactoryBlockPattern where(char symbol, TraceabilityPredicate blockMatcher) {
        symbolMap.put(symbol, blockMatcher.isAny() || blockMatcher.isAir() ? blockMatcher : blockMatcher.sort());
        return this;
    }

    public FactoryBlockPattern where(char symbol, BlockState blockState) {
        return where(symbol, Predicates.states(blockState));
    }

    public FactoryBlockPattern where(char symbol, Block block) {
        return where(symbol, block.defaultBlockState());
    }

    public FactoryBlockPattern where(char symbol, Supplier<? extends Block> block) {
        return where(symbol, block.get());
    }

    public BlockPattern build() {
        checkMissingPredicates();
        int[] centerOffset = new int[5];
        int[][] repetitions = aisleRepetitions.toArray(int[][]::new);
        TraceabilityPredicate[][][] predicates = (TraceabilityPredicate[][][]) Array
                .newInstance(TraceabilityPredicate.class, depth.size(), aisleHeight, rowWidth);
        for (int i = 0, minZ = 0, maxZ = 0; i < depth.size(); minZ += repetitions[i][0], maxZ += repetitions[i][1], i++) {
            for (int j = 0; j < aisleHeight; j++) {
                for (int k = 0; k < rowWidth; k++) {
                    predicates[i][j][k] = symbolMap.get(depth.get(i)[j].charAt(k));
                    if (predicates[i][j][k].isController()) {
                        centerOffset = new int[] { k, j, i, minZ, maxZ };
                    }
                }
            }
        }
        return new BlockPattern(predicates, structureDir, repetitions, centerOffset);
    }

    private void checkMissingPredicates() {
        CharList missing = new CharArrayList();
        for (var entry : symbolMap.char2ObjectEntrySet()) {
            if (entry.getValue() == null) {
                missing.add(entry.getCharKey());
            }
        }
        if (!missing.isEmpty()) {
            throw new IllegalStateException("Predicates for character(s) " + COMMA_JOIN.join(missing) + " are missing");
        }
    }
}
