package com.extfro.extfrocore.client.renderer.block;

import com.extfro.extfrocore.api.material.info.EFMaterialIconSet;
import com.extfro.extfrocore.api.material.info.EFMaterialIconType;
import com.extfro.extfrocore.data.pack.EFDynamicResourcePack;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.data.models.model.DelegatedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.HashSet;
import java.util.Set;

public class EFMaterialBlockRenderer {

    private static final Set<EFMaterialBlockRenderer> MODELS = new HashSet<>();

    public static void create(Block block, EFMaterialIconType type, EFMaterialIconSet iconSet) {
        MODELS.add(new EFMaterialBlockRenderer(block, type, iconSet));
    }

    public static void reinitModels() {
        for (EFMaterialBlockRenderer model : MODELS) {
            ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(model.block);
            ResourceLocation modelId = model.type.getBlockModelPath(model.iconSet, true);

            EFDynamicResourcePack.addBlockState(blockId, MultiVariantGenerator.multiVariant(model.block,
                    Variant.variant().with(VariantProperties.MODEL, modelId)));
            EFDynamicResourcePack.addItemModel(BuiltInRegistries.ITEM.getKey(model.block.asItem()),
                    new DelegatedModel(modelId));
        }
    }

    private final Block block;
    private final EFMaterialIconType type;
    private final EFMaterialIconSet iconSet;

    private EFMaterialBlockRenderer(Block block, EFMaterialIconType type, EFMaterialIconSet iconSet) {
        this.block = block;
        this.type = type;
        this.iconSet = iconSet;
    }
}
