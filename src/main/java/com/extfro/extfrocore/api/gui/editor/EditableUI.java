package com.extfro.extfrocore.api.gui.editor;

import com.extfro.extfrocore.api.gui.WidgetUtils;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import lombok.Getter;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class EditableUI<W extends UIElement, T> implements IEditableUI<W, T> {

    @Getter
    final String id;
    final Class<W> clazz;
    @Getter
    final Supplier<W> widgetSupplier;
    @Getter
    final BiConsumer<W, T> binder;

    public EditableUI(String id, Class<W> clazz, Supplier<W> widgetSupplier, BiConsumer<W, T> binder) {
        this.id = id;
        this.clazz = clazz;
        this.widgetSupplier = widgetSupplier;
        this.binder = binder;
    }

    public W createDefault() {
        var widget = widgetSupplier.get();
        widget.setId(id);
        return widget;
    }

    public void setupUI(UIElement template, T instance) {
        WidgetUtils.widgetByIdForEach(template, "^" + id + "$", clazz, w -> binder.accept(w, instance));
    }
}
