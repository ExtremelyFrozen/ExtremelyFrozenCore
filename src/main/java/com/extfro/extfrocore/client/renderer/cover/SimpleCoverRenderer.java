package com.extfro.extfrocore.client.renderer.cover;

import com.extfro.extfrocore.api.cover.CoverBehavior;

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

public class SimpleCoverRenderer implements ICoverRenderer {

    private final ResourceLocation texture;
    private final @Nullable ResourceLocation emissiveTexture;

    @OnlyIn(Dist.CLIENT)
    protected TextureAtlasSprite sprite;
    @OnlyIn(Dist.CLIENT)
    protected TextureAtlasSprite emissiveSprite;

    public SimpleCoverRenderer(ResourceLocation texture) {
        this(texture, null);
    }

    public SimpleCoverRenderer(ResourceLocation texture, @Nullable ResourceLocation emissiveTexture) {
        this.texture = texture;
        this.emissiveTexture = emissiveTexture;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderCover(List<BakedQuad> quads, @Nullable Direction side, RandomSource rand,
                            @NotNull CoverBehavior coverBehavior, BlockPos pos, BlockAndTintGetter level,
                            @NotNull ModelData modelData, @Nullable RenderType renderType) {
        if (side == null || side == coverBehavior.attachedSide) {
            TextureAtlasSprite overlay = getSprite();
            if (overlay != null) {
                quads.add(StaticCoverFaceBakery.bakeFace(StaticCoverFaceBakery.COVER_OVERLAY,
                        coverBehavior.attachedSide, overlay));
            }
            TextureAtlasSprite emissiveOverlay = getEmissiveSprite();
            if (emissiveOverlay != null) {
                quads.add(StaticCoverFaceBakery.bakeFace(StaticCoverFaceBakery.COVER_OVERLAY,
                        coverBehavior.attachedSide, emissiveOverlay));
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected @Nullable TextureAtlasSprite getSprite() {
        if (sprite == null) {
            sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(texture);
        }
        return sprite;
    }

    @OnlyIn(Dist.CLIENT)
    protected @Nullable TextureAtlasSprite getEmissiveSprite() {
        if (emissiveTexture == null) {
            return null;
        }
        if (emissiveSprite == null) {
            emissiveSprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(emissiveTexture);
        }
        return emissiveSprite;
    }
}
