package com.extfro.extfrocore.api.gui.texture;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import com.mojang.blaze3d.systems.RenderSystem;

public record CroppedTexture(ResourceLocation location, float u, float v, float width, float height) implements IGuiTexture {

    public static CroppedTexture of(String location, float u, float v, float width, float height) {
        return new CroppedTexture(ResourceLocation.parse(location), u, v, width, height);
    }

    public static CroppedTexture of(SpriteTexture texture, float u, float v, float width, float height) {
        return new CroppedTexture(texture.getImageLocation(), u, v, width, height);
    }

    @Override
    public void draw(GuiGraphics graphics, float mouseX, float mouseY, float x, float y, float width, float height,
                     float partialTicks) {
        RenderSystem.setShaderTexture(0, location);
        graphics.blit(location, (int) x, (int) y, 0, u, v, (int) width, (int) height, 1, 1);
    }

    @Override
    public void draw(GUIContext context, float x, float y, float width, float height) {
        draw(context.graphics, context.mouseX, context.mouseY, x, y, width, height, context.partialTick);
    }
}
