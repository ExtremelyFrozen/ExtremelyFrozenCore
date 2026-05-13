package com.extfro.extfrocore.api.gui.widget;

import net.minecraft.util.Mth;

import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import com.lowdragmc.lowdraglib2.math.Position;
import com.lowdragmc.lowdraglib2.math.Size;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A widget containing an integer input field, as well as adjacent buttons for increasing or decreasing the value.
 *
 * <p>
 * The buttons' change amount can be altered with Ctrl, Shift, or both.<br>
 * The input is limited by a minimum and maximum value.
 * </p>
 */
public class IntInputWidget extends NumberInputWidget<Integer> {

    public IntInputWidget(Supplier<Integer> valueSupplier, Consumer<Integer> onChanged) {
        super(valueSupplier, onChanged);
    }

    public IntInputWidget(Position position, Supplier<Integer> valueSupplier, Consumer<Integer> onChanged) {
        super(position, valueSupplier, onChanged);
    }

    public IntInputWidget(Position position, Size size, Supplier<Integer> valueSupplier, Consumer<Integer> onChanged) {
        super(position, size, valueSupplier, onChanged);
    }

    public IntInputWidget(int x, int y, int width, int height, Supplier<Integer> valueSupplier,
                          Consumer<Integer> onChanged) {
        super(x, y, width, height, valueSupplier, onChanged);
    }

    @Override
    protected Integer defaultMin() {
        return 0;
    }

    @Override
    protected Integer defaultMax() {
        return Integer.MAX_VALUE;
    }

    @Override
    protected String toText(Integer value) {
        return String.valueOf(value);
    }

    @Override
    protected Integer fromText(String value) {
        return Integer.parseInt(value);
    }

    @Override
    protected NumberInputWidget.ChangeValues<Integer> getChangeValues() {
        return new NumberInputWidget.ChangeValues<>(1, 8, 64, 512);
    }

    @Override
    protected Integer getOne(boolean positive) {
        return positive ? 1 : -1;
    }

    @Override
    protected Integer add(Integer a, Integer b) {
        return a + b;
    }

    @Override
    protected Integer multiply(Integer a, Integer b) {
        return a * b;
    }

    @Override
    protected Integer clamp(Integer value, Integer min, Integer max) {
        return Mth.clamp(value, min, max);
    }

    @Override
    protected void setTextFieldRange(TextField textField, Integer min, Integer max) {
        textField.setNumbersOnlyInt(min, max);
    }
}
