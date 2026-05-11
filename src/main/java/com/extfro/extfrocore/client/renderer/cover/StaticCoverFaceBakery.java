package com.extfro.extfrocore.client.renderer.cover;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

import org.joml.Vector3f;

public final class StaticCoverFaceBakery {

    public static final AABB COVER_OVERLAY = new AABB(-0.008f, -0.008f, -0.008f,
            1.008f, 1.008f, 1.008f);

    private static final FaceBakery FACE_BAKERY = new FaceBakery();

    private StaticCoverFaceBakery() {}

    public static BakedQuad bakeFace(AABB cube, Direction face, TextureAtlasSprite sprite) {
        return bakeFace(cube, face, sprite, -1, true, true);
    }

    public static BakedQuad bakeFace(AABB cube, Direction face, TextureAtlasSprite sprite, int tintIndex,
                                     boolean shade, boolean ambientOcclusion) {
        Vector3f from = new Vector3f((float) cube.minX * 16, (float) cube.minY * 16, (float) cube.minZ * 16);
        Vector3f to = new Vector3f((float) cube.maxX * 16, (float) cube.maxY * 16, (float) cube.maxZ * 16);
        BlockElementFace elementFace = new BlockElementFace(null, tintIndex, "", new BlockFaceUV(uv(face, cube), 0));
        BakedQuad quad = FACE_BAKERY.bakeQuad(from, to, elementFace, sprite, face, BlockModelRotation.X0_Y0, null,
                shade);
        return ambientOcclusion == quad.hasAmbientOcclusion() ? quad :
                new BakedQuad(quad.getVertices(), quad.getTintIndex(), quad.getDirection(), quad.getSprite(),
                        quad.isShade(), ambientOcclusion);
    }

    private static float[] uv(Direction face, AABB cube) {
        return switch (face) {
            case DOWN, UP -> new float[] {
                    (float) cube.minX * 16, (float) cube.minZ * 16,
                    (float) cube.maxX * 16, (float) cube.maxZ * 16
            };
            case NORTH, SOUTH -> new float[] {
                    (float) cube.minX * 16, (float) cube.minY * 16,
                    (float) cube.maxX * 16, (float) cube.maxY * 16
            };
            case WEST, EAST -> new float[] {
                    (float) cube.minZ * 16, (float) cube.minY * 16,
                    (float) cube.maxZ * 16, (float) cube.maxY * 16
            };
        };
    }
}
