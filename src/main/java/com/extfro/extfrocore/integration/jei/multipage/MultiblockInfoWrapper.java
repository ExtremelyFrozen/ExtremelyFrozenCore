package com.extfro.extfrocore.integration.jei.multipage;

import com.extfro.extfrocore.api.gui.widget.PatternPreviewWidget;
import com.extfro.extfrocore.api.machine.MultiblockMachineDefinition;

import com.lowdragmc.lowdraglib2.integration.xei.jei.ModularWrapper;

public class MultiblockInfoWrapper extends ModularWrapper<PatternPreviewWidget> {

    public final MultiblockMachineDefinition definition;

    public MultiblockInfoWrapper(MultiblockMachineDefinition definition) {
        super(PatternPreviewWidget.getPatternWidget(definition));
        this.definition = definition;
    }
}
