package com.extfro.extfrocore.api.material.property;

public class EFGemProperty implements EFMaterialProperty {

    @Override
    public void verifyProperty(EFMaterialProperties properties) {
        properties.ensureSet(EFMaterialPropertyKey.DUST, true);
        if (properties.hasProperty(EFMaterialPropertyKey.INGOT)) {
            throw new IllegalStateException("Material " + properties.getMaterial() +
                    " has both Ingot and Gem Property, which is not allowed");
        }
    }
}
