package com.extfro.extfrocore.api.gui.fancy;

import java.util.function.Consumer;

public class VerticalTabsWidget extends TabsWidget {

    public VerticalTabsWidget(Consumer<IFancyUIProvider> onTabClick, int x, int y, int width, int height) {
        super(onTabClick, x, y, width, height);
    }

    @Override
    protected void rebuildTabs() {
        clearAllChildren();
        if (mainTab == null) return;
        addTab(mainTab, 0, 8, true);
        int y = 32;
        for (IFancyUIProvider subTab : subTabs) {
            addTab(subTab, 0, y, false);
            y += 24;
        }
    }
}
