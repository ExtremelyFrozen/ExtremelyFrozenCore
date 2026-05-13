package com.extfro.extfrocore.client.util;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

import org.joml.Vector3f;

public class StaticFaceBakery {

    private static final FaceBakery FACE_BAKERY = new FaceBakery();

    public static final AABB SLIGHTLY_OVER_BLOCK = new AABB(-0.001f, -0.001f, -0.001f,
            1.001f, 1.001f, 1.001f);
    public static final AABB OUTPUT_OVERLAY = new AABB(-.006f, -.006f, -.006f,
            1.006f, 1.006f, 1.006f);
    public static final AABB AUTO_OUTPUT_OVERLAY = new AABB(-.008f, -.008f, -.008f,
            1.008f, 1.008f, 1.008f);
    public static final AABB COVER_OVERLAY = new AABB(-.008f, -.008f, -.008f,
            1.008f, 1.008f, 1.008f);

    public static BakedQuad bakeFace(AABB cube, Direction face, TextureAtlasSprite sprite) {
        return bakeFace(cube, face, sprite, BlockModelRotation.X0_Y0, -1, true, true);
    }

    public static BakedQuad bakeFace(AABB cube, Direction face, TextureAtlasSprite sprite,
                                     int tintIndex, boolean shade) {
        return bakeFace(cube, face, sprite, BlockModelRotation.X0_Y0, tintIndex, true, shade);
    }

    public static BakedQuad bakeFace(AABB cube, Direction face, TextureAtlasSprite sprite,
                                     BlockModelRotation rotation, int tintIndex,
                                     boolean uvLocked, boolean shade) {
        float[] uvs = getUVs(cube, face);
        BlockElementFace elementFace = new BlockElementFace(face, tintIndex, "", new BlockFaceUV(uvs, 0));
        return FACE_BAKERY.bakeQuad(
                new Vector3f((float) cube.minX * 16, (float) cube.minY * 16, (float) cube.minZ * 16),
                new Vector3f((float) cube.maxX * 16, (float) cube.maxY * 16, (float) cube.maxZ * 16),
                elementFace, sprite, face, rotation, null, shade);
    }

    private static float[] getUVs(AABB cube, Direction face) {
        return switch (face) {
            case DOWN -> new float[] { (float) cube.minX * 16, 16 - (float) cube.maxZ * 16,
                    (float) cube.maxX * 16, 16 - (float) cube.minZ * 16 };
            case UP -> new float[] { (float) cube.minX * 16, (float) cube.minZ * 16,
                    (float) cube.maxX * 16, (float) cube.maxZ * 16 };
            case NORTH -> new float[] { 16 - (float) cube.maxX * 16, 16 - (float) cube.maxY * 16,
                    16 - (float) cube.minX * 16, 16 - (float) cube.minY * 16 };
            case SOUTH -> new float[] { (float) cube.minX * 16, 16 - (float) cube.maxY * 16,
                    (float) cube.maxX * 16, 16 - (float) cube.minY * 16 };
            case WEST -> new float[] { (float) cube.minZ * 16, 16 - (float) cube.maxY * 16,
                    (float) cube.maxZ * 16, 16 - (float) cube.minY * 16 };
            case EAST -> new float[] { 16 - (float) cube.maxZ * 16, 16 - (float) cube.maxY * 16,
                    16 - (float) cube.minZ * 16, 16 - (float) cube.minY * 16 };
        };
    }
}
