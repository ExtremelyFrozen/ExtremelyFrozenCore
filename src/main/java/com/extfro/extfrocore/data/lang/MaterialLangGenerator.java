package com.extfro.extfrocore.data.lang;

import com.extfro.extfrocore.api.EFAPI;

import com.tterrag.registrate.providers.RegistrateLangProvider;

import static com.extfro.extfrocore.utils.FormattingUtil.toEnglishName;

public class MaterialLangGenerator {

    public static void generate(RegistrateLangProvider provider, final String modId) {
        EFAPI.materialManager.stream()
                .filter(mat -> mat.getModid().equals(modId))
                .forEach(material -> {
                    provider.add(material.getUnlocalizedName(), toEnglishName(material.getName()));
                });
    }
}
