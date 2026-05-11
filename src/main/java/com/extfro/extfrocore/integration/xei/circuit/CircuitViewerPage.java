package com.extfro.extfrocore.integration.xei.circuit;

import java.util.List;
import java.util.stream.IntStream;

public record CircuitViewerPage(CircuitDisplay display,
                                int slotSize,
                                int slotSpacing,
                                int padding) {

    public static final int DEFAULT_COLUMNS = 8;
    public static final int DEFAULT_SLOT_SIZE = 18;
    public static final int DEFAULT_PADDING = 3;

    public static CircuitViewerPage of(CircuitDisplay display) {
        return new CircuitViewerPage(display, DEFAULT_SLOT_SIZE, DEFAULT_SLOT_SIZE, DEFAULT_PADDING);
    }

    public int width() {
        return padding * 2 + display.columns() * slotSpacing;
    }

    public int height() {
        return padding * 2 + display.rows() * slotSpacing;
    }

    public List<Slot> slots() {
        return IntStream.range(0, display.stacks().size())
                .mapToObj(index -> {
                    CircuitStackEntry entry = display.stacks().get(index);
                    int x = padding + index % display.columns() * slotSpacing;
                    int y = padding + index / display.columns() * slotSpacing;
                    return new Slot(entry, x, y, slotSize);
                })
                .toList();
    }

    public record Slot(CircuitStackEntry entry, int x, int y, int size) {}
}
