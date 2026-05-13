package com.extfro.extfrocore.integration.ae2.gui.widget;

import com.extfro.extfrocore.api.gui.GuiTextures;

import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib2.gui.sync.rpc.RPCEmitter;
import com.lowdragmc.lowdraglib2.gui.sync.rpc.RPCEventBuilder;
import com.lowdragmc.lowdraglib2.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib2.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import com.lowdragmc.lowdraglib2.math.Position;
import com.lowdragmc.lowdraglib2.math.Size;
import dev.vfyjxf.taffy.style.TaffyPosition;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.function.Consumer;

@Accessors(chain = true)
public class AETextInputButtonWidget extends UIElement {

    private Consumer<String> onConfirm = text -> {};
    private final RPCEmitter confirmRPC = addRPCEvent(RPCEventBuilder.simple(String.class, this::confirm));

    @Getter
    private String text = "";

    private Component[] hoverTexts = new Component[0];

    @Getter
    private boolean isInputting;

    private TextField textField;
    private Button button;

    public AETextInputButtonWidget() {}

    public AETextInputButtonWidget(int x, int y, int width, int height) {
        layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(x)
                .top(y)
                .width(width)
                .height(height));
        buildUI(width, height);
    }

    public AETextInputButtonWidget(Position position) {
        this(position.x, position.y, 100, 20);
    }

    public AETextInputButtonWidget(Position position, Size size) {
        this(position.x, position.y, size.width, size.height);
    }

    public AETextInputButtonWidget setOnConfirm(Consumer<String> onConfirm) {
        this.onConfirm = onConfirm == null ? text -> {} : onConfirm;
        return this;
    }

    public AETextInputButtonWidget setText(String text) {
        this.text = text == null ? "" : text;
        if (textField != null) {
            textField.setText(this.text, false);
        }
        return this;
    }

    public AETextInputButtonWidget setButtonTooltips(Component... tooltipTexts) {
        this.hoverTexts = tooltipTexts;
        if (button != null) {
            button.style(style -> style.tooltips(tooltipTexts));
        }
        return this;
    }

    private void buildUI(int width, int height) {
        this.textField = new TextField()
                .setText(text, false)
                .setTextResponder(this::setText);
        this.textField.layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(0)
                .top(0)
                .width(width - height - 2)
                .height(height));
        this.textField.setActive(false);
        this.textField.setVisible(false);
        addChild(textField);

        TextTexture icon = new TextTexture("✎");
        icon.setSupplier(() -> isInputting ? "✔" : "✎");
        this.button = new Button().noText()
                .buttonStyle(style -> style
                        .baseTexture(new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, icon))
                        .hoverTexture(new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, icon))
                        .pressedTexture(new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, icon)))
                .setOnClick(event -> {
                    if (isInputting) {
                        confirmRPC.send(text);
                    }
                    setInputting(!isInputting);
                    event.stopPropagation();
                });
        button.layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(width - height)
                .top(0)
                .width(height)
                .height(height));
        button.style(style -> style.tooltips(hoverTexts));
        addChild(button);
    }

    private void setInputting(boolean inputting) {
        this.isInputting = inputting;
        this.textField.setActive(inputting);
        this.textField.setVisible(inputting);
    }

    private void confirm(String value) {
        setText(value);
        onConfirm.accept(this.text);
    }
}
