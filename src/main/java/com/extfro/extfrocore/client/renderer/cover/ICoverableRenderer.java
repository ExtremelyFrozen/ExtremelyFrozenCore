package com.extfro.extfrocore.client.renderer.cover;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.capability.ICoverable;
import com.extfro.extfrocore.api.cover.CoverBehavior;
import com.extfro.extfrocore.api.machine.MetaMachine;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.data.ModelData;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public interface ICoverableRenderer {

    Direction[] DIRECTIONS = Direction.values();

    @OnlyIn(Dist.CLIENT)
    TextureAtlasSprite[] COVER_BACK_PLATE = new TextureAtlasSprite[1];

    @OnlyIn(Dist.CLIENT)
    static TextureAtlasSprite getCoverBackPlateSprite() {
        if (COVER_BACK_PLATE[0] == null) {
            COVER_BACK_PLATE[0] = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                    .apply(ExtForCore.id("block/material_sets/dull/wire_side"));
        }
        return COVER_BACK_PLATE[0];
    }

    @OnlyIn(Dist.CLIENT)
    static void onResourceManagerReload() {
        COVER_BACK_PLATE[0] = null;
    }

    @OnlyIn(Dist.CLIENT)
    default void renderCovers(java.util.List<BakedQuad> quads, @NotNull ICoverable coverable,
                              BlockPos pos, BlockAndTintGetter level, @Nullable Direction side,
                              RandomSource rand, @NotNull ModelData modelData, @Nullable RenderType renderType) {
        double thickness = coverable.getCoverPlateThickness();
        for (Direction face : DIRECTIONS) {
            CoverBehavior cover = coverable.getCoverAtSide(face);
            if (cover == null) {
                continue;
            }

            ICoverRenderer coverRenderer = cover.getCoverRenderer() == null ? null : cover.getCoverRenderer().get();
            if (coverRenderer == null) {
                continue;
            }

            if (thickness > 0 && cover.shouldRenderPlate() &&
                    coverRenderer.shouldRenderBackPlateForSide(cover, pos, level, side)) {
                double min = thickness + 0.01;
                double max = 0.99 - thickness;
                var normal = face.getNormal();
                var cube = new AABB(
                        normal.getX() > 0 ? max : 0.01,
                        normal.getY() > 0 ? max : 0.01,
                        normal.getZ() > 0 ? max : 0.01,
                        normal.getX() >= 0 ? 0.99 : min,
                        normal.getY() >= 0 ? 0.99 : min,
                        normal.getZ() >= 0 ? 0.99 : min);
                if (side == null) {
                    quads.add(StaticCoverFaceBakery.bakeFace(cube, face.getOpposite(), getCoverBackPlateSprite()));
                } else if (side != face.getOpposite()) {
                    quads.add(StaticCoverFaceBakery.bakeFace(cube, side, getCoverBackPlateSprite()));
                }
            }

            coverRenderer.renderCover(quads, side, rand, cover, pos, level, modelData, renderType);
        }
    }

    @OnlyIn(Dist.CLIENT)
    default void renderDynamicCovers(MetaMachine machine, float partialTick, PoseStack poseStack,
                                     MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ICoverable coverable = machine.getCoverContainer();
        for (Direction face : DIRECTIONS) {
            CoverBehavior cover = coverable.getCoverAtSide(face);
            IDynamicCoverRenderer renderer = cover != null ? cover.getDynamicRenderer().get() : null;
            if (renderer != null) {
                poseStack.pushPose();
                CoverRenderUtil.moveToFace(poseStack, 0.5f, 0.5f, 0.5f, face);
                CoverRenderUtil.rotateToFace(poseStack, face, Direction.NORTH);
                poseStack.translate(-0.5f, -0.5f, 0.01f);
                renderer.render(machine, face, partialTick, poseStack, buffer, packedLight, packedOverlay);
                poseStack.popPose();
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    default ChunkRenderTypeSet getCoverRenderTypes(ICoverable coverable, BlockPos pos, BlockAndTintGetter level,
                                                   RandomSource rand, ModelData modelData) {
        Set<ChunkRenderTypeSet> renderTypeSets = new HashSet<>();
        for (Direction side : DIRECTIONS) {
            CoverBehavior cover = coverable.getCoverAtSide(side);
            if (cover == null || cover.getCoverRenderer() == null) {
                continue;
            }
            ICoverRenderer renderer = cover.getCoverRenderer().get();
            if (renderer != null) {
                renderTypeSets.add(renderer.getRenderTypes(cover, pos, level, rand, modelData));
            }
        }
        return ChunkRenderTypeSet.union(renderTypeSets);
    }
}
