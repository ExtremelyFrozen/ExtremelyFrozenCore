package com.extfro.extfrocore.client.model.machine.multipart;

import com.extfro.extfrocore.api.machine.MachineDefinition;
import com.extfro.extfrocore.api.machine.MachineRenderState;
import com.extfro.extfrocore.api.machine.MetaMachine;
import com.extfro.extfrocore.client.model.EFModelProperties;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import net.neoforged.neoforge.common.util.TriState;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class MultiPartBakedModel implements IDynamicBakedModel {

    private static final ModelProperty<Map<BakedModel, ModelData>> MULTI_PART_DATA_PROPERTY = new ModelProperty<>();

    private final List<Pair<Predicate<MachineRenderState>, BakedModel>> selectors;
    protected final boolean hasAmbientOcclusion;
    @Getter
    protected final boolean isGui3d;
    @Accessors(fluent = true)
    @Getter
    protected final boolean usesBlockLight;
    @Getter
    protected final TextureAtlasSprite particleIcon;
    @Getter
    protected final ItemTransforms transforms;
    @Getter
    protected final ItemOverrides overrides;
    private final Map<MachineRenderState, BitSet> selectorCache = new IdentityHashMap<>();
    private final BakedModel defaultModel;

    public MultiPartBakedModel(List<Pair<Predicate<MachineRenderState>, BakedModel>> selectors) {
        this.selectors = selectors;
        BakedModel defaultModel = selectors.getFirst().getRight();
        this.defaultModel = defaultModel;
        this.hasAmbientOcclusion = defaultModel.useAmbientOcclusion();
        this.isGui3d = defaultModel.isGui3d();
        this.usesBlockLight = defaultModel.usesBlockLight();
        this.particleIcon = defaultModel.getParticleIcon();
        this.transforms = defaultModel.getTransforms();
        this.overrides = defaultModel.getOverrides();
    }

    public BitSet getSelectors(@Nullable MachineRenderState state) {
        BitSet bitSet = selectorCache.get(state);
        if (bitSet == null) {
            bitSet = new BitSet();
            for (int i = 0; i < selectors.size(); ++i) {
                Pair<Predicate<MachineRenderState>, BakedModel> pair = selectors.get(i);
                if (pair.getLeft().test(state)) {
                    bitSet.set(i);
                }
            }
            selectorCache.put(state, bitSet);
        }
        return bitSet;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand,
                                    ModelData modelData, @Nullable RenderType renderType) {
        return defaultModel.getQuads(state, side, rand, modelData, renderType);
    }

    public List<BakedQuad> getMachineQuads(MachineDefinition definition, MachineRenderState renderState,
                                           @Nullable BlockState blockState, @Nullable Direction direction,
                                           RandomSource random, ModelData modelData, @Nullable RenderType renderType) {
        if (blockState == null) {
            blockState = definition.defaultBlockState();
        }
        BitSet bitSet = getSelectors(renderState);
        List<BakedQuad> quads = new LinkedList<>();
        long seed = random.nextLong();

        for (int i = 0; i < bitSet.length(); ++i) {
            if (bitSet.get(i)) {
                BakedModel model = selectors.get(i).getRight();
                ModelData partData = resolveMultipartData(modelData, model);
                if (renderType == null || model.getRenderTypes(blockState, random, partData).contains(renderType)) {
                    quads.addAll(model.getQuads(blockState, direction, RandomSource.create(seed), partData,
                            renderType));
                }
            }
        }
        return quads;
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData modelData) {
        BlockAndTintGetter level = modelData.get(EFModelProperties.LEVEL);
        BlockPos pos = modelData.get(EFModelProperties.POS);
        MetaMachine machine = level == null || pos == null ? null : MetaMachine.getMachine(level, pos);
        if (machine == null) {
            return defaultModel.getRenderTypes(state, rand, modelData);
        }

        List<ChunkRenderTypeSet> renderTypeSets = new LinkedList<>();
        BitSet selected = getSelectors(machine.getRenderState());
        for (int i = 0; i < selected.length(); i++) {
            if (selected.get(i)) {
                BakedModel model = selectors.get(i).getRight();
                ModelData partData = resolveMultipartData(modelData, model);
                renderTypeSets.add(model.getRenderTypes(state, rand, partData));
            }
        }
        return ChunkRenderTypeSet.union(renderTypeSets);
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
        ModelData.Builder builder = modelData.derive()
                .with(EFModelProperties.LEVEL, level)
                .with(EFModelProperties.POS, pos);
        MetaMachine machine = MetaMachine.getMachine(level, pos);
        if (machine == null) {
            return builder.build();
        }
        addMachineModelData(machine.getRenderState(), level, pos, state, modelData, builder);
        return builder.build();
    }

    public void addMachineModelData(MachineRenderState renderState, BlockAndTintGetter level, BlockPos pos,
                                    BlockState state, ModelData baseData, ModelData.Builder builder) {
        Map<BakedModel, ModelData> dataMap = null;
        BitSet selected = getSelectors(renderState);
        for (int i = 0; i < selected.length(); ++i) {
            if (selected.get(i)) {
                BakedModel model = selectors.get(i).getRight();
                ModelData data = model.getModelData(level, pos, state, baseData);
                if (data != baseData) {
                    if (dataMap == null) {
                        dataMap = new IdentityHashMap<>();
                    }
                    dataMap.put(model, data);
                }
            }
        }
        if (dataMap != null) {
            builder.with(MULTI_PART_DATA_PROPERTY, dataMap);
        }
    }

    @Override
    public boolean useAmbientOcclusion() {
        return hasAmbientOcclusion;
    }

    @Override
    public TriState useAmbientOcclusion(BlockState state, ModelData data, RenderType renderType) {
        return defaultModel.useAmbientOcclusion(state, data, renderType);
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon(ModelData modelData) {
        BlockAndTintGetter level = modelData.get(EFModelProperties.LEVEL);
        BlockPos pos = modelData.get(EFModelProperties.POS);
        MetaMachine machine = level == null || pos == null ? null : MetaMachine.getMachine(level, pos);
        return machine != null ? getParticleIcon(machine.getRenderState(), modelData) :
                defaultModel.getParticleIcon(modelData);
    }

    public TextureAtlasSprite getParticleIcon(@NotNull MachineRenderState renderState, ModelData modelData) {
        BitSet selected = getSelectors(renderState);
        for (int i = 0; i < selected.length(); i++) {
            if (selected.get(i)) {
                BakedModel model = selectors.get(i).getRight();
                return model.getParticleIcon(resolveMultipartData(modelData, model));
            }
        }
        return defaultModel.getParticleIcon(modelData);
    }

    @Override
    public BakedModel applyTransform(ItemDisplayContext transformType, PoseStack poseStack,
                                     boolean applyLeftHandTransform) {
        return defaultModel.applyTransform(transformType, poseStack, applyLeftHandTransform);
    }

    public static ModelData resolveMultipartData(ModelData modelData, BakedModel model) {
        Map<BakedModel, ModelData> multipartData = modelData.get(MULTI_PART_DATA_PROPERTY);
        if (multipartData == null) {
            return modelData;
        }
        ModelData partData = multipartData.get(model);
        return partData != null ? partData : modelData;
    }

    public static class Builder {

        private final List<Pair<Predicate<MachineRenderState>, BakedModel>> selectors = new ArrayList<>();

        public void add(Predicate<MachineRenderState> predicate, BakedModel model) {
            selectors.add(Pair.of(predicate, model));
        }

        public MultiPartBakedModel build() {
            return new MultiPartBakedModel(selectors);
        }
    }
}
