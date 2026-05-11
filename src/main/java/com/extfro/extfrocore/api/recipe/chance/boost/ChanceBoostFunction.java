package com.extfro.extfrocore.api.recipe.chance.boost;

import com.extfro.extfrocore.api.recipe.content.Content;

import net.minecraft.util.Mth;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ChanceBoostFunction {

    ChanceBoostFunction OVERCLOCK = (entry, recipeTier, chanceTier) -> {
        int tierDiff = chanceTier - recipeTier;
        if (tierDiff <= 0) return entry.chance;
        return Mth.clamp(entry.chance + (entry.tierChanceBoost * tierDiff), 0, entry.maxChance);
    };

    ChanceBoostFunction NONE = (entry, recipeTier, chanceTier) -> entry.chance;

    int getBoostedChance(@NotNull Content entry, int recipeTier, int chanceTier);
}
