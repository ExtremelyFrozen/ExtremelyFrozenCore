package com.extfro.extfrocore.api.material;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.fluid.EFFluidBuilder;
import com.extfro.extfrocore.api.fluid.EFFluidState;
import com.extfro.extfrocore.api.fluid.EFFluidStorageKey;
import com.extfro.extfrocore.api.fluid.EFFluidStorageKeys;
import com.extfro.extfrocore.api.material.info.EFMaterialFlag;
import com.extfro.extfrocore.api.material.info.EFMaterialFlags;
import com.extfro.extfrocore.api.material.info.EFMaterialIconSet;
import com.extfro.extfrocore.api.material.property.*;
import com.extfro.extfrocore.common.material.EFMaterialRegistryManager;
import com.extfro.extfrocore.common.material.EFMedicalConditions;
import com.extfro.extfrocore.utils.FormattingUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

import static com.extfro.extfrocore.api.material.property.EFMaterialPropertyKey.HAZARD;

public class EFMaterial implements Comparable<EFMaterial> {

    @ApiStatus.Internal
    public static final EFMaterial EMPTY = new EFMaterial(ExtForCore.id("empty"), "Empty", true);

    @NotNull
    @Getter
    private final MaterialInfo materialInfo;
    @NotNull
    @Getter
    private final EFMaterialProperties properties;
    @NotNull
    @Getter
    private final EFMaterialFlags flags;
    @Getter
    private String chemicalFormula;
    @Setter
    @Getter
    private List<TagKey<Item>> itemTags = new ArrayList<>();
    private final boolean empty;

    private EFMaterial(ResourceLocation resourceLocation, @Nullable String localizedName, boolean empty) {
        this.materialInfo = new MaterialInfo(resourceLocation);
        this.materialInfo.iconSet = EFMaterialIconSet.DULL;
        this.materialInfo.overriddenName = localizedName;
        this.properties = new EFMaterialProperties();
        this.flags = new EFMaterialFlags();
        this.empty = empty;
        this.properties.setMaterial(this);
        verifyMaterial();
    }

    private EFMaterial(@NotNull MaterialInfo materialInfo, @NotNull EFMaterialProperties properties,
                       @NotNull EFMaterialFlags flags) {
        this.materialInfo = materialInfo;
        this.properties = properties;
        this.flags = flags;
        this.empty = false;
        this.properties.setMaterial(this);
        verifyMaterial();
    }

    protected void registerMaterial() {
        EFMaterialRegistryManager.getInstance().getRegistry(getModId()).register(this);
    }

    public String getName() {
        return materialInfo.resourceLocation.getPath();
    }

    public String getModId() {
        return materialInfo.resourceLocation.getNamespace();
    }

    public String getModid() {
        return getModId();
    }

    public ResourceLocation getResourceLocation() {
        return materialInfo.resourceLocation;
    }

    public String getUnlocalizedName() {
        return getResourceLocation().toString();
    }

    @ApiStatus.Internal
    public String getDefaultTranslation() {
        return materialInfo.overriddenName != null ? materialInfo.overriddenName : toEnglishName(getName());
    }

    public MutableComponent getLocalizedName() {
        return Component.translatable(getUnlocalizedName());
    }

    public ImmutableList<EFMaterialStack> getMaterialComponents() {
        return materialInfo.componentList;
    }

    public EFMaterial setComponents(EFMaterialStack... components) {
        this.materialInfo.setComponents(components);
        this.chemicalFormula = calculateChemicalFormula();
        return this;
    }

    public EFMaterial setFormula(String formula) {
        return setFormula(formula, true);
    }

    public EFMaterial setFormula(String formula, boolean withFormatting) {
        this.chemicalFormula = withFormatting ? FormattingUtil.toSmallDownNumbers(formula) : formula;
        return this;
    }

    public boolean isElement() {
        return materialInfo.element != null;
    }

    @Nullable
    public EFElement getElement() {
        return materialInfo.element;
    }

    public void addFlags(EFMaterialFlag... flags) {
        if (!EFMaterialRegistryManager.getInstance().canModifyMaterials()) {
            throw new IllegalStateException("Cannot add flag to material when registry is frozen");
        }
        this.flags.addFlags(flags).verify(this);
    }

