package com.extfro.extfrocore.api.item;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.material.EFMaterial;
import com.extfro.extfrocore.api.material.property.EFDustProperty;
import com.extfro.extfrocore.api.material.property.EFMaterialPropertyKey;
import com.extfro.extfrocore.api.material.tag.EFMaterialTag;
import com.extfro.extfrocore.api.material.tag.EFMaterialTags;
import com.extfro.extfrocore.client.renderer.item.EFMaterialItemRenderer;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

public class EFMaterialItem extends Item {

    protected final EFMaterialTag materialTag;
    protected final EFMaterial material;

    public EFMaterialItem(Properties properties, EFMaterialTag materialTag, EFMaterial material) {
        super(properties);
        this.materialTag = materialTag;
        this.material = material;
        if (ExtForCore.isClientSide()) {
            registerModel();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void registerModel() {
        EFMaterialItemRenderer.create(this, materialTag.materialIconType(), material.getMaterialIconSet());
    }

    @Override
    public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
        EFDustProperty property = material.getProperty(EFMaterialPropertyKey.DUST);
        if (property != null) {
            return (int) (property.getBurnTime() * materialTag.getMaterialAmount(material) / EFMaterialTags.UNIT);
        }
        return -1;
    }

    public static ItemColor tintColor(EFMaterial material) {
        return (itemStack, index) -> material.getLayerARGB(index);
    }

    @Override
    public String getDescriptionId() {
        return materialTag.getUnlocalizedName(material);
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return getDescriptionId();
    }

    @Override
    public Component getDescription() {
        return materialTag.getLocalizedName(material);
    }

    @Override
    public Component getName(ItemStack stack) {
        return getDescription();
    }
}
