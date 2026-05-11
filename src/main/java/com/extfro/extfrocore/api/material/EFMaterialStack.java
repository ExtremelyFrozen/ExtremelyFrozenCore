package com.extfro.extfrocore.api.material;

import com.extfro.extfrocore.common.material.EFMaterialRegistryManager;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.WeakHashMap;

public record EFMaterialStack(@NotNull EFMaterial material, long amount) {

    public static final EFMaterialStack EMPTY = new EFMaterialStack(EFMaterial.EMPTY, 0);

    private static final Map<String, EFMaterialStack> PARSE_CACHE = new WeakHashMap<>();

    public EFMaterialStack copy() {
        return isEmpty() ? EMPTY : new EFMaterialStack(material, amount);
    }

    public EFMaterialStack add(long amount) {
        return new EFMaterialStack(material, this.amount + amount);
    }

    public EFMaterialStack multiply(long amount) {
        return new EFMaterialStack(material, this.amount * amount);
    }

    public EFMaterialStack multiply(float amount) {
        return new EFMaterialStack(material, (long) (this.amount * amount));
    }

    public EFMaterialStack divide(long amount) {
        return new EFMaterialStack(material, this.amount / amount);
    }

    public static EFMaterialStack fromString(CharSequence value) {
        String trimmed = value.toString().trim();
        EFMaterialStack cached = PARSE_CACHE.get(trimmed);
        if (cached != null) {
            return cached;
        }

        int count = 1;
        String materialName = trimmed;
        int spaceIndex = materialName.indexOf(' ');
        if (spaceIndex >= 2 && materialName.indexOf('x') == spaceIndex - 1) {
            count = Integer.parseInt(materialName.substring(0, spaceIndex - 1));
            materialName = materialName.substring(spaceIndex + 1);
        }

        EFMaterial material = EFMaterialRegistryManager.getInstance().getMaterial(materialName);
        cached = material == null ? EMPTY : new EFMaterialStack(material, count);
        PARSE_CACHE.put(trimmed, cached);
        return cached;
    }

    public boolean isEmpty() {
        return this.material.isEmpty() || this.amount < 1;
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "";
        }
        String formula = material.getChemicalFormula();
        String result = formula == null || formula.isEmpty() ? "?" : formula;
        return amount > 1 ? amount + "x " + result : result;
    }
}
