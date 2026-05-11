package com.extfro.extfrocore.client.renderer.item;

import com.extfro.extfrocore.api.material.info.EFMaterialIconSet;
import com.extfro.extfrocore.api.material.info.EFMaterialIconType;
import com.extfro.extfrocore.data.pack.EFDynamicResourcePack;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.models.model.DelegatedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.HashSet;
import java.util.Set;

public class EFMaterialItemRenderer {

    private static final Set<EFMaterialItemRenderer> MODELS = new HashSet<>();

    public static void create(Item item, EFMaterialIconType type, EFMaterialIconSet iconSet) {
        MODELS.add(new EFMaterialItemRenderer(item, type, iconSet));
    }

    public static void reinitModels() {
        for (EFMaterialItemRenderer model : MODELS) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(model.item);
            EFDynamicResourcePack.addItemModel(itemId,
                    new DelegatedModel(model.type.getItemModelPath(model.iconSet, true)));
        }
    }

    private final Item item;
    private final EFMaterialIconType type;
    private final EFMaterialIconSet iconSet;

    private EFMaterialItemRenderer(Item item, EFMaterialIconType type, EFMaterialIconSet iconSet) {
        this.item = item;
        this.type = type;
        this.iconSet = iconSet;
    }
}
