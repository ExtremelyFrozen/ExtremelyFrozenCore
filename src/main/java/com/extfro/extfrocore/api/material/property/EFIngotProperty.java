package com.extfro.extfrocore.api.material.property;

import com.extfro.extfrocore.api.material.EFMaterial;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

public class EFIngotProperty implements EFMaterialProperty {

    @Getter
    @Setter
    @NotNull
    private EFMaterial smeltingInto = EFMaterial.EMPTY;
    @Getter
    @Setter
    @NotNull
    private EFMaterial arcSmeltingInto = EFMaterial.EMPTY;
    @Getter
    @Setter
    @NotNull
    private EFMaterial macerateInto = EFMaterial.EMPTY;
    @Getter
    @Setter
    @NotNull
    private EFMaterial magneticMaterial = EFMaterial.EMPTY;

    @Override
    public void verifyProperty(EFMaterialProperties properties) {
        properties.ensureSet(EFMaterialPropertyKey.DUST, true);
        if (properties.hasProperty(EFMaterialPropertyKey.GEM)) {
            throw new IllegalStateException("Material " + properties.getMaterial() +
                    " has both Ingot and Gem Property, which is not allowed");
        }

        if (smeltingInto.isEmpty()) {
            smeltingInto = properties.getMaterial();
        } else {
            smeltingInto.getProperties().ensureSet(EFMaterialPropertyKey.INGOT, true);
        }
        if (arcSmeltingInto.isEmpty()) {
            arcSmeltingInto = properties.getMaterial();
        } else {
            arcSmeltingInto.getProperties().ensureSet(EFMaterialPropertyKey.INGOT, true);
        }
        if (macerateInto.isEmpty()) {
            macerateInto = properties.getMaterial();
        } else {
            macerateInto.getProperties().ensureSet(EFMaterialPropertyKey.INGOT, true);
        }
        if (!magneticMaterial.isEmpty()) {
            magneticMaterial.getProperties().ensureSet(EFMaterialPropertyKey.INGOT, true);
        }
    }
}
