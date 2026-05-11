package com.extfro.extfrocore.client.renderer;

import com.extfro.extfrocore.client.model.IBlockEntityRendererBakedModel;
import com.extfro.extfrocore.api.machine.MetaMachine;
import com.extfro.extfrocore.client.renderer.cover.ICoverableRenderer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

@SuppressWarnings("unchecked")
public class BlockEntityWithBERModelRenderer<T extends BlockEntity> implements BlockEntityRenderer<T>, ICoverableRenderer {

    private final BlockRenderDispatcher blockRenderDispatcher;

    public BlockEntityWithBERModelRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderDispatcher = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(T blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        BlockState blockState = blockEntity.getBlockState();
        BakedModel model = blockRenderDispatcher.getBlockModel(blockState);

        if (model instanceof IBlockEntityRendererBakedModel<?> berModel) {
            if (berModel.getBlockEntityType() != null && berModel.getBlockEntityType() != blockEntity.getType()) {
                return;
            }
            ((IBlockEntityRendererBakedModel<T>) berModel).render(blockEntity, partialTick, poseStack, buffer,
                    packedLight, packedOverlay);
            return;
        }

        Level level = blockEntity.getLevel();
        if (level == null) {
            return;
        }
        BlockPos pos = blockEntity.getBlockPos();
        ModelData modelData = level.getModelData(pos);
        long seed = blockState.getSeed(pos);
        RandomSource random = RandomSource.create();
        random.setSeed(seed);

        for (RenderType renderType : model.getRenderTypes(blockState, random, modelData)) {
            VertexConsumer consumer = buffer.getBuffer(renderType);
            blockRenderDispatcher.getModelRenderer()
                    .tesselateBlock(level, model, blockState, pos, poseStack, consumer, true, random, seed,
                            OverlayTexture.NO_OVERLAY, modelData, renderType);
        }
        if (blockEntity instanceof MetaMachine machine && machine.getCoverContainer().hasDynamicCovers()) {
            renderDynamicCovers(machine, partialTick, poseStack, buffer, packedLight, packedOverlay);
        }
    }

    @Override
    public boolean shouldRenderOffScreen(T blockEntity) {
        BlockState blockState = blockEntity.getBlockState();
        BakedModel model = blockRenderDispatcher.getBlockModel(blockState);
        if (model instanceof IBlockEntityRendererBakedModel<?> berModel &&
                berModel.getBlockEntityType() != null && berModel.getBlockEntityType() == blockEntity.getType()) {
            return ((IBlockEntityRendererBakedModel<T>) berModel).shouldRenderOffScreen(blockEntity);
        }
        return BlockEntityRenderer.super.shouldRenderOffScreen(blockEntity);
    }

    @Override
    public boolean shouldRender(T blockEntity, Vec3 cameraPos) {
        BlockState blockState = blockEntity.getBlockState();
        BakedModel model = blockRenderDispatcher.getBlockModel(blockState);
        if (model instanceof IBlockEntityRendererBakedModel<?> berModel &&
                berModel.getBlockEntityType() != null && berModel.getBlockEntityType() == blockEntity.getType()) {
            return ((IBlockEntityRendererBakedModel<T>) berModel).shouldRender(blockEntity, cameraPos);
        }
        return BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos);
    }
}
