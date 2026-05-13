package com.extfro.extfrocore.data.datamap;

import com.extfro.extfrocore.data.recipe.misc.ComposterRecipes;

import net.neoforged.neoforge.registries.datamaps.builtin.Compostable;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;

import com.tterrag.registrate.providers.RegistrateDataMapProvider;

public class DataMapsHandler {

    public static void init(RegistrateDataMapProvider provider) {
        final var compostables = provider.builder(NeoForgeDataMaps.COMPOSTABLES);
        ComposterRecipes.addComposterRecipes((item, chance) -> compostables.add(item.builtInRegistryHolder(),
                new Compostable(chance), false));
    }
}
