package com.extfro.extfrocore.client.util;

import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.util.Mth;

import com.google.common.base.Preconditions;
import com.mojang.math.Transformation;
import org.joml.Quaternionf;

public final class VariantRotationHelpers {

    private static final Transformation[] TRANSFORMS = createTransformations();

    private VariantRotationHelpers() {}

    private static Transformation[] createTransformations() {
        Transformation[] result = new Transformation[4 * 4 * 4];
        for (int xRot = 0; xRot < 360; xRot += 90) {
            for (int yRot = 0; yRot < 360; yRot += 90) {
                result[indexFromAngles(xRot, yRot, 0)] = BlockModelRotation.by(xRot, yRot).getRotation();
                for (int zRot = 90; zRot < 360; zRot += 90) {
                    int index = indexFromAngles(xRot, yRot, zRot);
                    Quaternionf quaternion = new Quaternionf().rotateYXZ(
                            -yRot * Mth.DEG_TO_RAD,
                            -xRot * Mth.DEG_TO_RAD,
                            -zRot * Mth.DEG_TO_RAD);
                    result[index] = new Transformation(null, quaternion, null, null);
                }
            }
        }
        return result;
    }

    public static Transformation getRotationTransform(int xRot, int yRot, int zRot) {
        return TRANSFORMS[indexFromAngles(xRot, yRot, zRot)];
    }

    private static int indexFromAngles(int xRot, int yRot, int zRot) {
        Preconditions.checkArgument(xRot >= 0 && xRot < 360 && xRot % 90 == 0);
        Preconditions.checkArgument(yRot >= 0 && yRot < 360 && yRot % 90 == 0);
        Preconditions.checkArgument(zRot >= 0 && zRot < 360 && zRot % 90 == 0);
        return xRot / 90 * 16 + yRot / 90 * 4 + zRot / 90;
    }
}