    public boolean hasFlag(EFMaterialFlag flag) {
        return flags.hasFlag(flag);
    }

    public boolean hasFlags(EFMaterialFlag... flags) {
        return Arrays.stream(flags).allMatch(this::hasFlag);
    }

    public boolean hasAnyOfFlags(EFMaterialFlag... flags) {
        return Arrays.stream(flags).anyMatch(this::hasFlag);
    }

    public boolean hasFluid() {
        return hasProperty(EFMaterialPropertyKey.FLUID);
    }

    public Fluid getFluid() {
        return getFluid(EFFluidStorageKeys.LIQUID);
    }

    public Fluid getFluid(EFFluidStorageKey key) {
        EFFluidProperty property = getProperty(EFMaterialPropertyKey.FLUID);
        if (property == null) {
            throw new IllegalArgumentException("Material " + getResourceLocation() + " does not have a Fluid");
        }
        return property.get(key);
    }

    public EFFluidBuilder getFluidBuilder() {
        return getFluidBuilder(EFFluidStorageKeys.LIQUID);
    }

    public EFFluidBuilder getFluidBuilder(EFFluidStorageKey key) {
        EFFluidProperty property = getProperty(EFMaterialPropertyKey.FLUID);
        if (property == null) {
            throw new IllegalArgumentException("Material " + getResourceLocation() + " does not have a Fluid");
        }
        return property.getQueuedBuilder(key);
    }

    public FluidStack getFluid(int amount) {
        Fluid fluid = getFluid();
        return fluid == null ? FluidStack.EMPTY : new FluidStack(fluid, amount);
    }

    public FluidStack getFluid(EFFluidStorageKey key, int amount) {
        Fluid fluid = getFluid(key);
        return fluid == null ? FluidStack.EMPTY : new FluidStack(fluid, amount);
    }

    public int getMaterialARGB() {
        return 0xFF000000 | getMaterialRGB();
    }

    public int getMaterialRGB() {
        return materialInfo.colors.getInt(0);
    }

    public int getMaterialSecondaryRGB() {
        return materialInfo.colors.getInt(1);
    }

    public int getLayerARGB(int layer) {
        if (layer < -100) {
            layer = -layer - 101;
        }
        if (layer > materialInfo.colors.size() - 1 || layer < 0) {
            return -1;
        }
        int layerColor = 0xFF000000 | materialInfo.colors.getInt(layer);
        return layerColor != -1 || layer == 0 ? layerColor : getMaterialARGB();
    }

    public int getLayerRGB(int layer) {
        if (layer > materialInfo.colors.size() - 1 || layer < 0) {
            return -1;
        }
        int layerColor = materialInfo.colors.getInt(layer);
        return layerColor != -1 || layer == 0 ? layerColor : getMaterialRGB();
    }

    public IntList getMaterialRGBs() {
        return materialInfo.colors;
    }

    public boolean hasFluidColor() {
        return materialInfo.hasFluidColor;
    }

    public EFMaterialIconSet getMaterialIconSet() {
        return materialInfo.iconSet;
    }

    public String getCamelCaseString() {
        return FormattingUtil.lowerUnderscoreToUpperCamel(getName());
    }

    public <T extends EFMaterialProperty> boolean hasProperty(EFMaterialPropertyKey<T> key) {
        return properties.hasProperty(key);
    }

    @Nullable
    public <T extends EFMaterialProperty> T getProperty(EFMaterialPropertyKey<T> key) {
        return properties.getProperty(key);
    }

    public <T extends EFMaterialProperty> void setProperty(EFMaterialPropertyKey<T> key, T property) {
        if (!EFMaterialRegistryManager.getInstance().canModifyMaterials()) {
            throw new IllegalStateException("Cannot add properties to a material when registry is frozen");
        }
        properties.setProperty(key, property);
        properties.verify();
    }

    public <T extends EFMaterialProperty> void removeProperty(EFMaterialPropertyKey<T> key) {
        properties.removeProperty(key);
        properties.verify();
    }

    public void verifyMaterial() {
        properties.verify();
        flags.verify(this);
        this.chemicalFormula = calculateChemicalFormula();
        calculateDecompositionType();
    }

    public boolean isEmpty() {
        return empty;
    }

