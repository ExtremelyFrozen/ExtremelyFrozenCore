package com.extfro.extfrocore.api.gui.fancy;

import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.config.ConfigHolder;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.inventory.InventorySlots;
import com.lowdragmc.lowdraglib2.gui.util.ClickData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.stream.Stream;

public class FancyMachineUIWidget extends UIElement {

    protected final TitleBarWidget titleBar;
    protected final VerticalTabsWidget sideTabsWidget;
    protected final UIElement pageContainer;
    protected final PageSwitcher pageSwitcher;
    protected final ConfiguratorPanel configuratorPanel;
    protected final TooltipsPanel tooltipsPanel;

    @Nullable
    protected final InventorySlots playerInventory;
    protected int border = 4;

    protected final IFancyUIProvider mainPage;
    protected IFancyUIProvider currentPage;
    protected IFancyUIProvider currentHomePage;
    protected List<IFancyUIProvider> allPages;
    protected final int baseWidth;
    protected final int baseHeight;

    protected Deque<NavigationEntry> previousPages = new ArrayDeque<>();

    protected record NavigationEntry(IFancyUIProvider page, IFancyUIProvider homePage, Runnable onNavigation) {}

    public FancyMachineUIWidget(IFancyUIProvider mainPage, int width, int height) {
        this.mainPage = mainPage;
        this.baseWidth = width;
        this.baseHeight = height;
        layout(layout -> layout.width(width).height(height));
        style(style -> style.background(GuiTextures.BACKGROUND.copy()
                .setColor(Long.decode(ConfigHolder.INSTANCE.client.defaultUIColor).intValue() | 0xFF000000)));

        addChild(this.pageContainer = new UIElement().layout(layout -> layout.left(0).top(0).width(width).height(height)));

        if (mainPage.hasPlayerInventory()) {
            addChild(this.playerInventory = new InventorySlots());
            this.playerInventory.layout(layout -> layout.left(7).top(height - 82).width(162).height(76));
            this.playerInventory.apply(slot -> slot.style(style -> style.background(GuiTextures.SLOT)));
        } else {
            playerInventory = null;
        }

        addChild(this.titleBar = new TitleBarWidget(width, this::navigateBack, this::openPageSwitcher));
        addChild(this.sideTabsWidget = new VerticalTabsWidget(this::navigate, -20, 0, 24, height));
        addChild(this.tooltipsPanel = new TooltipsPanel());
        addChild(this.configuratorPanel = new ConfiguratorPanel(-(24 + 2), height));
        this.pageSwitcher = new PageSwitcher(this::switchPage);

        this.allPages = Stream.concat(Stream.of(this.mainPage), this.mainPage.getSubTabs().stream()).toList();
        performNavigation(this.mainPage, this.mainPage);
    }

    public ConfiguratorPanel getConfiguratorPanel() {
        return configuratorPanel;
    }

    public TooltipsPanel getTooltipsPanel() {
        return tooltipsPanel;
    }

    public VerticalTabsWidget getSideTabsWidget() {
        return sideTabsWidget;
    }

    protected void navigate(IFancyUIProvider newPage) {
        navigate(newPage, this.currentHomePage);
    }

    protected void navigate(IFancyUIProvider nextPage, IFancyUIProvider nextHomePage) {
        if (nextPage != mainPage) {
            if (!this.previousPages.isEmpty() && this.previousPages.peek().page == nextPage) {
                this.previousPages.pop();
            } else if (this.currentPage != null) {
                this.previousPages.push(new NavigationEntry(this.currentPage, this.currentHomePage, () -> {}));
            }
        } else {
            this.previousPages.clear();
        }

        performNavigation(nextPage, nextHomePage);
    }

    protected void navigateBack(ClickData clickData) {
        if (previousPages.isEmpty()) return;
        NavigationEntry navigationEntry = previousPages.pop();
        performNavigation(navigationEntry.page, navigationEntry.homePage);
        navigationEntry.onNavigation.run();
    }

    protected void performNavigation(IFancyUIProvider nextPage, IFancyUIProvider nextHomePage) {
        if (currentHomePage != nextHomePage) {
            setupSideTabs(nextHomePage);
        }

        this.currentPage = nextPage;
        this.currentHomePage = nextHomePage;

        if (currentPage != currentHomePage) {
            setupFancyUI(currentHomePage);
        }

        setupFancyUI(nextPage, nextPage.hasPlayerInventory());
    }

    protected void openPageSwitcher(ClickData clickData) {
        pageSwitcher.setPageList(allPages, currentHomePage);

        if (currentPage != currentHomePage && !previousPages.isEmpty()) {
            previousPages.pop();
        }

        this.sideTabsWidget.setVisible(false);
        this.sideTabsWidget.setActive(false);

        this.previousPages.push(new NavigationEntry(currentHomePage, currentHomePage, () -> {
            sideTabsWidget.setVisible(true);
            sideTabsWidget.setActive(true);
        }));

        this.currentPage = this.pageSwitcher;
        this.currentHomePage = this.pageSwitcher;

        setupFancyUI(this.pageSwitcher);
    }

    protected void switchPage(IFancyUIProvider nextHomePage) {
        this.currentHomePage = mainPage;
        this.currentPage = mainPage;
        this.previousPages.clear();

        sideTabsWidget.setVisible(true);
        sideTabsWidget.setActive(true);

        setupSideTabs(this.currentHomePage);
        navigate(nextHomePage, nextHomePage);
    }

    protected void setupFancyUI(IFancyUIProvider fancyUI) {
        this.setupFancyUI(fancyUI, fancyUI.hasPlayerInventory());
    }

    protected void setupFancyUI(IFancyUIProvider fancyUI, boolean showInventory) {
        clearUI();

        sideTabsWidget.selectTab(fancyUI);
        titleBar.updateState(
                fancyUI,
                !this.previousPages.isEmpty(),
                this.allPages.size() > 1 && this.currentPage != this.pageSwitcher);

        var page = fancyUI.createMainPage(this);
        int pageHeight = showInventory && playerInventory != null ? baseHeight - 82 : baseHeight;
        pageContainer.layout(layout -> layout.left(0).top(0).width(baseWidth).height(pageHeight));
        page.layout(layout -> layout.left(border).top(border));
        pageContainer.addChild(page);

        setupInventoryPosition(showInventory, pageHeight);

        fancyUI.attachConfigurators(configuratorPanel);
        configuratorPanel.layout(layout -> layout.left(-24 - 2).top(Math.max(0, pageHeight - 4)));
        fancyUI.attachTooltips(tooltipsPanel);
        tooltipsPanel.layout(layout -> layout.left(baseWidth + 2).top(2));

        titleBar.setSize(baseWidth, 16);
        sideTabsWidget.setSize(24, pageHeight);
    }

    private void setupInventoryPosition(boolean showInventory, int pageHeight) {
        if (this.playerInventory == null)
            return;

        this.playerInventory.layout(layout -> layout.left((baseWidth - 162) / 2f).top(pageHeight).width(162).height(76));
        this.playerInventory.setActive(showInventory);
        this.playerInventory.setVisible(showInventory);
    }

    protected void clearUI() {
        this.pageContainer.clearAllChildren();
        this.configuratorPanel.clear();
        this.tooltipsPanel.clear();
    }

    protected void setupSideTabs(IFancyUIProvider currentHomePage) {
        this.sideTabsWidget.clearSubTabs();
        currentHomePage.attachSideTabs(sideTabsWidget);
    }
}
