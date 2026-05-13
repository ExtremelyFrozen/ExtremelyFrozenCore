package com.extfro.extfrocore.api.gui.widget;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.data.lang.LangHandler;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib2.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * A widget for selecting a value from an enum or a subset of its values.
 */
public class EnumSelectorWidget<T extends Enum<T> & EnumSelectorWidget.SelectableEnum> extends UIElement {

    public interface SelectableEnum {

        String getTooltip();

        default IGuiTexture getIcon() {
            return GuiTextures.BLANK_TRANSPARENT;
        }
    }

    public final Button buttonWidget;

    public final List<T> values;
    public final Consumer<T> onChanged;

    public int selected = 0;
    private final int width;
    private final int height;

    private BiFunction<T, IGuiTexture, IGuiTexture> textureSupplier = (value, texture) -> new GuiTextureGroup(
            GuiTextures.VANILLA_BUTTON, texture);

    private BiFunction<T, String, List<Component>> tooltipSupplier = (value, key) -> List
            .copyOf(LangHandler.getSingleOrMultiLang(key));

    public EnumSelectorWidget(int xPosition, int yPosition, int width, int height, T[] values, T initialValue,
                              Consumer<T> onChanged) {
        this(xPosition, yPosition, width, height, Arrays.asList(values), initialValue, onChanged);
    }

    public EnumSelectorWidget(int xPosition, int yPosition, int width, int height, List<T> values, T initialValue,
                              Consumer<T> onChanged) {
        this.width = width;
        this.height = height;
        layout(layout -> layout.left(xPosition).top(yPosition).width(width).height(height));

        this.values = values;
        this.onChanged = onChanged;

        this.buttonWidget = new Button().noText();
        this.buttonWidget.layout(layout -> layout.left(0).top(0).width(width).height(height));
        this.buttonWidget.setOnServerClick(event -> onSelected((selected + 1) % values.size()));
        this.addChild(buttonWidget);

        setSelected(initialValue);
    }

    public void writeInitialData(RegistryFriendlyByteBuf buffer) {
        buffer.writeInt(selected);
    }

    public void readInitialData(RegistryFriendlyByteBuf buffer) {
        onSelected(buffer.readInt());
    }

    public T getCurrentValue() {
        return values.get(selected);
    }

    public IGuiTexture getTexture(int selected) {
        var selectedValue = values.get(selected);
        return textureSupplier.apply(selectedValue, selectedValue.getIcon());
    }

    private void onSelected(int selected) {
        T selectedValue = values.get(selected);
        setSelected(selectedValue);
    }

    public EnumSelectorWidget<T> setTextureSupplier(BiFunction<T, IGuiTexture, IGuiTexture> textureSupplier) {
        this.textureSupplier = textureSupplier;

        T selectedValue = getCurrentValue();
        setButtonTexture(selectedValue);

        return this;
    }

    public EnumSelectorWidget<T> setTooltipSupplier(BiFunction<T, String, List<Component>> tooltipSupplier) {
        this.tooltipSupplier = tooltipSupplier;

        return this;
    }

    public void setSelected(@NotNull T value) {
        var selectedIndex = values.indexOf(value);

        if (selectedIndex == -1)
            throw new NoSuchElementException(value + " is not a possible value for this selector.");

        this.selected = selectedIndex;

        setButtonTexture(value);
        updateTooltip();

        onChanged.accept(value);
    }

    private void setButtonTexture(T value) {
        IGuiTexture texture = textureSupplier.apply(value, value.getIcon());
        buttonWidget.buttonStyle(style -> style
                .baseTexture(texture)
                .hoverTexture(texture)
                .pressedTexture(texture));
        buttonWidget.layout(layout -> layout.width(width).height(height));
    }

    private void updateTooltip() {
        if (!ExtForCore.isClientThread())
            return;

        T selectedValue = getCurrentValue();
        buttonWidget.style(style -> style.tooltips(tooltipSupplier.apply(selectedValue, selectedValue.getTooltip())
                .toArray(Component[]::new)));
    }
}
