package com.extfro.extfrocore.api.material.tag;

import com.extfro.extfrocore.api.material.info.EFMaterialIconType;

import org.jetbrains.annotations.NotNull;

public class EFMaterialTag extends EFTagPrefix {

    public EFMaterialTag(@NotNull String name, @NotNull EFMaterialIconType materialIconType, long materialAmount) {
        super(name);
        idPattern(name + "_%s");
        materialIconType(materialIconType);
        materialAmount(materialAmount);
        langValue(splitCamel(name) + " %s");
    }

    private static String splitCamel(String text) {
        return text.replaceAll("(?<!^)([A-Z])", " $1");
    }
}
