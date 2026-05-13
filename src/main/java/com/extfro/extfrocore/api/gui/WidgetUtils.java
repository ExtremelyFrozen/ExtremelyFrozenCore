package com.extfro.extfrocore.api.gui;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class WidgetUtils {

    public static List<UIElement> getWidgetsById(UIElement group, String regex) {
        return group.selectRegex(regex).toList();
    }

    @Nullable
    public static UIElement getFirstWidgetById(UIElement group, String regex) {
        return getWidgetsById(group, regex).stream().findFirst().orElse(null);
    }

    public static void widgetByIdForEach(UIElement group, String regex, Consumer<UIElement> consumer) {
        getWidgetsById(group, regex).forEach(consumer);
    }

    public static <T extends UIElement> void widgetByIdForEach(UIElement group, String regex, Class<T> clazz,
                                                               Consumer<T> consumer) {
        for (UIElement widget : getWidgetsById(group, regex)) {
            if (clazz.isInstance(widget)) {
                consumer.accept(clazz.cast(widget));
            }
        }
    }

    public static int widgetIdIndex(UIElement widget) {
        var id = widget.getId();
        if (id.isEmpty()) return -1;
        var split = id.split("_");
        if (split.length == 0) return -1;
        var end = split[split.length - 1];
        try {
            return Integer.parseInt(end);
        } catch (Exception e) {
            return -1;
        }
    }

    public static int getInventoryHeight(boolean includeHotbar) {
        return 64 + (includeHotbar ? 22 : 0);
    }
}
