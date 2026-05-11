package com.extfro.extfrocore.integration.xei.multipage;

import com.extfro.extfrocore.api.pattern.MultiblockShapeInfo;

public record MultiblockInfoPage(int index, MultiblockShapeInfo shape) {

    public MultiblockInfoPage {
        if (index < 0) {
            throw new IllegalArgumentException("Page index must not be negative");
        }
    }
}
