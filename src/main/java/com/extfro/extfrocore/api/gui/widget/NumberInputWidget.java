package com.extfro.extfrocore.api.gui.widget;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.utils.GTUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

import com.lowdragmc.lowdraglib2.gui.sync.rpc.RPCEmitter;
import com.lowdragmc.lowdraglib2.gui.sync.rpc.RPCEventBuilder;
import com.lowdragmc.lowdraglib2.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import com.lowdragmc.lowdraglib2.math.Position;
import com.lowdragmc.lowdraglib2.math.Size;
import lombok.Getter;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A UIElement containing a numeric input field and adjacent decrement/increment buttons.
 */
public abstract class NumberInputWidget<T extends Number> extends UIElement {

    protected abstract T defaultMin();

    protected abstract T defaultMax();

    protected abstract String toText(T value);

    protected abstract T fromText(String value);

    protected record ChangeValues<T extends Number>(T regular, T shift, T ctrl, T ctrlShift) {}

    protected abstract ChangeValues<T> getChangeValues();

    protected abstract T add(T a, T b);

    protected abstract T multiply(T a, T b);

    protected abstract T clamp(T value, T min, T max);

    protected abstract void setTextFieldRange(TextField textField, T min, T max);

    protected abstract T getOne(boolean positive);

    private final ChangeValues<T> changeValues = getChangeValues();
    private final T onePositive = getOne(true);
    private final T oneNegative = getOne(false);

    @Getter
    private Supplier<T> valueSupplier;
    @Getter
    private T min = defaultMin();
    @Getter
    private T max = defaultMax();

    private final Consumer<T> onChanged;
    private TextField textField;
    private String[] hoverTooltips = new String[0];
    private final RPCEmitter setTextRpc = addRPCEvent(RPCEventBuilder.simple(String.class, this::setValueFromText));
    private final RPCEmitter decreaseRpc = addRPCEvent(RPCEventBuilder.simple(Boolean.class, Boolean.class,
            (shift, ctrl) -> changeValue(shift, ctrl, oneNegative)));
    private final RPCEmitter increaseRpc = addRPCEvent(RPCEventBuilder.simple(Boolean.class, Boolean.class,
            (shift, ctrl) -> changeValue(shift, ctrl, onePositive)));

    public NumberInputWidget(Supplier<T> valueSupplier, Consumer<T> onChanged) {
        this(0, 0, 100, 20, valueSupplier, onChanged);
    }

    public NumberInputWidget(Position position, Supplier<T> valueSupplier, Consumer<T> onChanged) {
        this(position, new Size(100, 20), valueSupplier, onChanged);
    }

    public NumberInputWidget(Position position, Size size, Supplier<T> valueSupplier, Consumer<T> onChanged) {
        this(position.x, position.y, size.width, size.height, valueSupplier, onChanged);
    }

    public NumberInputWidget(int x, int y, int width, int height, Supplier<T> valueSupplier, Consumer<T> onChanged) {
        this.valueSupplier = valueSupplier;
        this.onChanged = onChanged;
        layout(layout -> layout.left(x).top(y).width(width).height(height));
        buildUI(width);
    }

    public NumberInputWidget<T> setHoverTooltips(String... tooltipText) {
        this.hoverTooltips = tooltipText;
        if (textField != null) {
            textField.style(style -> style.tooltips(tooltipText));
        }
        return this;
    }

    private void buildUI(int width) {
        int buttonWidth = Mth.clamp(width / 5, 15, 40);
        int textFieldWidth = width - (2 * buttonWidth) - 4;

        addChild(createButton(0, buttonWidth, "-", decreaseRpc));

        this.textField = new TextField()
                .setText(toText(valueSupplier.get()), false)
                .setTextResponder(stringValue -> {
                    setTextRpc.send(stringValue);
                });
        textField.layout(layout -> layout.left(buttonWidth + 2).top(0).width(textFieldWidth).height(20));
        updateTextFieldRange();
        addChild(textField);

        addChild(createButton(buttonWidth + textFieldWidth + 4, buttonWidth, "+", increaseRpc));
    }

    private Button createButton(int x, int width, String prefix, RPCEmitter clickRpc) {
        var button = new Button().noText();
        button.layout(layout -> layout.left(x).top(0).width(width).height(20));
        button.buttonStyle(style -> style
                .baseTexture(new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, getButtonTexture(prefix, width)))
                .hoverTexture(new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, getButtonTexture(prefix, width)))
                .pressedTexture(new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, getButtonTexture(prefix, width))));
        button.style(style -> style.tooltips("gui.widget.incrementButton.default_tooltip"));
        button.setOnClick(event -> {
            clickRpc.send(event.isShiftDown(), event.isCtrlDown());
            event.stopPropagation();
        });
        return button;
    }

    private IGuiTexture getButtonTexture(String prefix, int buttonWidth) {
        var texture = new TextTexture(prefix + "1");

        if (!ExtForCore.isClientThread()) {
            return texture;
        }

        int maxTextWidth = buttonWidth - 4;
        texture.setSupplier(() -> {
            T amount = GTUtil.isCtrlDown() ?
                    GTUtil.isShiftDown() ? changeValues.ctrlShift : changeValues.ctrl :
                    GTUtil.isShiftDown() ? changeValues.shift : changeValues.regular;

            String text = prefix + toText(amount);
            texture.scale(maxTextWidth / (float) Math.max(Minecraft.getInstance().font.width(text), maxTextWidth));
            return text;
        });

        return texture;
    }

    private void changeValue(boolean shift, boolean ctrl, T multiplier) {
        T amount = ctrl ?
                shift ? changeValues.ctrlShift : changeValues.ctrl :
                shift ? changeValues.shift : changeValues.regular;

        setValue(clamp(add(valueSupplier.get(), multiply(amount, multiplier)), min, max));
    }

    private void setValueFromText(String stringValue) {
        try {
            setValue(clamp(fromText(stringValue), min, max));
        } catch (NumberFormatException ignored) {}
    }

    public NumberInputWidget<T> setMin(T min) {
        this.min = min;
        updateTextFieldRange();
        return this;
    }

    public NumberInputWidget<T> setMax(T max) {
        this.max = max;
        updateTextFieldRange();
        return this;
    }

    public NumberInputWidget<T> setValue(T value) {
        onChanged.accept(value);
        if (textField != null) {
            textField.setText(toText(valueSupplier.get()), false);
        }
        return this;
    }

    protected void updateTextFieldRange() {
        if (textField == null) {
            return;
        }
        setTextFieldRange(textField, min, max);
        textField.style(style -> style.tooltips(hoverTooltips));
        setValue(clamp(valueSupplier.get(), min, max));
    }
}
