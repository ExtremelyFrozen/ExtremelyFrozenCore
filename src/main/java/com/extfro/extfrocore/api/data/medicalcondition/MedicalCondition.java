package com.extfro.extfrocore.api.data.medicalcondition;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.common.capability.MedicalConditionTracker;
import com.extfro.extfrocore.data.recipe.builder.GTRecipeBuilder;
import com.extfro.extfrocore.data.recipe.misc.AirScrubberRecipes;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;

import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

@Accessors(chain = true)
public class MedicalCondition {

    public static final Map<String, MedicalCondition> CONDITIONS = new HashMap<>();
    public static final Codec<MedicalCondition> CODEC = Codec.stringResolver(MedicalCondition::getName,
            MedicalCondition.CONDITIONS::get);

    @Getter
    public final String name;
    public final ResourceLocation id;
    public final int color;
    public final float maxProgression; // amount of seconds until maximum progression is reached
    public final Set<Symptom.ConfiguredSymptom> symptoms = new HashSet<>();
    @Getter
    private final ResourceKey<DamageType> damageType;
    public final IdleProgressionType idleProgressionType;
    public final float idleProgressionRate;
    public final boolean canBePermanent;
    /**
     * This should mirror the {@link AirScrubberRecipes} recipe's outputs for this condition.
     */
    @Getter
    @Setter
    @NotNull
    public Consumer<GTRecipeBuilder> recipeModifier = builder -> {};

    public MedicalCondition(String name, int color, int maxProgression, IdleProgressionType idleProgressionType,
                            float idleProgressionRate, boolean canBePermanent, Symptom.ConfiguredSymptom... symptoms) {
        this.name = name;
        this.id = ExtForCore.id(name);
        this.color = color;
        this.maxProgression = maxProgression;
        this.damageType = ResourceKey.create(Registries.DAMAGE_TYPE, ExtForCore.id("medical_condition/" + name));
        for (Symptom.ConfiguredSymptom symptom : symptoms) {
            symptom.addedToCondition(this);
            this.symptoms.add(symptom);
        }
        this.idleProgressionType = idleProgressionType;
        this.idleProgressionRate = idleProgressionRate;
        this.canBePermanent = canBePermanent;

        CONDITIONS.put(name, this);
    }

    public MedicalCondition(String name, int color, int maxProgression, IdleProgressionType progressionType,
                            boolean canBePermanent, Symptom.ConfiguredSymptom... symptoms) {
        this(name, color, maxProgression, progressionType, 1, canBePermanent, symptoms);
    }

    public MedicalCondition(String name, int color, int maxProgression, Symptom.ConfiguredSymptom... symptoms) {
        this(name, color, maxProgression, IdleProgressionType.NONE, 0, false, symptoms);
    }

    public DamageSource getDamageSource(MedicalConditionTracker tracker) {
        return tracker.getPlayer().damageSources().source(damageType);
    }

    public DamageSource getDamageSource(Level level) {
        return level.damageSources().source(damageType);
    }

    public Component getTranslatableName() {
        return Component.translatable("gtceu.medical_condition." + name);
    }

    public enum IdleProgressionType {
        UNTREATED_PROGRESSION,
        HEAL,
        NONE
    }
}