    public boolean isNull() {
        return isEmpty();
    }

    protected void calculateDecompositionType() {
        if (!materialInfo.componentList.isEmpty() &&
                !hasFlag(EFMaterialFlags.DECOMPOSITION_BY_CENTRIFUGING) &&
                !hasFlag(EFMaterialFlags.DECOMPOSITION_BY_ELECTROLYZING) &&
                !hasFlag(EFMaterialFlags.DISABLE_DECOMPOSITION)) {
            boolean onlyMetalMaterials = true;
            for (EFMaterialStack materialStack : materialInfo.componentList) {
                onlyMetalMaterials &= materialStack.material().hasProperty(EFMaterialPropertyKey.INGOT);
            }
            flags.addFlags(onlyMetalMaterials ? EFMaterialFlags.DECOMPOSITION_BY_CENTRIFUGING :
                    EFMaterialFlags.DECOMPOSITION_BY_ELECTROLYZING);
        }
    }

    private String calculateChemicalFormula() {
        if (chemicalFormula != null) return this.chemicalFormula;
        if (materialInfo.element != null) {
            String[] split = materialInfo.element.symbol().split("-");
            if (split.length > 1) {
                split[1] = FormattingUtil.toSmallUpNumbers(split[1]);
                return split[0] + split[1];
            }
            return materialInfo.element.symbol();
        }
        if (!materialInfo.componentList.isEmpty()) {
            StringBuilder components = new StringBuilder();
            for (EFMaterialStack component : materialInfo.componentList) {
                components.append(component);
            }
            return components.toString();
        }
        return "";
    }

    @Override
    public int compareTo(@NotNull EFMaterial other) {
        return this.getResourceLocation().compareTo(other.getResourceLocation());
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof EFMaterial material &&
                Objects.equals(this.getResourceLocation(), material.getResourceLocation());
    }

    @Override
    public int hashCode() {
        return getResourceLocation().hashCode();
    }

    @Override
    public String toString() {
        return getResourceLocation().toString();
    }

