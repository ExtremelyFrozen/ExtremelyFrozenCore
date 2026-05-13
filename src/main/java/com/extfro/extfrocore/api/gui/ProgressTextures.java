package com.extfro.extfrocore.api.gui;

import com.extfro.extfrocore.api.gui.texture.CroppedTexture;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.data.FillDirection;

public record ProgressTextures(IGuiTexture background, IGuiTexture bar, FillDirection fillDirection) {

    public static ProgressTextures from(SpriteTexture texture, FillDirection fillDirection) {
        return new ProgressTextures(
                CroppedTexture.of(texture.getImageLocation().toString(), 0, 0, 1, 0.5f),
                CroppedTexture.of(texture.getImageLocation().toString(), 0, 0.5f, 1, 0.5f),
                fillDirection);
    }
}
