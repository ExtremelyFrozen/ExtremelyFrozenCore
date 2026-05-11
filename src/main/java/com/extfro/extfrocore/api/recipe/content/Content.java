package com.extfro.extfrocore.api.recipe.content;

import com.extfro.extfrocore.api.capability.recipe.RecipeCapability;

import net.minecraft.util.ExtraCodecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class Content {

    @Getter
    public final Object content;
    public final int chance;
    public final int maxChance;
    public final int tierChanceBoost;

    public Content(Object content, int chance, int maxChance, int tierChanceBoost) {
        this.content = content;
        this.chance = chance;
        this.maxChance = maxChance;
        this.tierChanceBoost = tierChanceBoost;
    }

    public static <T> Codec<Content> codec(RecipeCapability<T> capability) {
        return RecordCodecBuilder.create(instance -> instance.group(
                        capability.serializer.codec().fieldOf("content").forGetter(value -> capability.of(value.content)),
                        ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("chance", maxChance()).forGetter(value -> value.chance),
                        ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("maxChance", maxChance()).forGetter(value -> value.maxChance),
                        Codec.INT.optionalFieldOf("tierChanceBoost", 0).forGetter(value -> value.tierChanceBoost))
                .apply(instance, Content::new));
    }

    public static int maxChance() {
        return 10_000;
    }

    public Content copy(RecipeCapability<?> capability) {
        return new Content(capability.copyContent(content), chance, maxChance, tierChanceBoost);
    }

    public Content copy(RecipeCapability<?> capability, @NotNull ContentModifier modifier) {
        if (modifier == ContentModifier.IDENTITY || isChanced()) {
            return copy(capability);
        }
        return new Content(capability.copyContent(content, modifier), chance, maxChance, tierChanceBoost);
    }

    public Content copyChanced(RecipeCapability<?> capability, @NotNull ContentModifier modifier) {
        if (modifier == ContentModifier.IDENTITY) {
            return copy(capability);
        }
        return new Content(capability.copyContent(content, modifier), chance, maxChance, tierChanceBoost);
    }

    public boolean isChanced() {
        return chance > 0 && chance < maxChance;
    }

    @Override
    public String toString() {
        return "Content{" +
                "content=" + content +
                ", chance=" + chance +
                ", maxChance=" + maxChance +
                ", tierChanceBoost=" + tierChanceBoost +
                '}';
    }
}
