package com.extfro.extfrocore.common.pipelike.laser;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.pipenet.IPipeType;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum LaserPipeType implements IPipeType<LaserPipeProperties>, StringRepresentable {

    NORMAL;

    public static final ResourceLocation TYPE_ID = ExtForCore.id("laser");

    @Override
    public float getThickness() {
        return 0.375f;
    }

    @Override
    public LaserPipeProperties modifyProperties(LaserPipeProperties baseProperties) {
        return baseProperties;
    }

    @Override
    public boolean isPaintable() {
        return true;
    }

    @Override
    public ResourceLocation type() {
        return TYPE_ID;
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
