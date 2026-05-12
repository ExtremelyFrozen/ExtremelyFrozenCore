package com.extfro.extfrocore.client.model.machine;

import com.extfro.extfrocore.api.machine.feature.multiblock.IMultiPart;
import com.extfro.extfrocore.api.machine.multiblock.MultiblockControllerMachine;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IControllerModelRenderer {

    /**
     * Render a specific model for given part.
     */
    @OnlyIn(Dist.CLIENT)
    void renderPartModel(List<BakedQuad> quads, MultiblockControllerMachine machine, IMultiPart part,
                         Direction frontFacing, @Nullable Direction side, RandomSource rand,
                         @NotNull ModelData modelData, @Nullable RenderType renderType);
}
