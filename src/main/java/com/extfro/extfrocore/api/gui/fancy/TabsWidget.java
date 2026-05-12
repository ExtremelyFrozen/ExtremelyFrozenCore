package com.extfro.extfrocore.api.gui.fancy;

import com.extfro.extfrocore.api.gui.GuiTextures;

import com.lowdragmc.lowdraglib2.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class TabsWidget extends UIElement {

    protected final Consumer<IFancyUIProvider> onTabClick;
    protected IFancyUIProvider mainTab;
    protected final List<IFancyUIProvider> subTabs = new ArrayList<>();
    @Nullable
    protected IFancyUIProvider selectedTab;
    protected int width;
    protected int height;
    @Setter
    protected IGuiTexture tabTexture = GuiTextures.BUTTON;
    @Setter
    protected IGuiTexture tabHoverTexture = GuiTextures.VANILLA_BUTTON;
    @Setter
    protected IGuiTexture tabPressedTexture = tabHoverTexture;
    @Setter
    @Nullable
    protected BiConsumer<IFancyUIProvider, IFancyUIProvider> onTabSwitch;

    public TabsWidget(Consumer<IFancyUIProvider> onTabClick) {
        this(onTabClick, 0, -20, 200, 24);
    }

    public TabsWidget(Consumer<IFancyUIProvider> onTabClick, int x, int y, int width, int height) {
        this.onTabClick = onTabClick;
        this.width = width;
        this.height = height;
        layout(layout -> layout.left(x).top(y).width(width).height(height));
    }

    public void setMainTab(IFancyUIProvider mainTab) {
        this.mainTab = mainTab;
        if (this.selectedTab == null) {
            this.selectedTab = mainTab;
        }
        rebuildTabs();
    }

    public void clearSubTabs() {
        subTabs.clear();
        rebuildTabs();
    }

    public void attachSubTab(IFancyUIProvider subTab) {
        subTabs.add(subTab);
        rebuildTabs();
    }

    public void selectTab(IFancyUIProvider selectedTab) {
        this.selectedTab = selectedTab;
        rebuildTabs();
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
        layout(layout -> layout.width(width).height(height));
        rebuildTabs();
    }

    protected void rebuildTabs() {
        clearAllChildren();
        if (mainTab == null) return;
        addTab(mainTab, 8, 0, true);
        int x = Math.max(32, width - 8 - 24 * subTabs.size());
        for (IFancyUIProvider subTab : subTabs) {
            addTab(subTab, x, 0, false);
            x += 24;
        }
    }

    protected void addTab(IFancyUIProvider tab, int x, int y, boolean main) {
        Button button = new Button().noText();
        button.layout(layout -> layout.left(x).top(y).width(24).height(24));
        button.buttonStyle(style -> style
                .baseTexture(new GuiTextureGroup(tab == selectedTab ? tabPressedTexture : tabTexture, tab.getTabIcon()))
                .hoverTexture(new GuiTextureGroup(tabHoverTexture, tab.getTabIcon()))
                .pressedTexture(new GuiTextureGroup(tabPressedTexture, tab.getTabIcon())));
        button.style(style -> style.tooltips(tab.getTabTooltips().toArray(net.minecraft.network.chat.Component[]::new)));
        button.setOnServerClick(event -> switchTo(tab));
        addChild(button);
    }

    protected void switchTo(IFancyUIProvider tab) {
        if (tab == selectedTab) return;
        var old = selectedTab;
        selectedTab = tab;
        if (onTabSwitch != null && old != null) {
            onTabSwitch.accept(old, tab);
        }
        onTabClick.accept(tab);
        rebuildTabs();
    }
}
