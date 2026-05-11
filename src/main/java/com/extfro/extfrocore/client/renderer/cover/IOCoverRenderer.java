package com.extfro.extfrocore.client.renderer.cover;

import com.extfro.extfrocore.api.capability.recipe.IO;
import com.extfro.extfrocore.api.cover.CoverBehavior;
import com.extfro.extfrocore.api.cover.IIOCover;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockAndTintGetter;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class IOCoverRenderer implements ICoverRenderer {

    private final @Nullable ResourceLocation overlay;
    private final @Nullable ResourceLocation invertedOverlay;
    private final @Nullable ResourceLocation emissiveOverlay;
    private final @Nullable ResourceLocation invertedEmissiveOverlay;

    @OnlyIn(Dist.CLIENT)
    protected TextureAtlasSprite overlaySprite;
    @OnlyIn(Dist.CLIENT)
    protected TextureAtlasSprite invertedOverlaySprite;
    @OnlyIn(Dist.CLIENT)
    protected TextureAtlasSprite emissiveOverlaySprite;
    @OnlyIn(Dist.CLIENT)
    protected TextureAtlasSprite invertedEmissiveOverlaySprite;

    public IOCoverRenderer(@Nullable ResourceLocation overlay,
                           @Nullable ResourceLocation invertedOverlay,
                           @Nullable ResourceLocation emissiveOverlay,
                           @Nullable ResourceLocation invertedEmissiveOverlay) {
        this.overlay = overlay;
        this.invertedOverlay = invertedOverlay;
        this.emissiveOverlay = emissiveOverlay;
        this.invertedEmissiveOverlay = invertedEmissiveOverlay;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onResourceManagerReload() {
        overlaySprite = null;
        invertedOverlaySprite = null;
        emissiveOverlaySprite = null;
        invertedEmissiveOverlaySprite = null;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderCover(List<BakedQuad> quads, @Nullable Direction side, RandomSource rand,
                            @NotNull CoverBehavior coverBehavior, BlockPos pos, BlockAndTintGetter level,
                            @NotNull ModelData modelData, @Nullable RenderType renderType) {
        if ((side == null || side == coverBehavior.attachedSide) && coverBehavior instanceof IIOCover ioCover) {
            boolean isInverted = ioCover.getIo() != IO.OUT;
            TextureAtlasSprite mainSprite = isInverted && getInvertedOverlaySprite() != null ?
                    getInvertedOverlaySprite() : getOverlaySprite();
            if (mainSprite != null) {
                quads.add(StaticCoverFaceBakery.bakeFace(StaticCoverFaceBakery.COVER_OVERLAY,
                        coverBehavior.attachedSide, mainSprite));
            }

            TextureAtlasSprite emissiveSprite = isInverted && getInvertedEmissiveOverlaySprite() != null ?
                    getInvertedEmissiveOverlaySprite() : getEmissiveOverlaySprite();
            if (emissiveSprite != null) {
                quads.add(StaticCoverFaceBakery.bakeFace(StaticCoverFaceBakery.COVER_OVERLAY,
                        coverBehavior.attachedSide, emissiveSprite, -101, true, false));
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected @Nullable TextureAtlasSprite getOverlaySprite() {
        if (overlay == null) {
            return null;
        }
        if (overlaySprite == null) {
            overlaySprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(overlay);
        }
        return overlaySprite;
    }

    @OnlyIn(Dist.CLIENT)
    protected @Nullable TextureAtlasSprite getInvertedOverlaySprite() {
        if (invertedOverlay == null) {
            return null;
        }
        if (invertedOverlaySprite == null) {
            invertedOverlaySprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                    .apply(invertedOverlay);
        }
        return invertedOverlaySprite;
    }

    @OnlyIn(Dist.CLIENT)
    protected @Nullable TextureAtlasSprite getEmissiveOverlaySprite() {
        if (emissiveOverlay == null) {
            return null;
        }
        if (emissiveOverlaySprite == null) {
            emissiveOverlaySprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                    .apply(emissiveOverlay);
        }
        return emissiveOverlaySprite;
    }

    @OnlyIn(Dist.CLIENT)
    protected @Nullable TextureAtlasSprite getInvertedEmissiveOverlaySprite() {
        if (invertedEmissiveOverlay == null) {
            return null;
        }
        if (invertedEmissiveOverlaySprite == null) {
            invertedEmissiveOverlaySprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                    .apply(invertedEmissiveOverlay);
        }
        return invertedEmissiveOverlaySprite;
    }
}
