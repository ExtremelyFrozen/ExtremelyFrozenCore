package com.extfro.extfrocore.common.machine.gui;

import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.gui.widget.ExtendedProgressWidget;

import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib2.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.FillDirection;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

public final class MachineUIHelper {

    private MachineUIHelper() {}

    public static UIElement group(int x, int y, int width, int height) {
        return new UIElement().layout(layout -> layout.left(x).top(y).width(width).height(height));
    }

    public static UIElement group(int width, int height) {
        return new UIElement().layout(layout -> layout.width(width).height(height));
    }

    public static UIElement image(int x, int y, int width, int height, IGuiTexture texture) {
        return group(x, y, width, height).style(style -> style.backgroundTexture(texture));
    }

    public static ExtendedProgressWidget progress(int x, int y, int width, int height, DoubleSupplier progressSupplier,
                                                  IGuiTexture background, IGuiTexture bar,
                                                  FillDirection fillDirection) {
        var progress = new ExtendedProgressWidget(progressSupplier, x, y, width, height);
        progress.style(style -> style.background(background));
        progress.bar(barElement -> barElement.style(style -> style.background(bar)));
        progress.setFillDirection(fillDirection);
        progress.label(Label::disabled);
        return progress;
    }

    public static ExtendedProgressWidget progress(int x, int y, int width, int height, DoubleSupplier progressSupplier,
                                                  IGuiTexture bar) {
        return progress(x, y, width, height, progressSupplier, IGuiTexture.EMPTY, bar,
                FillDirection.LEFT_TO_RIGHT);
    }

    public static Label label(int x, int y, String translationKey) {
        return label(x, y, 150, 10, () -> Component.translatable(translationKey));
    }

    public static Label literalLabel(int x, int y, Supplier<String> supplier) {
        return label(x, y, () -> Component.literal(supplier.get()));
    }

    public static Label label(int x, int y, Supplier<Component> supplier) {
        return label(x, y, 150, 10, supplier);
    }

    public static Label label(int x, int y, int width, int height, Supplier<Component> supplier) {
        Label label = new Label();
        label.setValue(supplier.get());
        label.layout(layout -> layout.left(x).top(y).width(width).height(height));
        label.textStyle(style -> style.textColor(0x404040).textShadow(false));
        label.addEventListener(UIEvents.TICK, event -> label.setValue(supplier.get()));
        return label;
    }

    public static Label lightLabel(int x, int y, Supplier<Component> supplier) {
        Label label = label(x, y, supplier);
        label.textStyle(style -> style.textColor(0xFFFFFFFF).textShadow(false));
        return label;
    }

    public static UIElement componentPanel(int x, int y, int width, int lineHeight,
                                           Consumer<List<Component>> textSupplier) {
        UIElement panel = group(x, y, width, 10);
        panel.addEventListener(UIEvents.TICK, event -> refreshComponentPanel(panel, width, lineHeight, textSupplier));
        refreshComponentPanel(panel, width, lineHeight, textSupplier);
        return panel;
    }

    private static void refreshComponentPanel(UIElement panel, int width, int lineHeight,
                                              Consumer<List<Component>> textSupplier) {
        List<Component> text = new ArrayList<>();
        textSupplier.accept(text);
        panel.clearAllChildren();
        int y = 0;
        for (Component component : text) {
            panel.addChild(label(0, y, width, lineHeight, () -> component));
            y += lineHeight;
        }
        panel.layout(layout -> layout.width(width).height(Math.max(10, y)));
    }

    public static TextField intTextField(int x, int y, int width, int height, Supplier<Integer> supplier,
                                         Consumer<String> responder, int min, int max) {
        var field = new TextField();
        field.layout(layout -> layout.left(x).top(y).width(width).height(height));
        field.style(style -> style.background(GuiTextures.DISPLAY));
        field.textFieldStyle(style -> style.textColor(0x404040).textShadow(false));
        field.setNumbersOnlyInt(min, max);
        field.setText(String.valueOf(supplier.get()), false);
        field.setTextResponder(text -> {
            if (!text.isBlank()) {
                responder.accept(text);
            }
        });
        field.addEventListener(UIEvents.TICK, event -> {
            if (!field.isFocused()) {
                field.setText(String.valueOf(supplier.get()), false);
            }
        });
        return field;
    }

    public static TextField longTextField(int x, int y, int width, int height, Supplier<Long> supplier,
                                          Consumer<String> responder, long min, long max) {
        var field = new TextField();
        field.layout(layout -> layout.left(x).top(y).width(width).height(height));
        field.style(style -> style.background(GuiTextures.DISPLAY));
        field.textFieldStyle(style -> style.textColor(0x404040).textShadow(false));
        field.setNumbersOnlyLong(min, max);
        field.setText(String.valueOf(supplier.get()), false);
        field.setTextResponder(text -> {
            if (!text.isBlank()) {
                responder.accept(text);
            }
        });
        field.addEventListener(UIEvents.TICK, event -> {
            if (!field.isFocused()) {
                field.setText(String.valueOf(supplier.get()), false);
            }
        });
        return field;
    }

    public static Button textButton(int x, int y, int width, int height, Supplier<Component> textSupplier,
                                    Consumer<UIEvent> onClick) {
        var button = new Button().noText();
        button.layout(layout -> layout.left(x).top(y).width(width).height(height));
        button.setOnServerClick(event -> onClick.accept(event));
        button.addEventListener(UIEvents.TICK, event -> applyButtonText(button, textSupplier.get()));
        applyButtonText(button, textSupplier.get());
        return button;
    }

    private static void applyButtonText(Button button, Component text) {
        var texture = new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture(text.getString()));
        button.buttonStyle(style -> style.baseTexture(texture).hoverTexture(texture).pressedTexture(texture));
    }
}