    private static String toEnglishName(String name) {
        StringBuilder result = new StringBuilder(name.length());
        boolean capitalize = true;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c == '_' || c == '-' || c == '/') {
                result.append(' ');
                capitalize = true;
            } else if (capitalize) {
                result.append(Character.toUpperCase(c));
                capitalize = false;
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    public static class Builder {

        private final MaterialInfo materialInfo;
        private final EFMaterialProperties properties;
        private final EFMaterialFlags flags;
        private final List<TagKey<Item>> itemTags = new ArrayList<>();
        private String formula = null;
        private boolean formatFormula = true;
        private List<EFMaterialStack> composition = new ArrayList<>();
        private boolean averageRGB = false;
        protected EFMaterial value;

        public Builder(ResourceLocation resourceLocation) {
            String name = resourceLocation.getPath();
            if (name.charAt(name.length() - 1) == '_') {
                throw new IllegalArgumentException("Material name cannot end with a '_'");
            }
            materialInfo = new MaterialInfo(resourceLocation);
            properties = new EFMaterialProperties();
            flags = new EFMaterialFlags();
        }

        public Builder langValue(String name) {
            materialInfo.setOverriddenName(name);
            return this;
        }

        public Builder fluid() {
            return fluid(EFFluidStorageKeys.LIQUID, EFFluidState.LIQUID);
        }

        public Builder fluid(@NotNull EFFluidStorageKey key, @NotNull EFFluidState state) {
            return fluid(key, new EFFluidBuilder().state(state));
        }

        public Builder fluid(@NotNull EFFluidStorageKey key, @NotNull EFFluidBuilder builder) {
            if (!properties.hasProperty(EFMaterialPropertyKey.FLUID)) {
                properties.setProperty(EFMaterialPropertyKey.FLUID, new EFFluidProperty(key, builder));
            } else {
                properties.getProperty(EFMaterialPropertyKey.FLUID).enqueueRegistration(key, builder);
            }
            return this;
        }

        public Builder liquid() {
            return fluid(EFFluidStorageKeys.LIQUID, EFFluidState.LIQUID);
        }

        public Builder liquid(@NotNull EFFluidBuilder builder) {
            return fluid(EFFluidStorageKeys.LIQUID, builder);
        }

        public Builder liquid(int temp) {
            return liquid(new EFFluidBuilder().temperature(temp));
        }

        public Builder plasma() {
            return fluid(EFFluidStorageKeys.PLASMA, EFFluidState.PLASMA);
        }

        public Builder plasma(@NotNull EFFluidBuilder builder) {
            return fluid(EFFluidStorageKeys.PLASMA, builder.state(EFFluidState.PLASMA));
        }

        public Builder plasma(int temp) {
            return plasma(new EFFluidBuilder().temperature(temp));
        }

        public Builder gas() {
            return fluid(EFFluidStorageKeys.GAS, EFFluidState.GAS);
        }

        public Builder gas(@NotNull EFFluidBuilder builder) {
            return fluid(EFFluidStorageKeys.GAS, builder.state(EFFluidState.GAS));
        }

        public Builder gas(int temp) {
            return gas(new EFFluidBuilder().temperature(temp));
        }

        public Builder dust() {
            return dust(2, 0);
        }

        public Builder dust(int harvestLevel) {
            return dust(harvestLevel, 0);
        }

        public Builder dust(int harvestLevel, int burnTime) {
            properties.setProperty(EFMaterialPropertyKey.DUST, new EFDustProperty(harvestLevel, burnTime));
            return this;
        }

        public Builder wood() {
            return wood(0, 0);
        }

        public Builder wood(int harvestLevel) {
            return wood(harvestLevel, 0);
        }

        public Builder wood(int harvestLevel, int burnTime) {
            properties.setProperty(EFMaterialPropertyKey.WOOD, new EFWoodProperty());
            properties.setProperty(EFMaterialPropertyKey.DUST, new EFDustProperty(harvestLevel, burnTime));
            return this;
        }

        public Builder ingot() {
            return ingot(2, 0);
        }

        public Builder ingot(int harvestLevel) {
            return ingot(harvestLevel, 0);
        }

        public Builder ingot(int harvestLevel, int burnTime) {
            properties.setProperty(EFMaterialPropertyKey.INGOT, new EFIngotProperty());
            properties.setProperty(EFMaterialPropertyKey.DUST, new EFDustProperty(harvestLevel, burnTime));
            return this;
        }

        public Builder gem() {
            return gem(2, 0);
        }

        public Builder gem(int harvestLevel) {
            return gem(harvestLevel, 0);
        }

        public Builder gem(int harvestLevel, int burnTime) {
            properties.setProperty(EFMaterialPropertyKey.GEM, new EFGemProperty());
            properties.setProperty(EFMaterialPropertyKey.DUST, new EFDustProperty(harvestLevel, burnTime));
            return this;
        }

        public Builder polymer() {
            return polymer(0, 0);
        }

        public Builder polymer(int harvestLevel) {
            return polymer(harvestLevel, 0);
        }

        public Builder polymer(int harvestLevel, int burnTime) {
            properties.setProperty(EFMaterialPropertyKey.POLYMER, new EFPolymerProperty());
            return ingot(harvestLevel, burnTime);
        }

        public Builder burnTime(int burnTime) {
            properties.ensureSet(EFMaterialPropertyKey.DUST);
            properties.getProperty(EFMaterialPropertyKey.DUST).setBurnTime(burnTime);
            return this;
        }

        public Builder color(int color) {
            return color(color, true);
        }

        public Builder color(int color, boolean hasFluidColor) {
            materialInfo.colors.set(0, color);
            materialInfo.hasFluidColor = hasFluidColor;
            return this;
        }

        public Builder secondaryColor(int color) {
            materialInfo.colors.set(1, color);
            return this;
        }

        public Builder colorAverage() {
            this.averageRGB = true;
            return this;
        }

        public Builder iconSet(EFMaterialIconSet iconSet) {
            materialInfo.iconSet = iconSet;
            return this;
        }

        public Builder components(Object... components) {
            if (components.length % 2 != 0) {
                throw new IllegalArgumentException("Components must be paired material and amount values");
            }
            composition = new ArrayList<>();
            for (int i = 0; i < components.length; i += 2) {
                if (!(components[i] instanceof EFMaterial material) || !(components[i + 1] instanceof Number amount)) {
                    throw new IllegalArgumentException("Components must be pairs of EFMaterial and Number");
                }
                composition.add(new EFMaterialStack(material, amount.longValue()));
            }
            return this;
        }

        public Builder componentStacks(EFMaterialStack... components) {
            return componentStacks(ImmutableList.copyOf(components));
        }

        public Builder componentStacks(ImmutableList<EFMaterialStack> components) {
            composition = new ArrayList<>(components);
            return this;
        }

        public Builder flags(EFMaterialFlag... flags) {
            this.flags.addFlags(flags);
            return this;
        }

        public Builder appendFlags(Collection<EFMaterialFlag> f1, EFMaterialFlag... f2) {
            this.flags.addFlags(f1.toArray(EFMaterialFlag[]::new));
            this.flags.addFlags(f2);
            return this;
        }

        public Builder customTags(TagKey<Item> key) {
            this.itemTags.add(key);
            return this;
        }

        public Builder element(EFElement element) {
            materialInfo.element = element;
            return this;
        }

        public Builder formula(String formula) {
            this.formula = formula;
            return this;
        }

        public Builder formula(String formula, boolean withFormatting) {
            this.formula = formula;
            this.formatFormula = withFormatting;
            return this;
        }

        public Builder toolStats(EFToolProperty toolProperty) {
            properties.setProperty(EFMaterialPropertyKey.TOOL, toolProperty);
            return this;
        }

        public Builder armorStats(EFArmorProperty armorProperty) {
            properties.setProperty(EFMaterialPropertyKey.ARMOR, armorProperty);
            return this;
        }

        public Builder rotorStats(int power, int efficiency, float damage, int durability) {
            properties.setProperty(EFMaterialPropertyKey.ROTOR, new EFRotorProperty(power, efficiency, damage,
                    durability));
            return this;
        }

        public Builder blastTemp(int temp) {
            return blast(temp);
        }

        public Builder blastTemp(int temp, EFBlastProperty.GasTier gasTier) {
            return blast(temp, gasTier);
        }

        public Builder blastTemp(int temp, EFBlastProperty.GasTier gasTier, int eutOverride) {
            return blast(builder -> builder.temp(temp, gasTier).blastStats(eutOverride));
        }

        public Builder blastTemp(int temp, EFBlastProperty.GasTier gasTier, int eutOverride, int durationOverride) {
            return blast(builder -> builder.temp(temp, gasTier).blastStats(eutOverride, durationOverride));
        }

        public Builder blast(int temp) {
            properties.setProperty(EFMaterialPropertyKey.BLAST, new EFBlastProperty(temp));
            return this;
        }

        public Builder blast(int temp, EFBlastProperty.GasTier gasTier) {
            properties.setProperty(EFMaterialPropertyKey.BLAST, new EFBlastProperty(temp, gasTier));
            return this;
        }

        public Builder blast(UnaryOperator<EFBlastProperty.Builder> builder) {
            properties.setProperty(EFMaterialPropertyKey.BLAST, builder.apply(new EFBlastProperty.Builder()).build());
            return this;
        }

        public Builder removeHazard() {
            properties.setProperty(HAZARD, new EFHazardProperty(EFHazardProperty.HazardTrigger.NONE,
                    EFMedicalConditions.NONE, 0, false));
            return this;
        }

        public Builder radioactiveHazard(float multiplier) {
            properties.setProperty(HAZARD, new EFHazardProperty(EFHazardProperty.HazardTrigger.ANY,
                    EFMedicalConditions.CARCINOGEN, multiplier, true));
            return this;
        }

        public Builder hazard(EFHazardProperty.HazardTrigger trigger,
                              com.extfro.extfrocore.api.medical.EFMedicalCondition condition) {
            properties.setProperty(HAZARD, new EFHazardProperty(trigger, condition, 1, false));
            return this;
        }

        public Builder hazard(EFHazardProperty.HazardTrigger trigger,
                              com.extfro.extfrocore.api.medical.EFMedicalCondition condition,
                              float progressionMultiplier) {
            properties.setProperty(HAZARD, new EFHazardProperty(trigger, condition, progressionMultiplier, false));
            return this;
        }

        public Builder hazard(EFHazardProperty.HazardTrigger trigger,
                              com.extfro.extfrocore.api.medical.EFMedicalCondition condition,
                              float progressionMultiplier, boolean applyToDerivatives) {
            properties.setProperty(HAZARD, new EFHazardProperty(trigger, condition, progressionMultiplier,
                    applyToDerivatives));
            return this;
        }

        public Builder hazard(EFHazardProperty.HazardTrigger trigger,
                              com.extfro.extfrocore.api.medical.EFMedicalCondition condition,
                              boolean applyToDerivatives) {
            properties.setProperty(HAZARD, new EFHazardProperty(trigger, condition, 1, applyToDerivatives));
            return this;
        }

        public Builder ore() {
            properties.ensureSet(EFMaterialPropertyKey.ORE);
            return this;
        }

        public Builder ore(boolean emissive) {
            properties.setProperty(EFMaterialPropertyKey.ORE, new EFOreProperty(1, 1, emissive));
            return this;
        }

        public Builder ore(int oreMultiplier, int byproductMultiplier) {
            properties.setProperty(EFMaterialPropertyKey.ORE, new EFOreProperty(oreMultiplier, byproductMultiplier));
            return this;
        }

        public Builder ore(int oreMultiplier, int byproductMultiplier, boolean emissive) {
            properties.setProperty(EFMaterialPropertyKey.ORE,
                    new EFOreProperty(oreMultiplier, byproductMultiplier, emissive));
            return this;
        }

        public Builder washedIn(EFMaterial material) {
            return washedIn(material, 100);
        }

        public Builder washedIn(EFMaterial material, int washedAmount) {
            properties.ensureSet(EFMaterialPropertyKey.ORE);
            properties.getProperty(EFMaterialPropertyKey.ORE).setWashedIn(material, washedAmount);
            return this;
        }

        public Builder separatedInto(EFMaterial... materials) {
            properties.ensureSet(EFMaterialPropertyKey.ORE);
            properties.getProperty(EFMaterialPropertyKey.ORE).setSeparatedInto(materials);
            return this;
        }

        public Builder oreSmeltInto(EFMaterial material) {
            properties.ensureSet(EFMaterialPropertyKey.ORE);
            properties.getProperty(EFMaterialPropertyKey.ORE).setDirectSmeltResult(material);
            return this;
        }

        public Builder polarizesInto(EFMaterial material) {
            properties.ensureSet(EFMaterialPropertyKey.INGOT);
            properties.getProperty(EFMaterialPropertyKey.INGOT).setMagneticMaterial(material);
            return this;
        }

        public Builder arcSmeltInto(EFMaterial material) {
            properties.ensureSet(EFMaterialPropertyKey.INGOT);
            properties.getProperty(EFMaterialPropertyKey.INGOT).setArcSmeltingInto(material);
            return this;
        }

        public Builder macerateInto(EFMaterial material) {
            properties.ensureSet(EFMaterialPropertyKey.INGOT);
            properties.getProperty(EFMaterialPropertyKey.INGOT).setMacerateInto(material);
            return this;
        }

        public Builder ingotSmeltInto(EFMaterial material) {
            properties.ensureSet(EFMaterialPropertyKey.INGOT);
            properties.getProperty(EFMaterialPropertyKey.INGOT).setSmeltingInto(material);
            return this;
        }

        public Builder addOreByproducts(EFMaterial... byproducts) {
            properties.ensureSet(EFMaterialPropertyKey.ORE);
            properties.getProperty(EFMaterialPropertyKey.ORE).addOreByProducts(byproducts);
            return this;
        }

        public Builder cableProperties(long voltage, int amperage, int loss) {
            return cableProperties(voltage, amperage, loss, false);
        }

        public Builder cableProperties(long voltage, int amperage, int loss, boolean isSuperCon) {
            properties.setProperty(EFMaterialPropertyKey.WIRE, new EFWireProperties(voltage, amperage, loss,
                    isSuperCon));
            return this;
        }

        public Builder cableProperties(long voltage, int amperage, int loss, boolean isSuperCon,
                                       int criticalTemperature) {
            properties.setProperty(EFMaterialPropertyKey.WIRE, new EFWireProperties(voltage, amperage, loss,
                    isSuperCon, criticalTemperature));
            return this;
        }

        public Builder fluidPipeProperties(int maxTemp, int throughput, boolean gasProof) {
            return fluidPipeProperties(maxTemp, throughput, gasProof, false, false, false);
        }

        public Builder fluidPipeProperties(int maxTemp, int throughput, boolean gasProof, boolean acidProof,
                                           boolean cryoProof, boolean plasmaProof) {
            properties.setProperty(EFMaterialPropertyKey.FLUID_PIPE,
                    new EFFluidPipeProperties(maxTemp, throughput, gasProof, acidProof, cryoProof, plasmaProof));
            return this;
        }

        public Builder itemPipeProperties(int priority, float stacksPerSec) {
            properties.setProperty(EFMaterialPropertyKey.ITEM_PIPE, new EFItemPipeProperties(priority, stacksPerSec));
            return this;
        }

        public Builder addDefaultEnchant(Enchantment enchantment, int level) {
            properties.ensureSet(EFMaterialPropertyKey.TOOL);
            properties.getProperty(EFMaterialPropertyKey.TOOL).addEnchantmentForTools(enchantment, level);
            return this;
        }

        protected EFMaterial buildAndRegister() {
            materialInfo.componentList = ImmutableList.copyOf(composition);
            if (!properties.hasProperty(HAZARD)) {
                for (EFMaterialStack materialStack : materialInfo.componentList) {
                    EFMaterial material = materialStack.material();
                    EFHazardProperty hazard = material.getProperty(HAZARD);
                    if (hazard != null && hazard.applyToDerivatives) {
                        properties.setProperty(HAZARD, hazard);
                        break;
                    }
                }
            }
            if (properties.hasProperty(HAZARD) &&
                    properties.getProperty(HAZARD).hazardTrigger == EFHazardProperty.HazardTrigger.NONE) {
                properties.removeProperty(HAZARD);
            }

            EFMaterial material = new EFMaterial(materialInfo, properties, flags);
            if (!itemTags.isEmpty()) {
                material.setItemTags(itemTags);
            }
            if (formula != null) {
                material.setFormula(formula, formatFormula);
            }
            materialInfo.verifyInfo(properties, averageRGB);
            material.registerMaterial();
            return material;
        }

        public EFMaterial register() {
            return value = buildAndRegister();
        }

        @Nullable
        public EFMaterial get() {
            return value;
        }
    }

    @Accessors(chain = true)
    private static class MaterialInfo {

        private final ResourceLocation resourceLocation;
        @Setter
        @Getter
        private String overriddenName;
        @Getter
        @Setter
        private IntList colors = IntArrayList.of(-1, -1);
        @Getter
        @Setter
        private boolean hasFluidColor = true;
        @Getter
        @Setter
        private EFMaterialIconSet iconSet = EFMaterialIconSet.DULL;
        @Getter
        @Setter
        private ImmutableList<EFMaterialStack> componentList = ImmutableList.of();
        @Getter
        @Setter
        private EFElement element;

        private MaterialInfo(ResourceLocation resourceLocation) {
            this.resourceLocation = resourceLocation;
        }

        private void setComponents(EFMaterialStack... components) {
            this.componentList = ImmutableList.copyOf(components);
        }

        private void verifyInfo(EFMaterialProperties properties, boolean averageRGB) {
            if (colors.getInt(0) == -1) {
                colors.set(0, averageRGB && !componentList.isEmpty() ? calculateAverageColor() : 0xFFFFFF);
            }
            if (colors.getInt(1) == -1) {
                colors.set(1, colors.getInt(0));
            }
            if (iconSet == null) {
                iconSet = properties.hasProperty(EFMaterialPropertyKey.FLUID) ? EFMaterialIconSet.FLUID :
                        EFMaterialIconSet.DULL;
            }
        }

        private int calculateAverageColor() {
            int red = 0;
            int green = 0;
            int blue = 0;
            int count = 0;
            for (EFMaterialStack stack : componentList) {
                int color = stack.material().getMaterialRGB();
                int amount = (int) Math.max(1, stack.amount());
                red += ((color >> 16) & 0xFF) * amount;
                green += ((color >> 8) & 0xFF) * amount;
                blue += (color & 0xFF) * amount;
                count += amount;
            }
            if (count == 0) return 0xFFFFFF;
            return ((red / count) << 16) | ((green / count) << 8) | (blue / count);
        }
    }
}
