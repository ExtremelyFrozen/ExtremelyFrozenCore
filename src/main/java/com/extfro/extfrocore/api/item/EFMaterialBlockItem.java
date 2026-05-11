package com.extfro.extfrocore.api.item;

import com.extfro.extfrocore.api.block.EFMaterialBlock;
import com.extfro.extfrocore.api.material.EFMaterial;
import com.extfro.extfrocore.api.material.property.EFDustProperty;
import com.extfro.extfrocore.api.material.property.EFMaterialPropertyKey;
import com.extfro.extfrocore.api.material.tag.EFMaterialTag;
import com.extfro.extfrocore.api.material.tag.EFMaterialTags;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EFMaterialBlockItem extends BlockItem {

    protected final EFMaterialTag materialTag;
    protected final EFMaterial material;

    public EFMaterialBlockItem(Block block, Properties properties, EFMaterialTag materialTag, EFMaterial material) {
        super(block, properties);
        this.materialTag = materialTag;
        this.material = material;
    }

    @Override
    public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
        EFDustProperty property = material.getProperty(EFMaterialPropertyKey.DUST);
        if (property != null) {
            return (int) (property.getBurnTime() * materialTag.getMaterialAmount(material) / EFMaterialTags.UNIT);
        }
        return -1;
    }

    @Override
    @NotNull
    public EFMaterialBlock getBlock() {
        return (EFMaterialBlock) super.getBlock();
    }

    public static ItemColor tintColor(EFMaterial material) {
        return (itemStack, index) -> material.getLayerARGB(index);
    }

    @Override
    public String getDescriptionId() {
        return getBlock().getDescriptionId();
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return getDescriptionId();
    }

    @Override
    public Component getDescription() {
        return getBlock().getName();
    }

    @Override
    public Component getName(ItemStack stack) {
        return getDescription();
    }
}
