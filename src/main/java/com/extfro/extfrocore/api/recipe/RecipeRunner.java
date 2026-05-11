package com.extfro.extfrocore.api.recipe;

import com.extfro.extfrocore.api.capability.recipe.IO;
import com.extfro.extfrocore.api.capability.recipe.IRecipeCapabilityHolder;
import com.extfro.extfrocore.api.capability.recipe.RecipeCapability;
import com.extfro.extfrocore.api.machine.feature.IVoidable;
import com.extfro.extfrocore.api.machine.trait.RecipeHandlerGroup;
import com.extfro.extfrocore.api.machine.trait.RecipeHandlerGroupColor;
import com.extfro.extfrocore.api.machine.trait.RecipeHandlerGroupDistinctness;
import com.extfro.extfrocore.api.machine.trait.RecipeHandlerList;
import com.extfro.extfrocore.api.recipe.chance.boost.ChanceBoostFunction;
import com.extfro.extfrocore.api.recipe.chance.logic.ChanceLogic;
import com.extfro.extfrocore.api.recipe.content.Content;

import net.minecraft.network.chat.Component;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class RecipeRunner {

    private final MachineRecipe recipe;
    private final IO io;
    private final boolean isTick;
    private final Map<RecipeCapability<?>, Object2IntMap<?>> chanceCaches;
    private final Map<IO, List<RecipeHandlerList>> capabilityProxies;
    private final boolean simulated;
    private Map<RecipeCapability<?>, List<Object>> recipeContents;
    private final Map<RecipeCapability<?>, List<Object>> searchRecipeContents;
    private final Predicate<RecipeCapability<?>> outputVoid;
    @Getter
    private int groupColor;

    public RecipeRunner(MachineRecipe recipe, IO io, boolean isTick,
                        IRecipeCapabilityHolder holder, Map<RecipeCapability<?>, Object2IntMap<?>> chanceCaches,
                        boolean simulated) {
        this.recipe = recipe;
        this.io = io;
        this.isTick = isTick;
        this.chanceCaches = chanceCaches;
        this.capabilityProxies = holder.getCapabilitiesProxy();
        this.recipeContents = new Reference2ObjectOpenHashMap<>();
        this.searchRecipeContents = simulated ? recipeContents : new Reference2ObjectOpenHashMap<>();
        this.simulated = simulated;
        this.outputVoid = capability -> holder instanceof IVoidable voidable &&
                voidable.canVoidRecipeOutputs(capability);
        this.groupColor = recipe.groupColor;
    }

    @NotNull
    public ActionResult handle(Map<RecipeCapability<?>, List<Content>> entries) {
        fillContentMatchList(entries);
        if (searchRecipeContents.isEmpty()) {
            return ActionResult.PASS_NO_CONTENTS;
        }
        return handleContents();
    }

    private void fillContentMatchList(Map<RecipeCapability<?>, List<Content>> entries) {
        ChanceBoostFunction function = recipe.recipeType.getChanceFunction();
        int recipeTier = recipe.ocLevel;
        int chanceTier = recipeTier + recipe.ocLevel;
        for (var entry : entries.entrySet()) {
            RecipeCapability<?> capability = entry.getKey();
            if (!capability.doMatchInRecipe()) continue;
            if (simulated && io == IO.OUT && outputVoid.test(capability)) continue;

            ChanceLogic logic = recipe.getChanceLogicForCapability(capability, io, isTick);
            List<Content> chancedContents = new ArrayList<>();
            if (entry.getValue().isEmpty()) continue;

            List<Object> contentList = recipeContents.computeIfAbsent(capability, ignored -> new ArrayList<>());
            List<Object> searchContentList = searchRecipeContents.computeIfAbsent(capability,
                    ignored -> new ArrayList<>());
            for (Content content : entry.getValue()) {
                searchContentList.add(content.content);
                if (simulated) continue;

                if (content.chance >= content.maxChance) {
                    contentList.add(content.content);
                } else if (content.chance > 0 || content.tierChanceBoost > 0) {
                    chancedContents.add(content);
                }
            }

            if (!chancedContents.isEmpty()) {
                Object2IntMap<?> cache = chanceCaches.get(capability);
                chancedContents = logic.roll(capability, chancedContents, function, recipeTier, chanceTier, cache,
                        recipe.getTotalRuns());
                for (Content content : chancedContents) {
                    contentList.add(content.content);
                }
            }

            if (contentList.isEmpty()) recipeContents.remove(capability);
        }
    }

    private ActionResult handleContents() {
        if (recipeContents.isEmpty()) return ActionResult.SUCCESS;
        if (!capabilityProxies.containsKey(io)) {
            return ActionResult.fail(
                    Component.translatable("extfrocore.recipe_logic.no_capabilities")
                            .append(Component.literal(": "))
                            .append(Component.literal(io.name())),
                    null, io);
        }

        List<RecipeHandlerList> handlers = capabilityProxies.getOrDefault(io, Collections.emptyList());
        if (!isTick && io.supports(IO.OUT)) {
            handlers.sort(RecipeHandlerList.COMPARATOR.reversed());
        }

        Map<RecipeHandlerGroup, List<RecipeHandlerList>> handlerGroups = new HashMap<>();
        for (RecipeHandlerList handler : handlers) {
            RecipeHelper.addToRecipeHandlerMap(handler.getGroup(), handler, handlerGroups);
        }

        for (RecipeHandlerList handler : handlerGroups.getOrDefault(RecipeHandlerGroupDistinctness.BUS_DISTINCT,
                Collections.emptyList())) {
            Map<RecipeCapability<?>, List<Object>> result = handler.handleRecipe(io, recipe, searchRecipeContents,
                    true);
            if (!result.isEmpty()) {
                for (RecipeHandlerList bypassHandler : handlerGroups.getOrDefault(
                        RecipeHandlerGroupDistinctness.BYPASS_DISTINCT, Collections.emptyList())) {
                    result = bypassHandler.handleRecipe(io, recipe, result, true);
                    if (result.isEmpty()) break;
                }
            }
            if (io == IO.OUT) {
                if (hasAnyNonVoidingContents(result)) continue;
            } else if (io == IO.IN && !result.isEmpty()) {
                continue;
            }
            if (!simulated) {
                recipeContents = handler.handleRecipe(io, recipe, recipeContents, false);
                if (!recipeContents.isEmpty()) {
                    for (RecipeHandlerList bypassHandler : handlerGroups.getOrDefault(
                            RecipeHandlerGroupDistinctness.BYPASS_DISTINCT, Collections.emptyList())) {
                        recipeContents = bypassHandler.handleRecipe(io, recipe, recipeContents, false);
                        if (recipeContents.isEmpty()) break;
                    }
                }
            }
            recipeContents.clear();
            return ActionResult.SUCCESS;
        }

        for (Map.Entry<RecipeHandlerGroup, List<RecipeHandlerList>> entry : handlerGroups.entrySet()) {
            if (entry.getKey().equals(RecipeHandlerGroupDistinctness.BUS_DISTINCT)) continue;

            if (entry.getKey() instanceof RecipeHandlerGroupColor coloredGroup) {
                if (io == IO.IN && simulated && !isTick) {
                    groupColor = coloredGroup.color();
                } else if (coloredGroup.color() != -1 && coloredGroup.color() != groupColor) {
                    continue;
                }
            }

            Map<RecipeCapability<?>, List<Object>> copiedRecipeContents = searchRecipeContents;
            for (RecipeHandlerList handler : entry.getValue()) {
                copiedRecipeContents = handler.handleRecipe(io, recipe, copiedRecipeContents, true);
                if (copiedRecipeContents.isEmpty()) break;
            }
            if (!entry.getKey().equals(RecipeHandlerGroupDistinctness.BYPASS_DISTINCT)) {
                for (RecipeHandlerList bypassHandler : handlerGroups.getOrDefault(
                        RecipeHandlerGroupDistinctness.BYPASS_DISTINCT, Collections.emptyList())) {
                    copiedRecipeContents = bypassHandler.handleRecipe(io, recipe, copiedRecipeContents, true);
                    if (copiedRecipeContents.isEmpty()) break;
                }
            }

            if (io == IO.OUT) {
                if (hasAnyNonVoidingContents(copiedRecipeContents)) continue;
            } else if (io == IO.IN && !copiedRecipeContents.isEmpty()) {
                continue;
            }
            if (simulated) return ActionResult.SUCCESS;

            for (RecipeHandlerList handler : entry.getValue()) {
                recipeContents = handler.handleRecipe(io, recipe, recipeContents, false);
                if (recipeContents.isEmpty()) return ActionResult.SUCCESS;
            }
            if (!entry.getKey().equals(RecipeHandlerGroupDistinctness.BYPASS_DISTINCT)) {
                for (RecipeHandlerList bypassHandler : handlerGroups.getOrDefault(
                        RecipeHandlerGroupDistinctness.BYPASS_DISTINCT, Collections.emptyList())) {
                    recipeContents = bypassHandler.handleRecipe(io, recipe, recipeContents, false);
                    if (recipeContents.isEmpty()) return ActionResult.SUCCESS;
                }
            }
        }

        for (var entry : recipeContents.entrySet()) {
            if (!simulated && io == IO.OUT && outputVoid.test(entry.getKey())) {
                entry.getValue().clear();
            }
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                return ActionResult.fail(null, entry.getKey(), io);
            }
        }

        boolean containsContent = false;
        for (var entry : recipeContents.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                containsContent = true;
                break;
            }
        }
        return containsContent ? ActionResult.FAIL_NO_REASON : ActionResult.PASS_NO_CONTENTS;
    }

    private boolean hasAnyNonVoidingContents(Map<RecipeCapability<?>, List<Object>> contents) {
        for (var entry : contents.entrySet()) {
            if (outputVoid.test(entry.getKey())) continue;
            if (!(entry.getValue() == null || entry.getValue().isEmpty())) {
                return true;
            }
        }
        return false;
    }
}
