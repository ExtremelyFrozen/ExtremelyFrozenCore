package com.extfro.extfrocore.api.gui.fancy;

import com.extfro.extfrocore.api.gui.GuiTextures;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib2.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PageSwitcher implements IFancyUIProvider {

    private final Consumer<IFancyUIProvider> onPageSwitched;

    private List<IFancyUIProvider> pages = List.of();
    private IFancyUIProvider currentPage = null;

    public PageSwitcher(Consumer<IFancyUIProvider> onPageSwitched) {
        this.onPageSwitched = onPageSwitched;
    }

    public void setPageList(List<IFancyUIProvider> allPages, IFancyUIProvider currentPage) {
        this.pages = allPages;
        this.currentPage = currentPage;
    }

    @Override
    public UIElement createMainPage(FancyMachineUIWidget widget) {
        UIElement container = new UIElement()
                .layout(layout -> layout.width(176).height(166))
                .style(style -> style.background(GuiTextures.BACKGROUND_INVERSE));

        ScrollerView scroller = new ScrollerView();
        scroller.layout(layout -> layout.left(10).top(10).width(156).height(146));
        container.addChild(scroller);

        var groupedPages = pages.stream().collect(Collectors.groupingBy(
                page -> Objects.requireNonNullElse(page.getPageGroupingData(), new PageGroupingData(null, -1))));

        final MutableInt currentY = new MutableInt(0);
        groupedPages.keySet().stream()
                .sorted(Comparator.comparingInt(PageGroupingData::groupPositionWeight))
                .forEachOrdered(group -> {
                    if (group.groupKey() != null) {
                        Label label = new Label();
                        label.setValue(Component.translatable(group.groupKey()));
                        label.layout(layout -> layout.left(0).top(currentY.getAndAdd(12)).width(148).height(12));
                        label.textStyle(style -> style.textShadow(false).textColor(0x404040));
                        scroller.addScrollViewChild(label);
                    }

                    final var currentPage = new MutableInt(0);
                    currentY.subtract(30);

                    groupedPages.get(group).forEach(page -> {
                        var index = currentPage.getAndIncrement();
                        var y = currentY.addAndGet(index % 5 == 0 ? 30 : 0);

                        Button pageButton = new Button().noText();
                        pageButton.layout(layout -> layout.left((index % 5) * 30).top(y).width(25).height(25));
                        pageButton.buttonStyle(style -> style
                                .baseTexture(new GuiTextureGroup(GuiTextures.BACKGROUND, page.getTabIcon()))
                                .hoverTexture(new GuiTextureGroup(GuiTextures.BUTTON, page.getTabIcon()))
                                .pressedTexture(new GuiTextureGroup(GuiTextures.BUTTON, page.getTabIcon())));
                        pageButton.style(style -> style.tooltips(page.getTitle()));
                        pageButton.setOnServerClick(event -> onPageSwitched.accept(page));
                        scroller.addScrollViewChild(pageButton);
                    });

                    if (!groupedPages.get(group).isEmpty()) {
                        currentY.add(30);
                    }
                });

        return container;
    }

    @Override
    public IGuiTexture getTabIcon() {
        return new TextTexture("+").setDropShadow(false).setColor(ChatFormatting.BLACK.getColor());
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gtceu.gui.title_bar.page_switcher");
    }

    @Override
    public boolean hasPlayerInventory() {
        return false;
    }
}
