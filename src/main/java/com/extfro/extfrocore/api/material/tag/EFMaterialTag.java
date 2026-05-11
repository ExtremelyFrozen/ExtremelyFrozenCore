package com.extfro.extfrocore.api.material.tag;

import com.extfro.extfrocore.api.material.EFMaterial;
import com.extfro.extfrocore.api.material.info.EFMaterialIconType;
import com.extfro.extfrocore.utils.FormattingUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import org.jetbrains.annotations.NotNull;

public class EFMaterialTag {

    private final String name;
    private final EFMaterialIconType materialIconType;
    private final long materialAmount;

    public EFMaterialTag(@NotNull String name, @NotNull EFMaterialIconType materialIconType, long materialAmount) {
        this.name = name;
        this.materialIconType = materialIconType;
        this.materialAmount = materialAmount;
    }

    public String getName() {
        return name;
    }

    public EFMaterialIconType materialIconType() {
        return materialIconType;
    }

    public long getMaterialAmount(EFMaterial material) {
        return materialAmount;
    }

    public String getRegisteredName(@NotNull EFMaterial material) {
        return name + "_" + material.getName();
    }

    public String getUnlocalizedName(@NotNull EFMaterial material) {
        return "item." + material.getModId() + "." + getRegisteredName(material);
    }

    public MutableComponent getLocalizedName(@NotNull EFMaterial material) {
        return Component.translatable(getUnlocalizedName(material));
    }

    public String getDefaultEnglishName(@NotNull EFMaterial material) {
        return splitCamel(FormattingUtil.lowerUnderscoreToUpperCamel(name)) +
                " " + material.getDefaultTranslation();
    }

    private static String splitCamel(String text) {
        return text.replaceAll("(?<!^)([A-Z])", " $1");
    }
}
