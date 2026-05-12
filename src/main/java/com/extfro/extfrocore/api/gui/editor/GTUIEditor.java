package com.extfro.extfrocore.api.gui.editor;

import com.lowdragmc.lowdraglib2.LDLib;
import com.lowdragmc.lowdraglib2.gui.editor.ui.UIEditor;
import com.lowdragmc.lowdraglib2.gui.editor.ui.tool.WidgetToolBox;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;

import static com.lowdragmc.lowdraglib2.gui.editor.ui.tool.WidgetToolBox.Default.registerTab;

@LDLRegister(name = "editor.gtceu", group = "editor")
public class GTUIEditor extends UIEditor {

    public static final WidgetToolBox.Default GT_CONTAINER = registerTab("widget.gtm_container",
            Icons.WIDGET_CONTAINER);

    public GTUIEditor() {
        super(LDLib.getLDLibDir());
    }
}
