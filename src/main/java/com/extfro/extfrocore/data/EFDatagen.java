package com.extfro.extfrocore.data;

import com.extfro.extfrocore.common.registry.EFRegistration;
import com.extfro.extfrocore.data.lang.EFLangHandler;
import com.extfro.extfrocore.data.material.EFMaterialIconModelProvider;

import net.minecraft.data.DataProvider;

import com.tterrag.registrate.providers.ProviderType;

public final class EFDatagen {

    private static boolean initialized;

    private EFDatagen() {}

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        DataProvider.INDENT_WIDTH.set(4);

        EFRegistration.REGISTRATE.addDataGenerator(ProviderType.LANG, EFLangHandler::init);
        EFRegistration.REGISTRATE.addDataGenerator(ProviderType.GENERIC_CLIENT,
                provider -> provider.add(data -> new EFMaterialIconModelProvider(data.output(), data.registries(),
                        data.existingFileHelper())));
    }
}
