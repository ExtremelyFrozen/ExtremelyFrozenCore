package com.extfro.extfrocore.client.renderer.cover;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Quaternionf;

public final class CoverRenderUtil {

    private CoverRenderUtil() {}

    public static void moveToFace(PoseStack poseStack, float x, float y, float z, Direction face) {
        poseStack.translate(Math.fma(face.getStepX(), 0.5f, x),
                Math.fma(face.getStepY(), 0.5f, y),
                Math.fma(face.getStepZ(), 0.5f, z));
    }

    public static void rotateToFace(PoseStack poseStack, Direction face, Direction spin) {
        float rotationAngle = Mth.HALF_PI * switch (face) {
            case UP, WEST -> 1;
            case DOWN, EAST -> -1;
            case SOUTH -> 2;
            case NORTH -> 0;
        };
        Quaternionf rotation = new Quaternionf();
        if (face.getAxis() == Direction.Axis.Y) {
            poseStack.scale(1.0f, -1.0f, 1.0f);
            rotation.rotateX(rotationAngle);
        } else {
            poseStack.scale(-1.0f, -1.0f, -1.0f);
            rotation.rotateY(rotationAngle);
        }
        rotation.rotateZ(getSpinAngle(spin, face));

        poseStack.mulPose(rotation);
    }

    private static float getSpinAngle(Direction spin, Direction face) {
        if (spin.getAxis() == Direction.Axis.Z && face == Direction.DOWN) {
            spin = spin.getOpposite();
        }
        return switch (spin) {
            case DOWN, NORTH -> 0;
            case UP, SOUTH -> Mth.PI;
            case WEST -> Mth.HALF_PI;
            case EAST -> -Mth.HALF_PI;
        };
    }
}
