package com.extfro.extfrocore.client.renderer.cover;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.capability.recipe.IO;
import com.extfro.extfrocore.api.cover.CoverBehavior;
import com.extfro.extfrocore.api.cover.IIOCover;
import com.extfro.extfrocore.client.util.ModelUtils;
import com.extfro.extfrocore.client.util.StaticFaceBakery;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockAndTintGetter;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;

import com.lowdragmc.lowdraglib2.client.bakedpipeline.FaceQuad;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class IOCoverRenderer implements ICoverRenderer {

    public static final IOCoverRenderer PUMP_LIKE_COVER_RENDERER = new IOCoverRenderer(
            ExtForCore.id("block/cover/pump"),
            ExtForCore.id("block/cover/pump_inverted"),
            null, null);

    @OnlyIn(Dist.CLIENT)
    protected TextureAtlasSprite overlaySprite = null;
    @OnlyIn(Dist.CLIENT)
    protected TextureAtlasSprite invertedOverlaySprite = null;
    @OnlyIn(Dist.CLIENT)
    protected TextureAtlasSprite emissiveOverlaySprite = null;
    @OnlyIn(Dist.CLIENT)
    protected TextureAtlasSprite invertedEmissiveOverlaySprite = null;

    public IOCoverRenderer(@Nullable ResourceLocation overlay,
                           @Nullable ResourceLocation invertedOverlay,
                           @Nullable ResourceLocation emissiveOverlay,
                           @Nullable ResourceLocation invertedEmissiveOverlay) {
        ModelUtils.registerAtlasStitchedEventListener(false, InventoryMenu.BLOCK_ATLAS, event -> {
            var atlas = event.getAtlas();

            if (overlay != null) {
                overlaySprite = atlas.getSprite(overlay);
            }
            if (invertedOverlay != null) {
                invertedOverlaySprite = atlas.getSprite(invertedOverlay);
            }
            if (emissiveOverlay != null) {
                emissiveOverlaySprite = atlas.getSprite(emissiveOverlay);
            }
            if (invertedEmissiveOverlay != null) {
                invertedEmissiveOverlaySprite = atlas.getSprite(invertedEmissiveOverlay);
            }
        });
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderCover(List<BakedQuad> quads, @Nullable Direction side, RandomSource rand,
                            @NotNull CoverBehavior coverBehavior, BlockPos pos, BlockAndTintGetter level,
                            @NotNull ModelData modelData, @Nullable RenderType renderType) {
        if ((side == null || side == coverBehavior.attachedSide) && coverBehavior instanceof IIOCover ioCover) {
            boolean isInverted = ioCover.getIo() != IO.OUT;

            if (isInverted && invertedOverlaySprite != null) {
                quads.add(StaticFaceBakery.bakeFace(StaticFaceBakery.COVER_OVERLAY, coverBehavior.attachedSide,
                        invertedOverlaySprite));
            } else if (overlaySprite != null) {
                quads.add(StaticFaceBakery.bakeFace(StaticFaceBakery.COVER_OVERLAY, coverBehavior.attachedSide,
                        overlaySprite));
            }
            if (isInverted && invertedEmissiveOverlaySprite != null) {
                quads.add(FaceQuad.bakeFace(StaticFaceBakery.COVER_OVERLAY, coverBehavior.attachedSide,
                        invertedEmissiveOverlaySprite, BlockModelRotation.X0_Y0, -101, 15, true, false));
            } else if (emissiveOverlaySprite != null) {
                quads.add(FaceQuad.bakeFace(StaticFaceBakery.COVER_OVERLAY, coverBehavior.attachedSide,
                        emissiveOverlaySprite, BlockModelRotation.X0_Y0, -101, 15, true, false));
            }
        }
    }
}
