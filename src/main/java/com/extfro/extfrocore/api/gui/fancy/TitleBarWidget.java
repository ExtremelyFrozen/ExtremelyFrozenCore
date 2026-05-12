package com.extfro.extfrocore.api.gui.fancy;

import com.extfro.extfrocore.api.gui.GuiTextures;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib2.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.util.ClickData;

import java.util.function.Consumer;

public class TitleBarWidget extends UIElement {

    private static final int HORIZONTAL_MARGIN = 8;
    private static final int HEIGHT = 16;
    private static final int BTN_WIDTH = 18;

    private final Consumer<ClickData> onBackClicked;
    private final Consumer<ClickData> onMenuClicked;
    private int width;

    public TitleBarWidget(int parentWidth, Consumer<ClickData> onBackClicked, Consumer<ClickData> onMenuClicked) {
        this.onBackClicked = onBackClicked;
        this.onMenuClicked = onMenuClicked;
        setSize(parentWidth, HEIGHT);
        layout(layout -> layout.left(HORIZONTAL_MARGIN).top(-HEIGHT));
    }

    public void updateState(IFancyUIProvider currentPage, boolean showBackButton, boolean showMenuButton) {
        clearAllChildren();

        int innerWidth = width - 2 * HORIZONTAL_MARGIN;
        int left = showBackButton ? BTN_WIDTH : 0;
        int right = showMenuButton ? BTN_WIDTH : 0;
        int titleWidth = Math.max(0, innerWidth - left - right);

        if (showBackButton) {
            addButton(0, Component.literal(" <"), Component.translatable("gtceu.gui.title_bar.back"), onBackClicked);
        }

        UIElement title = fixed(left, 0, titleWidth, HEIGHT)
                .style(style -> style.background(new GuiTextureGroup(
                        GuiTextures.TITLE_BAR_BACKGROUND,
                        currentPage.getTabIcon(),
                        titleText(currentPage))));
        addChild(title);

        if (showMenuButton) {
            addButton(left + titleWidth, Component.literal("+"),
                    Component.translatable("gtceu.gui.title_bar.page_switcher"), onMenuClicked);
        }
    }

    public void setSize(int width, int height) {
        this.width = width;
        layout(layout -> layout.width(width).height(height));
    }

    private void addButton(int x, Component text, Component tooltip, Consumer<ClickData> onClick) {
        Button button = new Button().setText(text);
        button.layout(layout -> layout.left(x).top(0).width(BTN_WIDTH).height(HEIGHT));
        button.buttonStyle(style -> style
                .baseTexture(GuiTextures.TITLE_BAR_BACKGROUND)
                .hoverTexture(GuiTextures.TITLE_BAR_BACKGROUND)
                .pressedTexture(GuiTextures.TITLE_BAR_BACKGROUND));
        button.textStyle(style -> style.textColor(ChatFormatting.BLACK.getColor()).textShadow(false));
        button.style(style -> style.tooltips(tooltip));
        button.setOnServerClick(event -> onClick.accept(new ClickData()));
        addChild(button);
    }

    private static IGuiTexture titleText(IFancyUIProvider currentPage) {
        return new TextTexture(ChatFormatting.BLACK + currentPage.getTitle().copy().getString())
                .setDropShadow(false)
                .setType(TextTexture.TextType.LEFT_HIDE);
    }

    private static UIElement fixed(int x, int y, int width, int height) {
        return new UIElement().layout(layout -> layout.left(x).top(y).width(width).height(height));
    }
}
