package com.extfro.extfrocore.api.material.property;

import com.extfro.extfrocore.api.medical.EFMedicalCondition;

import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class EFHazardProperty implements EFMaterialProperty {

    public final EFMedicalCondition condition;
    public final HazardTrigger hazardTrigger;
    public final boolean applyToDerivatives;
    public final float progressionMultiplier;

    public EFHazardProperty(HazardTrigger hazardTrigger, EFMedicalCondition condition, float progressionMultiplier,
                            boolean applyToDerivatives) {
        this.hazardTrigger = hazardTrigger;
        this.condition = condition;
        this.applyToDerivatives = applyToDerivatives;
        this.progressionMultiplier = progressionMultiplier;
    }

    @Override
    public void verifyProperty(EFMaterialProperties properties) {}

    public record HazardTrigger(String name, ProtectionType protectionType) implements StringRepresentable {

        public static final Map<String, HazardTrigger> ALL_TRIGGERS = new HashMap<>();

        public static final HazardTrigger INHALATION = new HazardTrigger("inhalation", ProtectionType.MASK);
        public static final HazardTrigger ANY = new HazardTrigger("any", ProtectionType.FULL);
        public static final HazardTrigger SKIN_CONTACT = new HazardTrigger("skin_contact", ProtectionType.HANDS);
        public static final HazardTrigger NONE = new HazardTrigger("none", ProtectionType.NONE);
        public static final HazardTrigger CONSUMPTION = new HazardTrigger("consumption", ProtectionType.NONE);

        public HazardTrigger {
            ALL_TRIGGERS.put(name, this);
        }

        public boolean isAffected(Object prefix) {
            return true;
        }

        public Component getTranslatableName() {
            return Component.translatable("tooltip.extfrocore.hazard_trigger." + this.name);
        }

        @Override
        public @NotNull String getSerializedName() {
            return this.name;
        }
    }

    public enum ProtectionType {

        MASK(Set.of("head"), ArmorItem.Type.HELMET),
        HANDS(Set.of("hands"), ArmorItem.Type.CHESTPLATE),
        FULL(Set.of(), ArmorItem.Type.BOOTS, ArmorItem.Type.HELMET, ArmorItem.Type.CHESTPLATE,
                ArmorItem.Type.LEGGINGS),
        NONE(Set.of());

        @Getter
        private final Set<ArmorItem.Type> equipmentTypes;
        @Getter
        private final Set<String> curioSlots;

        ProtectionType(Set<String> curioSlots, ArmorItem.Type... equipmentTypes) {
            this.curioSlots = curioSlots;
            this.equipmentTypes = Set.of(equipmentTypes);
        }

        public boolean isProtected(LivingEntity livingEntity) {
            return this == NONE;
        }

        public void damageEquipment(Player player, int amount) {}
    }
}
