package com.extfro.extfrocore.api.capability;

import com.extfro.extfrocore.ExtForCore;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;

import org.jetbrains.annotations.Nullable;

public final class EFBlockCapabilities {

    public static final BlockCapability<ICoverable, @Nullable Direction> COVERABLE =
            BlockCapability.createSided(ExtForCore.id("coverable"), ICoverable.class);

    private EFBlockCapabilities() {}
}
