package com.extfro.extfrocore.api.recipe.chance.logic;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.capability.recipe.RecipeCapability;
import com.extfro.extfrocore.api.recipe.chance.boost.ChanceBoostFunction;
import com.extfro.extfrocore.api.recipe.content.Content;
import com.extfro.extfrocore.api.recipe.content.ContentModifier;
import com.extfro.extfrocore.api.registry.EFRegistries;

import net.minecraft.network.chat.Component;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public abstract class ChanceLogic {

    public static final ChanceLogic OR = new ChanceLogic("or") {

        @Override
        public @Unmodifiable List<@NotNull Content> roll(RecipeCapability<?> cap,
                                                         @NotNull @Unmodifiable List<@NotNull Content> chancedEntries,
                                                         @NotNull ChanceBoostFunction boostFunction, int recipeTier,
                                                         int chanceTier, @Nullable Object2IntMap<?> cache, int times) {
            ImmutableList.Builder<Content> builder = ImmutableList.builder();
            for (Content entry : chancedEntries) {
                int maxChance = entry.maxChance;
                int newChance = getChance(entry, boostFunction, recipeTier, chanceTier);
                int totalChance = times * newChance;
                int guaranteed = totalChance / maxChance;
                if (guaranteed > 0) {
                    builder.add(entry.copyChanced(cap, ContentModifier.multiplier(guaranteed)));
                }
                newChance = totalChance % maxChance;

                int cached = getCachedChance(entry, cache);
                int chance = newChance + cached;
                while (passesChance(chance, maxChance)) {
                    builder.add(entry);
                    chance -= maxChance;
                    newChance -= maxChance;
                }
                updateCachedChance(entry.content, cache, newChance / 2 + cached);
            }
            return builder.build();
        }

        @Override
        public @NotNull Component getTranslation() {
            return Component.translatable("extfrocore.chance_logic.or");
        }

        @Override
        public String toString() {
            return "ChanceLogic{OR}";
        }
    };

    public static final ChanceLogic AND = new ChanceLogic("and") {

        @Override
        public @Unmodifiable List<@NotNull Content> roll(RecipeCapability<?> cap,
                                                         @NotNull @Unmodifiable List<@NotNull Content> chancedEntries,
                                                         @NotNull ChanceBoostFunction boostFunction, int recipeTier,
                                                         int chanceTier, @Nullable Object2IntMap<?> cache, int times) {
            ImmutableList.Builder<Content> builder = ImmutableList.builder();
            for (int i = 0; i < times; ++i) {
                boolean failed = false;
                for (Content entry : chancedEntries) {
                    int newChance = getChance(entry, boostFunction, recipeTier, chanceTier);
                    int cached = getCachedChance(entry, cache);
                    int chance = newChance + cached;
                    if (passesChance(chance, entry.maxChance)) {
                        newChance -= entry.maxChance;
                    } else {
                        failed = true;
                    }
                    updateCachedChance(entry.content, cache, newChance / 2 + cached);
                    if (failed) break;
                }
                if (!failed) builder.addAll(chancedEntries);
            }
            return builder.build();
        }

        @Override
        public @NotNull Component getTranslation() {
            return Component.translatable("extfrocore.chance_logic.and");
        }

        @Override
        public String toString() {
            return "ChanceLogic{AND}";
        }
    };

    @Deprecated
    public static final ChanceLogic FIRST = new ChanceLogic("first") {

        @Override
        public @Unmodifiable List<@NotNull Content> roll(RecipeCapability<?> cap,
                                                         @NotNull @Unmodifiable List<@NotNull Content> chancedEntries,
                                                         @NotNull ChanceBoostFunction boostFunction, int recipeTier,
                                                         int chanceTier, @Nullable Object2IntMap<?> cache, int times) {
            ImmutableList.Builder<Content> builder = ImmutableList.builder();
            for (int i = 0; i < times; ++i) {
                Content selected = null;
                for (Content entry : chancedEntries) {
                    int newChance = getChance(entry, boostFunction, recipeTier, chanceTier);
                    int cached = getCachedChance(entry, cache);
                    int chance = newChance + cached;
                    if (passesChance(chance, entry.maxChance)) {
                        selected = entry;
                        newChance -= entry.maxChance;
                    }
                    updateCachedChance(entry.content, cache, newChance / 2 + cached);
                    if (selected != null) break;
                }
                if (selected != null) builder.add(selected);
            }
            return builder.build();
        }

        @Override
        public @NotNull Component getTranslation() {
            return Component.translatable("extfrocore.chance_logic.first");
        }

        @Override
        public String toString() {
            return "ChanceLogic{FIRST}";
        }
    };

    public static final ChanceLogic XOR = new ChanceLogic("xor") {

        @Override
        public @Unmodifiable List<@NotNull Content> roll(RecipeCapability<?> cap,
                                                         @NotNull @Unmodifiable List<@NotNull Content> chancedEntries,
                                                         @NotNull ChanceBoostFunction boostFunction, int recipeTier,
                                                         int chanceTier, @Nullable Object2IntMap<?> cache, int times) {
            IntList chancesOutOfTenThousand = new IntArrayList();
            for (Content entry : chancedEntries) {
                if (entry.maxChance == getMaxChancedValue()) {
                    chancesOutOfTenThousand.add(entry.chance);
                } else {
                    chancesOutOfTenThousand.add((int) ((entry.chance / (float) entry.maxChance) * getMaxChancedValue()));
                }
            }

            int chanceTotal = 0;
            for (int chance : chancesOutOfTenThousand) {
                chanceTotal += chance;
            }
            if (chanceTotal != getMaxChancedValue() && chanceTotal > 0) {
                int remaining = getMaxChancedValue();
                for (int i = 0; i < chancesOutOfTenThousand.size(); i++) {
                    int newChance = (int) (chancesOutOfTenThousand.getInt(i) *
                            ((float) getMaxChancedValue() / (float) chanceTotal));
                    if (i == chancesOutOfTenThousand.size() - 1) {
                        chancesOutOfTenThousand.set(i, remaining);
                    } else {
                        chancesOutOfTenThousand.set(i, newChance);
                    }
                    remaining -= newChance;
                }
            }

            List<Content> normalizedEntries = new ArrayList<>();
            for (int i = 0; i < chancesOutOfTenThousand.size(); i++) {
                Content original = chancedEntries.get(i);
                normalizedEntries.add(new Content(original.content, chancesOutOfTenThousand.getInt(i),
                        getMaxChancedValue(), original.tierChanceBoost));
            }

            ImmutableList.Builder<Content> builder = ImmutableList.builder();
            int nonGuaranteedTimes = times;
            if (times > 1) {
                for (Content entry : normalizedEntries) {
                    int newChance = getChance(entry, boostFunction, recipeTier, chanceTier);
                    int totalChance = times * newChance;
                    int guaranteed = totalChance / getMaxChancedValue();
                    if (guaranteed > 0) {
                        builder.add(entry.copyChanced(cap, ContentModifier.multiplier(guaranteed)));
                        nonGuaranteedTimes -= guaranteed;
                    }
                }
            }
            for (int i = 0; i < nonGuaranteedTimes; ++i) {
                Content selected = null;
                int maxChance = getMaxChancedValue();
                for (Content entry : normalizedEntries) {
                    int newChance = getChance(entry, boostFunction, recipeTier, chanceTier);
                    int cached = getCachedChance(entry, cache);
                    int chance = newChance + cached;
                    if (passesChance(chance, maxChance)) {
                        selected = entry;
                        newChance -= maxChance;
                    }
                    updateCachedChance(entry.content, cache, newChance / 2 + cached);
                    if (selected != null) break;
                    maxChance -= newChance;
                }
                if (selected != null) builder.add(selected);
            }
            return builder.build();
        }

        @Override
        public @NotNull Component getTranslation() {
            return Component.translatable("extfrocore.chance_logic.xor");
        }

        @Override
        public String toString() {
            return "ChanceLogic{XOR}";
        }
    };

    public static final ChanceLogic NONE = new ChanceLogic("none") {

        @Override
        public @Unmodifiable List<@NotNull Content> roll(RecipeCapability<?> cap,
                                                         @NotNull @Unmodifiable List<@NotNull Content> chancedEntries,
                                                         @NotNull ChanceBoostFunction boostFunction, int recipeTier,
                                                         int chanceTier, @Nullable Object2IntMap<?> cache, int times) {
            return Collections.emptyList();
        }

        @Override
        public @NotNull Component getTranslation() {
            return Component.translatable("extfrocore.chance_logic.none");
        }

        @Override
        public String toString() {
            return "ChanceLogic{NONE}";
        }
    };

    public ChanceLogic(String id) {
        EFRegistries.register(EFRegistries.CHANCE_LOGICS, ExtForCore.id(id), this);
    }

    static int getChance(@NotNull Content entry, @NotNull ChanceBoostFunction boostFunction, int recipeTier,
                         int chanceTier) {
        return boostFunction.getBoostedChance(entry, recipeTier, chanceTier);
    }

    static boolean passesChance(int chance, int maxChance) {
        return chance >= maxChance;
    }

    public static int getMaxChancedValue() {
        return 10_000;
    }

    static int getCachedChance(Content entry, @Nullable Object2IntMap<?> cache) {
        if (cache == null || !cache.containsKey(entry.content)) {
            return ThreadLocalRandom.current().nextInt(entry.maxChance);
        }
        return cache.getInt(entry.content);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    static void updateCachedChance(Object ingredient, @Nullable Object2IntMap<?> cache, int chance) {
        if (cache != null) {
            ((Object2IntMap) cache).put(ingredient, chance);
        }
    }

    public abstract @Unmodifiable List<@NotNull Content> roll(RecipeCapability<?> cap,
                                                              @NotNull @Unmodifiable List<@NotNull Content> chancedEntries,
                                                              @NotNull ChanceBoostFunction boostFunction,
                                                              int recipeTier, int chanceTier,
                                                              @Nullable Object2IntMap<?> cache, int times);

    @Unmodifiable
    public List<@NotNull Content> roll(RecipeCapability<?> cap,
                                       @NotNull @Unmodifiable List<@NotNull Content> chancedEntries,
                                       @NotNull ChanceBoostFunction boostFunction, int recipeTier, int chanceTier,
                                       int times) {
        return roll(cap, chancedEntries, boostFunction, recipeTier, chanceTier, null, times);
    }

    @NotNull
    public abstract Component getTranslation();

    @ApiStatus.Internal
    public static void init() {}
}
