package com.extfro.extfrocore.api.recipe;

import com.extfro.extfrocore.api.capability.recipe.IO;
import com.extfro.extfrocore.api.capability.recipe.RecipeCapability;
import com.extfro.extfrocore.api.recipe.category.RecipeCategory;
import com.extfro.extfrocore.api.recipe.chance.boost.ChanceBoostFunction;
import com.extfro.extfrocore.api.recipe.lookup.RecipeAdditionHandler;
import com.extfro.extfrocore.api.recipe.lookup.RecipeDB;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import it.unimi.dsi.fastutil.objects.Object2IntAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2IntSortedMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class MachineRecipeType implements RecipeType<MachineRecipe> {

    public static final String LANGUAGE_KEY_PATH = "recipe_type";

    @Getter
    @Setter(onMethod_ = { @ApiStatus.Internal })
    public RecipeSerializer<MachineRecipe> serializer;

    @Getter
    public final ResourceLocation registryName;
    public final String group;
    public final Object2IntSortedMap<RecipeCapability<?>> maxInputs = new Object2IntAVLTreeMap<>(RecipeCapability.COMPARATOR);
    public final Object2IntSortedMap<RecipeCapability<?>> maxOutputs = new Object2IntAVLTreeMap<>(RecipeCapability.COMPARATOR);
    @Getter
    @Setter
    private ChanceBoostFunction chanceFunction = ChanceBoostFunction.NONE;
    @Setter
    @Getter
    private MachineRecipeType smallRecipeMap;
    @Setter
    @Getter
    @Nullable
    private Supplier<ItemStack> iconSupplier;
    @Getter
    private final Map<RecipeType<?>, List<RecipeHolder<MachineRecipe>>> proxyRecipes;
    @Getter
    private final RecipeCategory category;
    @Getter
    private final Map<RecipeCategory, Set<MachineRecipe>> categoryMap = new Object2ObjectOpenHashMap<>();
    @Getter
    private final RecipeDB lookup = new RecipeDB();
    private final RecipeAdditionHandler additionHandler = new RecipeAdditionHandler(lookup);
    @Getter
    private final List<ICustomRecipeLogic> customRecipeLogicRunners = new java.util.ArrayList<>();
    @Getter
    private int minRecipeConditions = 0;

    public MachineRecipeType(ResourceLocation registryName, String group, RecipeType<?>... proxyRecipes) {
        this.registryName = registryName;
        this.group = group;
        this.category = RecipeCategory.registerDefault(this);
        Map<RecipeType<?>, List<RecipeHolder<MachineRecipe>>> map = new Object2ObjectLinkedOpenHashMap<>();
        for (RecipeType<?> proxyRecipe : proxyRecipes) {
            map.put(proxyRecipe, new java.util.ArrayList<>());
        }
        this.proxyRecipes = map;
    }

    public MachineRecipeType setMaxSize(IO io, RecipeCapability<?> capability, int max) {
        if (io == IO.IN || io == IO.BOTH) {
            maxInputs.put(capability, max);
        }
        if (io == IO.OUT || io == IO.BOTH) {
            maxOutputs.put(capability, max);
        }
        return this;
    }

    public int getMaxInputs(RecipeCapability<?> capability) {
        return maxInputs.getOrDefault(capability, 0);
    }

    public int getMaxOutputs(RecipeCapability<?> capability) {
        return maxOutputs.getOrDefault(capability, 0);
    }

    public MachineRecipeType addCustomRecipeLogic(ICustomRecipeLogic recipeLogic) {
        customRecipeLogicRunners.add(recipeLogic);
        return this;
    }

    public void setMinRecipeConditions(int count) {
        minRecipeConditions = Math.max(minRecipeConditions, count);
    }

    public void buildRepresentativeRecipes() {
        for (ICustomRecipeLogic logic : customRecipeLogicRunners) {
            logic.buildRepresentativeRecipes();
        }
    }

    public void addToMainCategory(MachineRecipe recipe) {
        addToCategoryMap(category, recipe);
    }

    public void addToCategoryMap(RecipeCategory category, MachineRecipe recipe) {
        categoryMap.computeIfAbsent(category, key -> new ObjectLinkedOpenHashSet<>()).add(recipe);
    }

    public void clearRecipes() {
        categoryMap.clear();
        lookup.clear();
    }

    public void beginStagingRecipes() {
        categoryMap.clear();
        additionHandler.beginStaging();
    }

    public void addStagingRecipe(MachineRecipe recipe) {
        additionHandler.addStaging(recipe);
    }

    public void completeStagingRecipes() {
        additionHandler.completeStaging();
    }

    public Set<RecipeCategory> getCategories() {
        return Collections.unmodifiableSet(categoryMap.keySet());
    }

    public Set<MachineRecipe> getRecipesInCategory(RecipeCategory category) {
        return Collections.unmodifiableSet(categoryMap.getOrDefault(category, Set.of()));
    }

    public Iterator<MachineRecipe> searchRecipe(RecipeLogicContext context, Predicate<MachineRecipe> canHandle) {
        if (context instanceof com.extfro.extfrocore.api.capability.recipe.IRecipeCapabilityHolder holder) {
            RecipeDB.RecipeIterator iterator = lookup.iterator(holder,
                    recipe -> recipe.conditions.size() >= minRecipeConditions && canHandle.test(recipe));
            if (iterator != null) {
                return iterator;
            }
        } else if (context.machine() instanceof com.extfro.extfrocore.api.capability.recipe.IRecipeCapabilityHolder holder) {
            RecipeDB.RecipeIterator iterator = lookup.iterator(holder,
                    recipe -> recipe.conditions.size() >= minRecipeConditions && canHandle.test(recipe));
            if (iterator != null) {
                return iterator;
            }
        }
        List<MachineRecipe> matches = categoryMap.values().stream()
                .flatMap(Set::stream)
                .filter(recipe -> recipe.conditions.size() >= minRecipeConditions)
                .filter(canHandle)
                .toList();
        if (!matches.isEmpty()) {
            return matches.iterator();
        }
        for (ICustomRecipeLogic logic : customRecipeLogicRunners) {
            MachineRecipe recipe = logic.createCustomRecipe(context);
            if (recipe != null && canHandle.test(recipe)) {
                return Collections.singleton(recipe).iterator();
            }
        }
        return Collections.emptyIterator();
    }

    public String getTranslationKey() {
        return registryName.toLanguageKey(LANGUAGE_KEY_PATH);
    }

    public Component getName() {
        return Component.translatable(getTranslationKey());
    }

    @Override
    public String toString() {
        return registryName.toString();
    }

    public interface ICustomRecipeLogic {

        @Nullable
        MachineRecipe createCustomRecipe(RecipeLogicContext context);

        default void buildRepresentativeRecipes() {}
    }
}
