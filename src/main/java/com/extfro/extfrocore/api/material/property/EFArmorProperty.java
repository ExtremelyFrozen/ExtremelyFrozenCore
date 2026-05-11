package com.extfro.extfrocore.api.material.property;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.material.EFMaterial;

import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class EFArmorProperty implements EFMaterialProperty {

    @Setter
    @Range(from = 0, to = Integer.MAX_VALUE)
    private int durabilityMultiplier;
    @Setter
    private Map<ArmorItem.Type, Integer> protectionValues;
    @Setter
    private int enchantability;
    private Supplier<Holder<SoundEvent>> sound;
    @Setter
    private float toughness;
    @Setter
    private float knockbackResistance;
    @Nullable
    @Setter
    private Supplier<@NotNull Ingredient> repairIngredient;
    private boolean noRepair;
    @Setter
    private String name = ExtForCore.MOD_ID + ":metal";
    @Getter
    @Setter
    private CustomTextureGetter customTextureGetter = (stack, entity, slot, overlay) -> null;
    @Getter
    @Setter
    private boolean dyeable;
    @Getter
    private ArmorMaterial armorMaterial;
    private EFMaterial material;

    public EFArmorProperty(int durabilityMultiplier, int[] protectionValues) {
        this.durabilityMultiplier = durabilityMultiplier;
        this.protectionValues = Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
            ArmorItem.Type[] values = ArmorItem.Type.values();
            for (int i = 0; i < values.length && i < protectionValues.length; i++) {
                map.put(values[i], protectionValues[i]);
            }
        });
        this.sound = () -> SoundEvents.ARMOR_EQUIP_IRON;
        this.toughness = 0;
        this.knockbackResistance = 0;
    }

    public void setSound(Supplier<Holder<SoundEvent>> sound) {
        this.sound = sound;
    }

    @Override
    public void verifyProperty(EFMaterialProperties properties) {
        if (this.material == null) {
            this.material = properties.getMaterial();
        }
        if (this.repairIngredient == null && !noRepair) {
            this.repairIngredient = () -> Ingredient.EMPTY;
        }
        if (this.armorMaterial == null) {
            this.armorMaterial = new ArmorMaterial(
                    protectionValues,
                    enchantability,
                    sound.get(),
                    repairIngredient,
                    List.of(new ArmorMaterial.Layer(ResourceLocation.parse(name))),
                    toughness,
                    knockbackResistance);
        }
    }

    public static class Builder {

        private final EFArmorProperty armorProperty;

        public static EFArmorProperty.Builder of(int durabilityMultiplier, int[] protectionValues) {
            Preconditions.checkArgument(protectionValues != null && protectionValues.length == 4,
                    "protectionValues must have 4 entries");
            return new EFArmorProperty.Builder(durabilityMultiplier, protectionValues);
        }

        private Builder(int durabilityMultiplier, int[] protectionValues) {
            armorProperty = new EFArmorProperty(durabilityMultiplier, protectionValues);
        }

        public EFArmorProperty.Builder unbreakable() {
            armorProperty.durabilityMultiplier = 0;
            return this;
        }

        public EFArmorProperty.Builder enchantability(int enchantability) {
            armorProperty.enchantability = enchantability;
            return this;
        }

        public EFArmorProperty.Builder protectionValue(ArmorItem.Type type, int value) {
            armorProperty.protectionValues.put(type, value);
            return this;
        }

        public EFArmorProperty.Builder protectionValues(Map<ArmorItem.Type, Integer> protectionValues) {
            Preconditions.checkArgument(protectionValues != null && protectionValues.size() == 4,
                    "protectionValues must have 4 entries");
            armorProperty.protectionValues = protectionValues;
            return this;
        }

        public EFArmorProperty.Builder repairIngredient(@Nullable Supplier<@NotNull Ingredient> repairIngredient) {
            if (repairIngredient == null) {
                armorProperty.repairIngredient = null;
                armorProperty.noRepair = true;
            } else {
                armorProperty.repairIngredient = repairIngredient;
            }
            return this;
        }

        public EFArmorProperty.Builder toughness(float toughness) {
            armorProperty.toughness = toughness;
            return this;
        }

        public EFArmorProperty.Builder knockbackResistance(float knockbackResistance) {
            armorProperty.knockbackResistance = knockbackResistance;
            return this;
        }

        public EFArmorProperty.Builder dyeable(boolean dyeable) {
            armorProperty.dyeable = dyeable;
            return this;
        }

        public EFArmorProperty.Builder customTexture(EFArmorProperty.@NotNull CustomTextureGetter textureGetter) {
            armorProperty.customTextureGetter = textureGetter;
            return this;
        }

        public EFArmorProperty build() {
            return armorProperty;
        }
    }

    @FunctionalInterface
    public interface CustomTextureGetter {

        ResourceLocation getCustomTexture(ItemStack stack, Entity entity, EquipmentSlot slot, boolean overlay);
    }
}
