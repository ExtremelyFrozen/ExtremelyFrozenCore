package com.extfro.extfrocore.integration.jei;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.recipe.category.GTRecipeCategory;
import com.extfro.extfrocore.api.registry.GTRegistries;
import com.extfro.extfrocore.common.data.GTFluids;
import com.extfro.extfrocore.common.data.GTItems;
import com.extfro.extfrocore.common.data.GTRecipeTypes;
import com.extfro.extfrocore.common.data.machines.GTMultiMachines;
import com.extfro.extfrocore.common.fluid.potion.PotionFluid;
import com.extfro.extfrocore.common.item.behavior.IntCircuitBehaviour;
import com.extfro.extfrocore.config.ConfigHolder;
import com.extfro.extfrocore.integration.jei.circuit.GTProgrammedCircuitCategory;
import com.extfro.extfrocore.integration.jei.multipage.MultiblockInfoCategory;
import com.extfro.extfrocore.integration.jei.oreprocessing.GTOreProcessingInfoCategory;
import com.extfro.extfrocore.integration.jei.orevein.GTBedrockFluidInfoCategory;
import com.extfro.extfrocore.integration.jei.orevein.GTBedrockOreInfoCategory;
import com.extfro.extfrocore.integration.jei.orevein.GTOreVeinInfoCategory;
import com.extfro.extfrocore.integration.jei.recipe.GTRecipeJEICategory;
import com.extfro.extfrocore.integration.jei.subtype.CircuitSubtypeInterpreter;
import com.extfro.extfrocore.integration.jei.subtype.MaterialSubtypeInterpreter;
import com.extfro.extfrocore.integration.jei.subtype.PotionFluidSubtypeInterpreter;
import com.extfro.extfrocore.integration.xei.widgets.GTProgrammedCircuitWidget;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.FluidStack;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.registration.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@JeiPlugin
public class GTJEIPlugin implements IModPlugin {

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ExtForCore.id("jei_plugin");
    }

    @Override
    public void registerCategories(@NotNull IRecipeCategoryRegistration registry) {
        if (!ExtForCore.Mods.isJEILoaded()) return;

        IJeiHelpers jeiHelpers = registry.getJeiHelpers();
        registry.addRecipeCategories(new MultiblockInfoCategory(jeiHelpers));
        if (!ConfigHolder.INSTANCE.compat.hideOreProcessingDiagrams)
            registry.addRecipeCategories(new GTOreProcessingInfoCategory(jeiHelpers));
        registry.addRecipeCategories(new GTOreVeinInfoCategory(jeiHelpers));
        registry.addRecipeCategories(new GTBedrockFluidInfoCategory(jeiHelpers));
        if (ConfigHolder.INSTANCE.machines.doBedrockOres)
            registry.addRecipeCategories(new GTBedrockOreInfoCategory(jeiHelpers));
        for (GTRecipeCategory category : GTRegistries.RECIPE_CATEGORIES) {
            if (category.shouldRegisterDisplays()) {
                registry.addRecipeCategories(new GTRecipeJEICategory(jeiHelpers, category));
            }
        }
        registry.addRecipeCategories(new GTProgrammedCircuitCategory(jeiHelpers));
    }

    @Override
    public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
        if (!ExtForCore.Mods.isJEILoaded()) return;

        GTRecipeJEICategory.registerRecipeCatalysts(registration);
        if (!ConfigHolder.INSTANCE.compat.hideOreProcessingDiagrams)
            GTOreProcessingInfoCategory.registerRecipeCatalysts(registration);
        GTOreVeinInfoCategory.registerRecipeCatalysts(registration);
        GTBedrockFluidInfoCategory.registerRecipeCatalysts(registration);
        if (ConfigHolder.INSTANCE.machines.doBedrockOres)
            GTBedrockOreInfoCategory.registerRecipeCatalysts(registration);
        registration.addRecipeCatalyst(GTMultiMachines.LARGE_CHEMICAL_REACTOR.asStack(),
                GTRecipeJEICategory.TYPES.apply(GTRecipeTypes.CHEMICAL_RECIPES.getCategory()));
        registration.addRecipeCatalyst(IntCircuitBehaviour.stack(0), GTProgrammedCircuitCategory.RECIPE_TYPE);
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        if (!ExtForCore.Mods.isJEILoaded()) return;

        MultiblockInfoCategory.registerRecipes(registration);
        GTRecipeJEICategory.registerRecipes(registration);
        if (!ConfigHolder.INSTANCE.compat.hideOreProcessingDiagrams)
            GTOreProcessingInfoCategory.registerRecipes(registration);
        GTOreVeinInfoCategory.registerRecipes(registration);
        GTBedrockFluidInfoCategory.registerRecipes(registration);
        if (ConfigHolder.INSTANCE.machines.doBedrockOres)
            GTBedrockOreInfoCategory.registerRecipes(registration);
        registration.addRecipes(GTProgrammedCircuitCategory.RECIPE_TYPE, List.of(new GTProgrammedCircuitWidget()));
    }

    @Override
    public void registerItemSubtypes(@NotNull ISubtypeRegistration registration) {
        if (!ExtForCore.Mods.isJEILoaded()) return;

        registration.registerSubtypeInterpreter(GTItems.PROGRAMMED_CIRCUIT.asItem(),
                CircuitSubtypeInterpreter.INSTANCE);
        registration.registerSubtypeInterpreter(GTItems.TURBINE_ROTOR.asItem(), MaterialSubtypeInterpreter.INSTANCE);
    }

    @Override
    public <T> void registerFluidSubtypes(@NotNull ISubtypeRegistration registration,
                                          @NotNull IPlatformFluidHelper<T> platformFluidHelper) {
        if (!ExtForCore.Mods.isJEILoaded()) return;

        PotionFluidSubtypeInterpreter interpreter = PotionFluidSubtypeInterpreter.INSTANCE;
        PotionFluid potionFluid = GTFluids.POTION.get();
        registration.registerSubtypeInterpreter(NeoForgeTypes.FLUID_STACK, potionFluid.getSource(), interpreter);
        registration.registerSubtypeInterpreter(NeoForgeTypes.FLUID_STACK, potionFluid.getFlowing(), interpreter);
    }

    @Override
    public void registerExtraIngredients(@NotNull IExtraIngredientRegistration registration) {
        if (!ExtForCore.Mods.isJEILoaded()) return;

        Collection<FluidStack> potionFluids = new ArrayList<>(BuiltInRegistries.POTION.size());
        BuiltInRegistries.POTION.holders().forEach(potion -> {
            FluidStack potionFluid = PotionFluid.of(1000, potion);
            potionFluids.add(potionFluid);
        });
        registration.addExtraIngredients(NeoForgeTypes.FLUID_STACK, potionFluids);
    }
}
