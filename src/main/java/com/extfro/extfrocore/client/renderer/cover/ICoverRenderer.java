package com.extfro.extfrocore.client.renderer.cover;

import com.extfro.extfrocore.api.cover.CoverBehavior;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.data.ModelData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ICoverRenderer {

    @OnlyIn(Dist.CLIENT)
    default boolean shouldRenderBackPlateForSide(@NotNull CoverBehavior coverBehavior, BlockPos pos,
                                                 BlockAndTintGetter level, @Nullable Direction side) {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    default ChunkRenderTypeSet getRenderTypes(@NotNull CoverBehavior coverBehavior, BlockPos pos,
                                              BlockAndTintGetter level, RandomSource rand,
                                              @NotNull ModelData modelData) {
        return ChunkRenderTypeSet.of(RenderType.solid());
    }

    @OnlyIn(Dist.CLIENT)
    void renderCover(List<BakedQuad> quads, @Nullable Direction side, RandomSource rand,
                     @NotNull CoverBehavior coverBehavior, BlockPos pos, BlockAndTintGetter level,
                     @NotNull ModelData modelData, @Nullable RenderType renderType);
}
