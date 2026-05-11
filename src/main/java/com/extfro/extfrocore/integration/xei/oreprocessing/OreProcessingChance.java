package com.extfro.extfrocore.integration.xei.oreprocessing;

import com.extfro.extfrocore.api.recipe.content.Content;

public record OreProcessingChance(int chance, int maxChance, int tierChanceBoost) {

    public static final OreProcessingChance GUARANTEED = new OreProcessingChance(Content.maxChance(),
            Content.maxChance(), 0);

    public OreProcessingChance {
        if (chance < 0) {
            throw new IllegalArgumentException("chance must be non-negative");
        }
        if (maxChance <= 0) {
            throw new IllegalArgumentException("maxChance must be positive");
        }
    }

    public static OreProcessingChance of(int chance) {
        return new OreProcessingChance(chance, Content.maxChance(), 0);
    }

    public static OreProcessingChance of(int chance, int tierChanceBoost) {
        return new OreProcessingChance(chance, Content.maxChance(), tierChanceBoost);
    }

    public boolean isGuaranteed() {
        return chance >= maxChance && tierChanceBoost == 0;
    }
}
