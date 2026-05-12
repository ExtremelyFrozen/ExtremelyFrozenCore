package com.extfro.extfrocore.api.gui.fancy;

import com.extfro.extfrocore.api.gui.GuiTextures;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib2.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ConfiguratorPanel extends UIElement {

    protected final List<Tab> tabs = new ArrayList<>();
    @Nullable
    protected Tab expanded;
    protected int border = 4;
    protected IGuiTexture texture = GuiTextures.BACKGROUND;
    protected int tabSize = 24;

    public ConfiguratorPanel(int x, int y) {
        layout(layout -> layout.left(x).top(y).width(tabSize).height(0));
    }

    public List<Tab> getTabs() {
        return tabs;
    }

    @Nullable
    public Tab getExpanded() {
        return expanded;
    }

    public ConfiguratorPanel setBorder(int border) {
        this.border = border;
        return this;
    }

    public ConfiguratorPanel setTexture(IGuiTexture texture) {
        this.texture = texture;
        return this;
    }

    public void clear() {
        clearAllChildren();
        tabs.clear();
        expanded = null;
        layout(layout -> layout.height(0));
    }

    public int getTabSize() {
        return tabSize;
    }

    public void attachConfigurators(IFancyConfigurator... fancyConfigurators) {
        for (IFancyConfigurator fancyConfigurator : fancyConfigurators) {
            var tab = new Tab(fancyConfigurator);
            tabs.add(tab);
            addChild(tab);
        }
        relayoutCollapsedTabs();
    }

    public void expandTab(Tab tab) {
        if (expanded != null && expanded != tab) {
            expanded.setExpanded(false);
        }
        expanded = tab;
        tab.setExpanded(true);
        relayoutCollapsedTabs();
    }

    public void collapseTab() {
        if (expanded != null) {
            expanded.setExpanded(false);
            if (expanded instanceof FloatingTab floatingTab) {
                floatingTab.close();
            }
        }
        expanded = null;
        relayoutCollapsedTabs();
    }

    public FloatingTab createFloatingTab(IFancyConfigurator configurator) {
        return new FloatingTab(configurator);
    }

    public void removeTab(Tab tab) {
        removeChild(tab);
        tabs.remove(tab);
        if (expanded == tab) expanded = null;
        relayoutCollapsedTabs();
    }

    private void relayoutCollapsedTabs() {
        int collapsedIndex = 0;
        for (Tab tab : tabs) {
            if (tab == expanded) {
                continue;
            }
            tab.layout(layout -> layout.left(0).top(collapsedIndex++ * (tabSize + 2)).width(tabSize).height(tabSize));
        }
        if (expanded != null) {
            expanded.layout(layout -> layout.left(-expanded.getExpandedWidth() + tabSize).top(0)
                    .width(expanded.getExpandedWidth()).height(expanded.getExpandedHeight()));
        }
        layout(layout -> layout.height(Math.max(0, tabs.size() * (tabSize + 2) - 2)));
    }

    public class Tab extends UIElement {

        protected final IFancyConfigurator configurator;
        protected final Button button;
        @Nullable
        protected final UIElement view;
        protected final int expandedWidth;
        protected final int expandedHeight;

        public Tab(IFancyConfigurator configurator) {
            this.configurator = configurator;
            this.button = new Button().noText();
            this.button.layout(layout -> layout.left(0).top(0).width(tabSize).height(tabSize));
            this.button.buttonStyle(style -> style
                    .baseTexture(new GuiTextureGroup(texture, configurator.getIcon()))
                    .hoverTexture(new GuiTextureGroup(texture, configurator.getIcon()))
                    .pressedTexture(new GuiTextureGroup(texture, configurator.getIcon())));
            this.button.style(style -> style.tooltips(configurator.getTooltips().toArray(Component[]::new)));
            this.button.setOnServerClick(event -> onClick());
            addChild(button);

            if (configurator instanceof IFancyConfiguratorButton) {
                this.view = null;
                this.expandedWidth = tabSize;
                this.expandedHeight = tabSize;
            } else {
                UIElement content = configurator.createConfigurator();
                int contentWidth = Math.max(120, (int) content.getSizeWidth());
                int contentHeight = Math.max(80, (int) content.getSizeHeight());
                this.expandedWidth = contentWidth + border * 2 + tabSize;
                this.expandedHeight = contentHeight + border * 2 + tabSize;
                this.view = new UIElement()
                        .layout(layout -> layout.left(0).top(0).width(expandedWidth).height(expandedHeight))
                        .style(style -> style.background(texture));
                content.layout(layout -> layout.left(border).top(tabSize).width(contentWidth).height(contentHeight));
                view.addChild(content);
                view.addChild(new UIElement()
                        .layout(layout -> layout.left(border).top(border).width(Math.max(0, contentWidth - 4))
                                .height(tabSize - border))
                        .style(style -> style.background(new TextTexture(configurator.getTitle().getString())
                                .setType(TextTexture.TextType.LEFT_HIDE)
                                .setWidth(contentWidth))));
                view.setVisible(false);
                view.setActive(false);
                addChild(view);
            }
        }

        public int getExpandedWidth() {
            return expandedWidth;
        }

        public int getExpandedHeight() {
            return expandedHeight;
        }

        public void writeInitialData(RegistryFriendlyByteBuf buffer) {
            configurator.writeInitialData(buffer);
        }

        public void readInitialData(RegistryFriendlyByteBuf buffer) {
            configurator.readInitialData(buffer);
        }

        public void detectAndSendChanges() {
            configurator.detectAndSendChange(this::writeUpdateInfo);
        }

        public void readUpdateInfo(int id, RegistryFriendlyByteBuf buffer) {
            configurator.readUpdateInfo(id, buffer);
        }

        protected void writeUpdateInfo(int id, Consumer<RegistryFriendlyByteBuf> sender) {
            // LDLib2 event/RPC wiring is handled by child UIElements; configurator-specific legacy sync is preserved
            // at the data holder level and can be expanded per configurator while migrating individual pages.
        }

        protected void onClick() {
            if (configurator instanceof IFancyConfiguratorButton fancyButton) {
                fancyButton.onClick(new com.lowdragmc.lowdraglib2.gui.util.ClickData());
            } else if (expanded == this) {
                collapseTab();
            } else {
                expandTab(this);
            }
        }

        protected void setExpanded(boolean expanded) {
            if (view != null) {
                view.setVisible(expanded);
                view.setActive(expanded);
            }
            button.layout(layout -> layout.left(getExpandedWidth() - tabSize).top(0).width(tabSize).height(tabSize));
        }
    }

    public class FloatingTab extends Tab {

        protected Runnable closeCallback = () -> {};

        public FloatingTab(IFancyConfigurator configurator) {
            super(configurator);
        }

        public void onClose(Runnable closeCallback) {
            this.closeCallback = closeCallback;
        }

        private void close() {
            removeTab(this);
            closeCallback.run();
        }
    }
}
