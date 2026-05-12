package com.extfro.extfrocore.api.item.tool;

import com.extfro.extfrocore.api.data.chemical.material.Material;
import com.extfro.extfrocore.api.data.chemical.material.properties.PropertyKey;
import com.extfro.extfrocore.api.data.chemical.material.properties.ToolProperty;
import com.extfro.extfrocore.data.recipe.CustomTags;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import org.jetbrains.annotations.NotNull;

public class MaterialToolTier implements Tier {

    public final Material material;

    public final ToolProperty property;

    public MaterialToolTier(Material material) {
        this.material = material;
        if (!material.hasProperty(PropertyKey.TOOL)) {
            throw new IllegalArgumentException("material %s hasn't got Tool Property".formatted(material));
        }
        this.property = material.getProperty(PropertyKey.TOOL);
    }

    @Override
    public int getUses() {
        return property.getDurability() * property.getDurabilityMultiplier();
    }

    @Override
    public float getSpeed() {
        return property.getHarvestSpeed();
    }

    @Override
    public float getAttackDamageBonus() {
        return property.getAttackDamage();
    }

    @Override
    public TagKey<Block> getIncorrectBlocksForDrops() {
        return CustomTags.INCORRECT_TOOL_TIERS[property.getHarvestLevel()];
    }

    @Override
    public int getEnchantmentValue() {
        return property.getEnchantability();
    }

    @Override
    @NotNull
    public Ingredient getRepairIngredient() {
        return Ingredient.EMPTY;
    }
}
