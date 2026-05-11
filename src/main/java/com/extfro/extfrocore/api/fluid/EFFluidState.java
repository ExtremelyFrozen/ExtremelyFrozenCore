package com.extfro.extfrocore.api.fluid;

import com.extfro.extfrocore.ExtForCore;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public enum EFFluidState {

    LIQUID("extfrocore.fluid.state_liquid", TagKey.create(Registries.FLUID, ExtForCore.id("liquid_fluids"))),
    GAS("extfrocore.fluid.state_gas", TagKey.create(Registries.FLUID, ExtForCore.id("gaseous_fluids"))),
    PLASMA("extfrocore.fluid.state_plasma", TagKey.create(Registries.FLUID, ExtForCore.id("plasma_fluids")));

    @Getter
    private final String translationKey;
    @Getter
    private final TagKey<Fluid> tagKey;

    EFFluidState(@NotNull String translationKey, @NotNull TagKey<Fluid> tagKey) {
        this.translationKey = translationKey;
        this.tagKey = tagKey;
    }
}
