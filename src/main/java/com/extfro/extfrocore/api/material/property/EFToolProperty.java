package com.extfro.extfrocore.api.material.property;

import com.extfro.extfrocore.api.material.EFMaterial;
import com.extfro.extfrocore.api.tool.EFMaterialToolTier;
import com.extfro.extfrocore.api.tool.EFToolType;

import net.minecraft.world.item.enchantment.Enchantment;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.LinkedHashSet;

import static com.extfro.extfrocore.api.tool.EFToolType.*;

public class EFToolProperty implements EFMaterialProperty {

    @Getter
    @Setter
    private float harvestSpeed;
    @Getter
    @Setter
    private float attackDamage;
    @Getter
    @Setter
    private float attackSpeed;
    @Getter
    @Setter
    private int durability;
    @Getter
    @Setter
    private int harvestLevel;
    @Getter
    @Setter
    private int prospectingDepth;
    @Getter
    @Setter
    private int enchantability = 10;
    @Getter
    @Setter
    private boolean ignoreCraftingTools;
    @Getter
    @Setter
    private boolean isUnbreakable;
    @Getter
    @Setter
    private boolean isMagnetic;
    @Getter
    @Setter
    private int durabilityMultiplier = 1;
    private EFMaterialToolTier toolTier;
    @Getter
    @Setter
    private EFToolType[] types;
    @Getter
    private final Object2IntMap<Enchantment> enchantments = new Object2IntArrayMap<>();

    public EFToolProperty(float harvestSpeed, float attackDamage, int durability, int harvestLevel,
                          EFToolType[] types) {
        this.harvestSpeed = harvestSpeed;
        this.attackDamage = attackDamage;
        this.durability = durability;
        this.harvestLevel = harvestLevel;
        this.types = types;
        this.prospectingDepth = this.harvestLevel * 2 + 1;
    }

    public EFToolProperty(float harvestSpeed, float attackDamage, int durability, int harvestLevel,
                          int prospectingDepth, EFToolType[] types) {
        this(harvestSpeed, attackDamage, durability, harvestLevel, types);
        this.prospectingDepth = prospectingDepth;
    }

    public EFToolProperty() {
        this(1.0F, 1.0F, 100, 2, EFToolType.getTypes().values().toArray(EFToolType[]::new));
    }

    @Override
    public void verifyProperty(EFMaterialProperties properties) {
        if (!properties.hasProperty(EFMaterialPropertyKey.WOOD)) {
            if (!properties.hasProperty(EFMaterialPropertyKey.GEM)) {
                properties.ensureSet(EFMaterialPropertyKey.INGOT, true);
            }
        }
    }

    public void addEnchantmentForTools(Enchantment enchantment, int level) {
        enchantments.put(enchantment, level);
    }

    public EFMaterialToolTier getTier(EFMaterial material) {
        if (toolTier == null) {
            toolTier = new EFMaterialToolTier(material);
        }
        return toolTier;
    }

    public boolean hasType(EFToolType toolType) {
        return Arrays.asList(types).contains(toolType);
    }

    public EFToolProperty addTypes(EFToolType... types) {
        LinkedHashSet<EFToolType> merged = new LinkedHashSet<>(Arrays.asList(this.types));
        merged.addAll(Arrays.asList(types));
        this.types = merged.toArray(EFToolType[]::new);
        return this;
    }

    public EFToolProperty removeTypes(EFToolType... types) {
        this.types = Arrays.stream(this.types)
                .filter(type -> !Arrays.asList(types).contains(type))
                .toArray(EFToolType[]::new);
        return this;
    }

    public static class Builder {

        private final EFToolProperty toolProperty;

        public static Builder of(float harvestSpeed, float attackDamage, int durability, int harvestLevel) {
            return new Builder(harvestSpeed, attackDamage, durability, harvestLevel, new EFToolType[] {
                    SWORD, PICKAXE, SHOVEL, AXE, HOE, MINING_HAMMER, SPADE, SAW, HARD_HAMMER, WRENCH, FILE,
                    CROWBAR, SCREWDRIVER, WIRE_CUTTER, SCYTHE, KNIFE, BUTCHERY_KNIFE, DRILL_LV, DRILL_MV,
                    DRILL_HV, DRILL_EV, DRILL_IV, CHAINSAW_LV, CHAINSAW_HV, CHAINSAW_IV, WRENCH_LV, WRENCH_HV,
                    WRENCH_IV, BUZZSAW, SCREWDRIVER_LV, SCREWDRIVER_HV, SCREWDRIVER_IV, WIRE_CUTTER_LV,
                    WIRE_CUTTER_HV, WIRE_CUTTER_IV
            });
        }

        public static Builder of(float harvestSpeed, float attackDamage, int durability, int harvestLevel,
                                 EFToolType... types) {
            return new Builder(harvestSpeed, attackDamage, durability, harvestLevel, types);
        }

        private Builder(float harvestSpeed, float attackDamage, int durability, int harvestLevel, EFToolType[] types) {
            toolProperty = new EFToolProperty(harvestSpeed, attackDamage, durability, harvestLevel, types);
        }

        public Builder enchantability(int enchantability) {
            toolProperty.enchantability = enchantability;
            return this;
        }

        public Builder attackSpeed(float attackSpeed) {
            toolProperty.attackSpeed = attackSpeed;
            return this;
        }

        public Builder ignoreCraftingTools() {
            toolProperty.ignoreCraftingTools = true;
            return this;
        }

        public Builder unbreakable() {
            toolProperty.isUnbreakable = true;
            return this;
        }

        public Builder types(EFToolType... types) {
            toolProperty.types = types;
            return this;
        }

        public Builder addTypes(EFToolType... types) {
            toolProperty.addTypes(types);
            return this;
        }

        public Builder enchantment(Enchantment enchantment, int level) {
            toolProperty.addEnchantmentForTools(enchantment, level);
            return this;
        }

        public Builder magnetic() {
            toolProperty.isMagnetic = true;
            return this;
        }

        public Builder durabilityMultiplier(int multiplier) {
            toolProperty.durabilityMultiplier = multiplier;
            return this;
        }

        public EFToolProperty build() {
            return toolProperty;
        }
    }
}
