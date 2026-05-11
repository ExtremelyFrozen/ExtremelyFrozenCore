package com.extfro.extfrocore.api.tool;

import com.extfro.extfrocore.api.material.EFMaterial;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import org.jetbrains.annotations.NotNull;

public class EFMaterialToolTier implements Tier {

    private final EFMaterial material;

    public EFMaterialToolTier(EFMaterial material) {
        this.material = material;
    }

    @Override
    public int getUses() {
        var property = material.getProperty(com.extfro.extfrocore.api.material.property.EFMaterialPropertyKey.TOOL);
        return property == null ? 0 : property.getDurability();
    }

    @Override
    public float getSpeed() {
        var property = material.getProperty(com.extfro.extfrocore.api.material.property.EFMaterialPropertyKey.TOOL);
        return property == null ? 1.0F : property.getHarvestSpeed();
    }

    @Override
    public float getAttackDamageBonus() {
        var property = material.getProperty(com.extfro.extfrocore.api.material.property.EFMaterialPropertyKey.TOOL);
        return property == null ? 0.0F : property.getAttackDamage();
    }

    @Override
    public @NotNull TagKey<Block> getIncorrectBlocksForDrops() {
        return BlockTags.INCORRECT_FOR_IRON_TOOL;
    }

    @Override
    public int getEnchantmentValue() {
        var property = material.getProperty(com.extfro.extfrocore.api.material.property.EFMaterialPropertyKey.TOOL);
        return property == null ? 0 : property.getEnchantability();
    }

    @Override
    public @NotNull Ingredient getRepairIngredient() {
        return Ingredient.EMPTY;
    }
}
