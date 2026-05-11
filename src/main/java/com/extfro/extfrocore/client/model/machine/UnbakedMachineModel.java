package com.extfro.extfrocore.client.model.machine;

import com.extfro.extfrocore.api.machine.MachineDefinition;
import com.extfro.extfrocore.api.machine.MachineRenderState;
import com.extfro.extfrocore.client.model.machine.multipart.MultiPartBakedModel;
import com.extfro.extfrocore.client.model.machine.multipart.MultiPartUnbakedModel;
import com.extfro.extfrocore.client.renderer.machine.DynamicMachineRender;

import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class UnbakedMachineModel implements IUnbakedGeometry<UnbakedMachineModel> {

    private final MachineDefinition definition;
    private final Map<MachineRenderState, UnbakedModel> models;
    private final @Nullable MultiPartUnbakedModel multiPart;
    private final List<DynamicMachineRender<?, ?>> dynamicRenders;
    private final @Nullable ResourceLocation particle;

    public UnbakedMachineModel(MachineDefinition definition, Map<MachineRenderState, UnbakedModel> models,
                               @Nullable MultiPartUnbakedModel multiPart,
                               List<DynamicMachineRender<?, ?>> dynamicRenders,
                               @Nullable ResourceLocation particle) {
        this.definition = definition;
        this.models = models;
        this.multiPart = multiPart;
        this.dynamicRenders = dynamicRenders;
        this.particle = particle;
    }

    public MachineDefinition getDefinition() {
        return definition;
    }

    public Map<MachineRenderState, UnbakedModel> getModels() {
        return models;
    }

    @Nullable
    public MultiPartUnbakedModel getMultiPart() {
        return multiPart;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
                           Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState,
                           ItemOverrides overrides) {
        Map<MachineRenderState, BakedModel> bakedModels = new IdentityHashMap<>();
        models.forEach((machineState, unbaked) -> bakedModels.put(machineState,
                unbaked.bake(baker, spriteGetter, modelState)));
        MultiPartBakedModel bakedMultiPart = multiPart == null ? null :
                multiPart.bake(baker, spriteGetter, modelState);

        MachineModel model = new MachineModel(definition, bakedModels, bakedMultiPart, dynamicRenders,
                context.getTransforms(), context.getRootTransform(), modelState, context.isGui3d(),
                context.useBlockLight(), context.useAmbientOcclusion());
        if (particle != null) {
            model.setParticleIcon(spriteGetter.apply(new Material(TextureAtlas.LOCATION_BLOCKS, particle)));
        } else if (context.hasMaterial("particle")) {
            model.setParticleIcon(spriteGetter.apply(context.getMaterial("particle")));
        }
        return model;
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> resolver, IGeometryBakingContext context) {
        MachineModelLoader.resolveStateModels(this, resolver);
    }
}
