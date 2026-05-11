package com.extfro.extfrocore.client.model.machine;

import com.extfro.extfrocore.api.machine.MachineDefinition;
import com.extfro.extfrocore.api.machine.MachineRenderState;
import com.extfro.extfrocore.api.machine.MetaMachine;
import com.extfro.extfrocore.client.model.BaseBakedModel;
import com.extfro.extfrocore.client.model.EFModelProperties;
import com.extfro.extfrocore.client.model.IBlockEntityRendererBakedModel;
import com.extfro.extfrocore.client.model.machine.multipart.MultiPartBakedModel;
import com.extfro.extfrocore.client.renderer.cover.ICoverableRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class MachineModel extends BaseBakedModel implements ICoverableRenderer,
                                IBlockEntityRendererBakedModel<BlockEntity> {

    @Getter
    private final MachineDefinition definition;
    private final Map<MachineRenderState, BakedModel> modelsByState;
    private final @Nullable MultiPartBakedModel multiPart;
    private final ItemTransforms transforms;
    private final Transformation rootTransform;
    private final ModelState modelState;
    @Getter
    private final boolean isGui3d;
    @Accessors(fluent = true)
    @Getter
    private final boolean usesBlockLight;
    @Accessors(fluent = true)
    @Getter
    private final boolean useAmbientOcclusion;
    @Setter
    private TextureAtlasSprite particleIcon;

    public MachineModel(MachineDefinition definition, Map<MachineRenderState, BakedModel> modelsByState,
                        @Nullable MultiPartBakedModel multiPart,
                        ItemTransforms transforms, Transformation rootTransform, ModelState modelState,
                        boolean isGui3d, boolean usesBlockLight, boolean useAmbientOcclusion) {
        this.definition = definition;
        this.modelsByState = new IdentityHashMap<>(modelsByState);
        this.multiPart = multiPart;
        this.transforms = transforms;
        this.rootTransform = rootTransform;
        this.modelState = modelState;
        this.isGui3d = isGui3d;
        this.usesBlockLight = usesBlockLight;
        this.useAmbientOcclusion = useAmbientOcclusion;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        if (particleIcon != null) {
            return particleIcon;
        }
        BakedModel model = modelsByState.get(definition.defaultRenderState());
        if (model == multiPart && multiPart != null) {
            return multiPart.getParticleIcon();
        }
        if (model != null) {
            return model.getParticleIcon();
        }
        return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                .apply(MissingTextureAtlasSprite.getLocation());
    }

    @Override
    public TextureAtlasSprite getParticleIcon(ModelData modelData) {
        MachineRenderState renderState = getRenderState(modelData);
        if (multiPart != null) {
            return multiPart.getParticleIcon(renderState, modelData);
        }
        BakedModel model = modelsByState.get(renderState);
        return model == null ? getParticleIcon() : model.getParticleIcon(modelData);
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
        ModelData.Builder builder = modelData.derive()
                .with(EFModelProperties.LEVEL, level)
                .with(EFModelProperties.POS, pos);
        MetaMachine machine = MetaMachine.getMachine(level, pos);
        MachineRenderState renderState = machine == null ? definition.defaultRenderState() : machine.getRenderState();
        if (multiPart != null) {
            multiPart.addMachineModelData(renderState, level, pos, state, modelData, builder);
        }
        BakedModel model = modelsByState.get(renderState);
        if (model != null && model != multiPart) {
            ModelData data = model.getModelData(level, pos, state, modelData);
            for (ModelProperty<?> key : data.getProperties()) {
                copyModelData(builder, data, key);
            }
        }
        return builder.build();
    }

    private static <T> void copyModelData(ModelData.Builder builder, ModelData data, ModelProperty<T> key) {
        builder.with(key, data.get(key));
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand,
                                    ModelData modelData, @Nullable RenderType renderType) {
        List<BakedQuad> quads = new LinkedList<>();
        MachineRenderState renderState = getRenderState(modelData);
        if (multiPart != null) {
            quads.addAll(multiPart.getMachineQuads(definition, renderState, state, side, rand, modelData, renderType));
        }
        BakedModel model = modelsByState.get(renderState);
        if (model != null && model != multiPart) {
            quads.addAll(model.getQuads(state, side, rand, modelData, renderType));
        }
        BlockAndTintGetter level = modelData.get(EFModelProperties.LEVEL);
        BlockPos pos = modelData.get(EFModelProperties.POS);
        MetaMachine machine = level == null || pos == null ? null : MetaMachine.getMachine(level, pos);
        if (machine != null) {
            renderCovers(quads, machine.getCoverContainer(), pos, level, side, rand, modelData, renderType);
        }
        return quads;
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData modelData) {
        MachineRenderState renderState = getRenderState(modelData);
        BakedModel model = modelsByState.get(renderState);
        ChunkRenderTypeSet baseTypes;
        if (multiPart != null) {
            baseTypes = multiPart.getRenderTypes(state, rand, modelData);
        } else {
            baseTypes = model == null ? ChunkRenderTypeSet.none() :
                model.getRenderTypes(state, rand, modelData);
        }

        BlockAndTintGetter level = modelData.get(EFModelProperties.LEVEL);
        BlockPos pos = modelData.get(EFModelProperties.POS);
        MetaMachine machine = level == null || pos == null ? null : MetaMachine.getMachine(level, pos);
        if (machine == null) {
            return baseTypes;
        }
        return ChunkRenderTypeSet.union(baseTypes,
                getCoverRenderTypes(machine.getCoverContainer(), pos, level, rand, modelData));
    }

    private MachineRenderState getRenderState(ModelData modelData) {
        BlockAndTintGetter level = modelData.get(EFModelProperties.LEVEL);
        BlockPos pos = modelData.get(EFModelProperties.POS);
        MetaMachine machine = level == null || pos == null ? null : MetaMachine.getMachine(level, pos);
        return machine == null ? definition.defaultRenderState() : machine.getRenderState();
    }

    @Override
    public boolean isCustomRenderer() {
        return true;
    }

    @Override
    public void render(BlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        if (blockEntity instanceof MetaMachine machine && machine.getDefinition() == definition) {
            renderDynamicCovers(machine, partialTick, poseStack, buffer, packedLight, packedOverlay);
        }
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                             MultiBufferSource buffer, int packedLight, int packedOverlay) {
    }

    @Override
    public AABB getRenderBoundingBox(BlockEntity blockEntity) {
        return IBlockEntityRendererBakedModel.super.getRenderBoundingBox(blockEntity);
    }

    @Override
    public boolean shouldRenderOffScreen(BlockEntity blockEntity) {
        return false;
    }

    @Override
    public boolean shouldRender(BlockEntity blockEntity, Vec3 cameraPos) {
        return blockEntity instanceof MetaMachine machine && machine.getDefinition() == definition &&
                machine.getCoverContainer().hasDynamicCovers();
    }

    @Override
    public int getViewDistance() {
        return 64;
    }

    @Override
    public BlockEntityType<? extends BlockEntity> getBlockEntityType() {
        return definition.getBlockEntityType();
    }

    @Override
    public ItemTransforms getTransforms() {
        return transforms;
    }

    public Transformation getRootTransform() {
        return rootTransform;
    }

    public ModelState getModelState() {
        return modelState;
    }

    @Override
    public ItemOverrides getOverrides() {
        BakedModel model = modelsByState.get(definition.defaultRenderState());
        return model == null ? super.getOverrides() : model.getOverrides();
    }
}
