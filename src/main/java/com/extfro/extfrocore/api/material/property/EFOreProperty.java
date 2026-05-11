package com.extfro.extfrocore.api.material.property;

import com.extfro.extfrocore.api.material.EFMaterial;

import net.minecraft.util.Mth;

import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class EFOreProperty implements EFMaterialProperty {

    @Getter
    private final List<EFMaterial> oreByProducts = new ArrayList<>();
    @Getter
    @Setter
    private int oreMultiplier;
    @Getter
    @Setter
    private int byProductMultiplier;
    @Getter
    @Setter
    private boolean emissive;
    @Getter
    @Setter
    @NotNull
    private EFMaterial directSmeltResult = EFMaterial.EMPTY;
    @Setter
    @NotNull
    private EFMaterial washedIn = EFMaterial.EMPTY;
    private int washedAmount = 100;
    @Getter
    private final List<EFMaterial> separatedInto = new ArrayList<>();

    public EFOreProperty(int oreMultiplier, int byProductMultiplier) {
        this.oreMultiplier = oreMultiplier;
        this.byProductMultiplier = byProductMultiplier;
        this.emissive = false;
    }

    public EFOreProperty(int oreMultiplier, int byProductMultiplier, boolean emissive) {
        this.oreMultiplier = oreMultiplier;
        this.byProductMultiplier = byProductMultiplier;
        this.emissive = emissive;
    }

    public EFOreProperty() {
        this(1, 1);
    }

    public void setWashedIn(EFMaterial material, int washedAmount) {
        this.washedIn = material;
        this.washedAmount = washedAmount;
    }

    public @NotNull ObjectIntPair<EFMaterial> getWashedIn() {
        return ObjectIntPair.of(this.washedIn, this.washedAmount);
    }

    public void setSeparatedInto(EFMaterial... materials) {
        this.separatedInto.addAll(Arrays.asList(materials));
    }

    public void setOreByProducts(@NotNull EFMaterial @NotNull... materials) {
        setOreByProducts(Arrays.asList(materials));
    }

    public void setOreByProducts(@NotNull Collection<@NotNull EFMaterial> materials) {
        this.oreByProducts.clear();
        this.oreByProducts.addAll(materials);
    }

    public void addOreByProducts(@NotNull EFMaterial @NotNull... materials) {
        this.oreByProducts.addAll(Arrays.asList(materials));
    }

    @NotNull
    public final EFMaterial getOreByProduct(int index) {
        if (this.oreByProducts.isEmpty()) return EFMaterial.EMPTY;
        return this.oreByProducts.get(Mth.clamp(index, 0, this.oreByProducts.size() - 1));
    }

    @NotNull
    public final EFMaterial getOreByProduct(int index, @NotNull EFMaterial fallback) {
        EFMaterial material = getOreByProduct(index);
        return !material.isEmpty() ? material : fallback;
    }

    @Override
    public void verifyProperty(EFMaterialProperties properties) {
        properties.ensureSet(EFMaterialPropertyKey.DUST, true);
        if (!directSmeltResult.isEmpty()) {
            directSmeltResult.getProperties().ensureSet(EFMaterialPropertyKey.DUST, true);
        }
        if (!washedIn.isEmpty()) {
            washedIn.getProperties().ensureSet(EFMaterialPropertyKey.FLUID, true);
        }
        separatedInto.forEach(material -> material.getProperties().ensureSet(EFMaterialPropertyKey.DUST, true));
        oreByProducts.forEach(material -> material.getProperties().ensureSet(EFMaterialPropertyKey.DUST, true));
    }
}
