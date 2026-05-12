package com.extfro.extfrocore.integration.emi;

import com.extfro.extfrocore.api.machine.MachineDefinition;
import com.extfro.extfrocore.api.machine.MultiblockMachineDefinition;
import com.extfro.extfrocore.api.recipe.category.GTRecipeCategory;
import com.extfro.extfrocore.api.registry.GTRegistries;
import com.extfro.extfrocore.common.data.GTFluids;
import com.extfro.extfrocore.common.data.GTItems;
import com.extfro.extfrocore.common.data.GTMenuTypes;
import com.extfro.extfrocore.common.data.GTRecipeTypes;
import com.extfro.extfrocore.common.data.machines.GTMultiMachines;
import com.extfro.extfrocore.common.fluid.potion.PotionFluid;
import com.extfro.extfrocore.common.fluid.potion.PotionFluidHelper;
import com.extfro.extfrocore.common.item.behavior.IntCircuitBehaviour;
import com.extfro.extfrocore.config.ConfigHolder;
import com.extfro.extfrocore.integration.emi.circuit.GTProgrammedCircuitCategory;
import com.extfro.extfrocore.integration.emi.multipage.MultiblockInfoEmiCategory;
import com.extfro.extfrocore.integration.emi.oreprocessing.GTOreProcessingEmiCategory;
import com.extfro.extfrocore.integration.emi.orevein.GTBedrockFluidEmiCategory;
import com.extfro.extfrocore.integration.emi.orevein.GTBedrockOreEmiCategory;
import com.extfro.extfrocore.integration.emi.orevein.GTOreVeinEmiCategory;
import com.extfro.extfrocore.integration.emi.recipe.GTEmiRecipeHandler;
import com.extfro.extfrocore.integration.emi.recipe.GTRecipeEMICategory;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.neoforge.fluids.FluidStack;

import com.lowdragmc.lowdraglib2.gui.factory.LDMenuTypes;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiStack;
import org.jetbrains.annotations.ApiStatus;

import java.util.Comparator;
import java.util.List;

@EmiEntrypoint
public class GTEMIPlugin implements EmiPlugin {

    @ApiStatus.Internal
    public static List<MachineDefinition> SORTED_MACHINES = null;

    @Override
    public void register(EmiRegistry registry) {
        if (SORTED_MACHINES == null) {
            SORTED_MACHINES = GTRegistries.MACHINES.stream()
                    .sorted(SORT_MACHINES_BY_TIER)
                    .toList();
        }

        // Categories
        registry.addCategory(MultiblockInfoEmiCategory.CATEGORY);
        if (!ConfigHolder.INSTANCE.compat.hideOreProcessingDiagrams)
            registry.addCategory(GTOreProcessingEmiCategory.CATEGORY);
        registry.addCategory(GTOreVeinEmiCategory.CATEGORY);
        registry.addCategory(GTBedrockFluidEmiCategory.CATEGORY);
        if (ConfigHolder.INSTANCE.machines.doBedrockOres)
            registry.addCategory(GTBedrockOreEmiCategory.CATEGORY);
        for (GTRecipeCategory category : GTRegistries.RECIPE_CATEGORIES) {
            if (category.shouldRegisterDisplays()) {
                registry.addCategory(GTRecipeEMICategory.CATEGORIES.apply(category));
            }
        }
        registry.addRecipeHandler(LDMenuTypes.BLOCK_UI.get(), new GTEmiRecipeHandler());
        registry.addRecipeHandler(LDMenuTypes.HELD_ITEM_UI.get(), new GTEmiRecipeHandler());
        registry.addRecipeHandler(LDMenuTypes.PLAYER_UI.get(), new GTEmiRecipeHandler());
        registry.addRecipeHandler(GTMenuTypes.COVER_UI.get(), new GTEmiRecipeHandler());
        registry.addCategory(GTProgrammedCircuitCategory.CATEGORY);

        // Recipes
        MultiblockInfoEmiCategory.registerDisplays(registry);
        GTRecipeEMICategory.registerDisplays(registry);
        if (!ConfigHolder.INSTANCE.compat.hideOreProcessingDiagrams)
            GTOreProcessingEmiCategory.registerDisplays(registry);
        GTOreVeinEmiCategory.registerDisplays(registry);
        GTBedrockFluidEmiCategory.registerDisplays(registry);
        if (ConfigHolder.INSTANCE.machines.doBedrockOres)
            GTBedrockOreEmiCategory.registerDisplays(registry);
        GTProgrammedCircuitCategory.registerDisplays(registry);

        // workstations
        GTRecipeEMICategory.registerWorkStations(registry);
        if (!ConfigHolder.INSTANCE.compat.hideOreProcessingDiagrams)
            GTOreProcessingEmiCategory.registerWorkStations(registry);
        GTOreVeinEmiCategory.registerWorkStations(registry);
        GTBedrockFluidEmiCategory.registerWorkStations(registry);
        if (ConfigHolder.INSTANCE.machines.doBedrockOres)
            GTBedrockOreEmiCategory.registerWorkStations(registry);
        registry.addWorkstation(GTRecipeEMICategory.CATEGORIES.apply(GTRecipeTypes.CHEMICAL_RECIPES.getCategory()),
                EmiStack.of(GTMultiMachines.LARGE_CHEMICAL_REACTOR.asStack()));

        // Comparators
        registry.setDefaultComparison(GTItems.TURBINE_ROTOR.asItem(), Comparison.compareComponents());

        registry.setDefaultComparison(GTItems.PROGRAMMED_CIRCUIT.asItem(), Comparison.compareComponents());
        registry.removeEmiStacks(EmiStack.of(GTItems.PROGRAMMED_CIRCUIT.asStack()));
        registry.addEmiStack(EmiStack.of(IntCircuitBehaviour.stack(0)));
        registry.addWorkstation(GTProgrammedCircuitCategory.CATEGORY, EmiStack.of(IntCircuitBehaviour.stack(0)));

        Comparison potionComparison = Comparison.compareData(
                stack -> stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY));
        PotionFluid potionFluid = GTFluids.POTION.get();
        registry.setDefaultComparison(potionFluid.getSource(), potionComparison);
        registry.setDefaultComparison(potionFluid.getFlowing(), potionComparison);

        BuiltInRegistries.POTION.holders().forEach(potion -> {
            FluidStack stack = PotionFluidHelper.getFluidFromPotion(potion, PotionFluidHelper.BOTTLE_AMOUNT);
            registry.addEmiStack(EmiStack.of(stack.getFluid(), stack.getComponentsPatch()));
        });
    }

    public static final Comparator<MachineDefinition> SORT_MACHINES_BY_TIER = (a, b) -> {
        boolean isAMulti = a instanceof MultiblockMachineDefinition;
        boolean isBMulti = b instanceof MultiblockMachineDefinition;
        if (isAMulti && !isBMulti) {
            return 1;
        } else if (!isAMulti && isBMulti) {
            return -1;
        } else {
            return a.getTier() - b.getTier();
        }
    };
}
