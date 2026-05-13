package com.extfro.extfrocore.data;

import com.extfro.extfrocore.api.registry.registrate.provider.GTBlockstateProvider;
import com.extfro.extfrocore.common.registry.GTRegistration;
import com.extfro.extfrocore.core.mixins.registrate.RegistrateDataProviderAccessor;
import com.extfro.extfrocore.data.datamap.DataMapsHandler;
import com.extfro.extfrocore.data.lang.LangHandler;
import com.extfro.extfrocore.data.model.BlockstateModelLoader;
import com.extfro.extfrocore.data.tags.BlockTagLoader;
import com.extfro.extfrocore.data.tags.EntityTypeTagLoader;
import com.extfro.extfrocore.data.tags.FluidTagLoader;
import com.extfro.extfrocore.data.tags.ItemTagLoader;

import net.minecraft.data.DataProvider;

import com.tterrag.registrate.providers.ProviderType;

public class GregTechDatagen {

    // we only register this so the class gets loaded. the key gets overwritten in #initPre.
    private static final ProviderType<GTBlockstateProvider> BLOCKSTATE_PROVIDER = ProviderType.registerProvider(
            "ex_blockstate",
            GTBlockstateProvider::new);

    public static void initPre() {
        DataProvider.INDENT_WIDTH.set(4);
        // replace some default providers with ours
        RegistrateDataProviderAccessor.gtceu$getTypes().forcePut("blockstate", BLOCKSTATE_PROVIDER);

        GTRegistration.REGISTRATE.addDataGenerator(ProviderType.BLOCKSTATE,
                p -> BlockstateModelLoader.init((GTBlockstateProvider) p));
    }

    public static void initPost() {
        GTRegistration.REGISTRATE.addDataGenerator(ProviderType.BLOCK_TAGS, BlockTagLoader::init);
        GTRegistration.REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, ItemTagLoader::init);
        GTRegistration.REGISTRATE.addDataGenerator(ProviderType.FLUID_TAGS, FluidTagLoader::init);
        GTRegistration.REGISTRATE.addDataGenerator(ProviderType.ENTITY_TAGS, EntityTypeTagLoader::init);
        GTRegistration.REGISTRATE.addDataGenerator(ProviderType.LANG, LangHandler::init);
        GTRegistration.REGISTRATE.addDataGenerator(ProviderType.DATA_MAP, DataMapsHandler::init);
    }
}
