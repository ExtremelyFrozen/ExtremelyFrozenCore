package com.extfro.extfrocore.api.gui.widget;

import com.extfro.extfrocore.api.gui.GuiTextures;

import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.DataBindingBuilder;
import com.lowdragmc.lowdraglib2.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

@Accessors(chain = true)
public class ConfirmTextInputWidget extends UIElement {

    private final Consumer<String> textResponder;
    @Nullable
    private final Function<String, String> returnValidator;
    private Function<String, String> validator = s -> s;
    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private String inputText = "";
    @Setter
    private String tooltip = "";

    public ConfirmTextInputWidget(int x, int y, int width, int height, String text,
                                  Consumer<String> textResponder,
                                  @Nullable Function<String, String> validator,
                                  @Nullable Function<String, String> returnValidator) {
        this.textResponder = textResponder;
        this.returnValidator = returnValidator;
        if (validator != null) {
            this.validator = validator;
        }
        if (text != null) {
            this.inputText = text;
        }
        layout(layout -> layout.left(x).top(y).width(width).height(height));
        buildUI(width, height);
    }

    private void buildUI(int width, int height) {
        Button confirmButton = new Button().noText()
                .buttonStyle(style -> style
                        .baseTexture(new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, GuiTextures.BUTTON_CHECK))
                        .hoverTexture(new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, GuiTextures.BUTTON_CHECK))
                        .pressedTexture(new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, GuiTextures.BUTTON_CHECK)))
                .setOnServerClick(event -> {
                    if (returnValidator != null) {
                        inputText = returnValidator.apply(inputText);
                    }
                    textResponder.accept(inputText);
                });
        confirmButton.layout(layout -> layout.left(width - height).top(0).width(height).height(height));
        addChild(confirmButton);

        TextField textField = new TextField();
        textField.layout(layout -> layout.left(1).top(1).width(width - height - 4).height(height - 2));
        textField.setTextValidator(s -> this.validator.apply(s).equals(s));
        textField.bindDataSource(DataBindingBuilder.create(this::getInputText, this::setInputText)
                .syncType(String.class)
                .remoteSetter(this::setInputText)
                .build());
        textField.addEventListener(UIEvents.BLUR, event -> setInputText(validator.apply(textField.getText())));
        if (!tooltip.isEmpty()) {
            textField.style(style -> style.tooltips(Component.translatable(tooltip)));
        }
        addChild(textField);
    }
}
