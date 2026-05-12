package com.extfro.extfrocore.integration.xei.widgets;

import com.extfro.extfrocore.api.data.DimensionMarker;
import com.extfro.extfrocore.api.data.chemical.ChemicalHelper;
import com.extfro.extfrocore.api.data.tag.TagPrefix;
import com.extfro.extfrocore.api.data.worldgen.GTOreDefinition;
import com.extfro.extfrocore.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.extfro.extfrocore.api.data.worldgen.bedrockore.BedrockOreDefinition;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.registry.GTRegistries;
import com.extfro.extfrocore.api.transfer.fluid.CustomFluidTank;
import com.extfro.extfrocore.api.transfer.item.CustomItemStackHandler;
import com.extfro.extfrocore.common.recipe.condition.DimensionCondition;
import com.extfro.extfrocore.config.ConfigHolder;

import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import com.lowdragmc.lowdraglib2.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.event.HoverTooltips;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.integration.xei.IngredientIO;
import com.lowdragmc.lowdraglib2.utils.LocalizationUtils;
import it.unimi.dsi.fastutil.ints.IntList;
import lombok.Getter;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Getter
public class GTOreVeinWidget extends UIElement {

    private final String translationKey;
    private final int weight;
    private final String range;
    private final Set<ResourceKey<Level>> dimensionFilter;
    public final static int width = 120;

    public GTOreVeinWidget(Holder<GTOreDefinition> ore) {
        layout(layout -> layout.width(width).height(160));
        this.translationKey = getOreName(ore);
        this.weight = ore.value().getWeight();
        this.dimensionFilter = ore.value().getDimensionFilter();
        this.range = range(ore.value());
        setupBaseGui(ore.value());
        setupText(ore.value());
    }

    public GTOreVeinWidget(Holder<BedrockFluidDefinition> fluid, Object marker) {
        layout(layout -> layout.width(width).height(140));
        this.translationKey = getFluidName(fluid);
        this.weight = fluid.value().getWeight();
        this.dimensionFilter = fluid.value().getDimensionFilter();
        this.range = "NULL";
        setupBaseGui(fluid.value());
        setupText(fluid.value());
    }

    public GTOreVeinWidget(Holder<BedrockOreDefinition> bedrockOre, Void marker) {
        layout(layout -> layout.width(width).height(140));
        this.translationKey = getBedrockOreName(bedrockOre);
        this.weight = bedrockOre.value().getWeight();
        this.dimensionFilter = bedrockOre.value().getDimensionFilter();
        this.range = "NULL";
        setupBaseGui(bedrockOre.value());
        setupText(bedrockOre.value());
    }

    @SuppressWarnings("all")
    private String range(GTOreDefinition oreDefinition) {
        HeightProvider height = oreDefinition.getHeightRange().height;
        int minHeight = 0, maxHeight = 0;
        if (height instanceof UniformHeight uniform) {
            minHeight = uniform.minInclusive.resolveY(null);
            maxHeight = uniform.maxInclusive.resolveY(null);
        }
        return String.format("%d - %d", minHeight, maxHeight);
    }

    private void setupBaseGui(GTOreDefinition oreDefinition) {
        NonNullList<ItemStack> containedOresAsItemStacks = NonNullList.create();
        IntList chances = oreDefinition.getVeinGenerator().getAllChances();
        containedOresAsItemStacks.addAll(getRawMaterialList(oreDefinition));
        int n = containedOresAsItemStacks.size();
        int x = (width - 18 * n) / 2;
        for (int i = 0; i < n; i++) {
            var handler = new CustomItemStackHandler(containedOresAsItemStacks);
            var oreSlot = GTRecipeElement.itemSlot(x, 18, GuiTextures.SLOT, "ore_" + i)
                    .bind(handler, i)
                    .xeiRecipeSlot(IngredientIO.OUTPUT, 1, 0, Stream.of(containedOresAsItemStacks.get(i)));
            int finalIndex = i;
            oreSlot.addEventListener(UIEvents.HOVER_TOOLTIPS, event -> {
                var tooltips = oreSlot.getFullTooltipTexts();
                tooltips.add(Component.translatable("gtceu.jei.ore_vein_diagram.chance", chances.getInt(finalIndex)));
                event.hoverTooltips = new HoverTooltips(tooltips, null, null, oreSlot.getValue());
            });
            addChild(oreSlot);
            x += 18;
        }
    }

    private void setupBaseGui(BedrockFluidDefinition fluid) {
        Fluid storedFluid = fluid.getStoredFluid();
        var tank = new CustomFluidTank(new FluidStack(storedFluid, 1000));
        addChild(GTRecipeElement.fluidSlot(51, 18, GuiTextures.FLUID_SLOT, "bedrock_fluid")
                .bind(tank, 0)
                .xeiRecipeSlot(IngredientIO.OUTPUT, 1, 0, Stream.of(new FluidStack(storedFluid, 1000))));
    }

    private void setupBaseGui(BedrockOreDefinition bedrockOreDefinition) {
        NonNullList<ItemStack> containedOresAsItemStacks = NonNullList.create();
        IntList chances = bedrockOreDefinition.getAllChances();
        containedOresAsItemStacks.addAll(getRawMaterialList(bedrockOreDefinition));
        int n = containedOresAsItemStacks.size();
        int x = (width - 18 * n) / 2;
        for (int i = 0; i < n; i++) {
            var handler = new CustomItemStackHandler(containedOresAsItemStacks);
            var oreSlot = GTRecipeElement.itemSlot(x, 18, GuiTextures.SLOT, "bedrock_ore_" + i)
                    .bind(handler, i)
                    .xeiRecipeSlot(IngredientIO.OUTPUT, 1, 0, Stream.of(containedOresAsItemStacks.get(i)));
            int finalIndex = i;
            oreSlot.addEventListener(UIEvents.HOVER_TOOLTIPS, event -> {
                var tooltips = oreSlot.getFullTooltipTexts();
                tooltips.add(Component.translatable("gtceu.jei.ore_vein_diagram.chance", chances.getInt(finalIndex)));
                event.hoverTooltips = new HoverTooltips(tooltips, null, null, oreSlot.getValue());
            });
            addChild(oreSlot);
            x += 18;
        }
    }

    private void setupText(GTOreDefinition ignored) {
        addTextTexture(5, 0, width - 10, 16, translationKey);
        addChild(GTRecipeElement.label(5, 40,
                Component.literal(LocalizationUtils.format("gtceu.jei.ore_vein_diagram.spawn_range"))));
        addChild(GTRecipeElement.label(5, 50, Component.literal(range)));

        addChild(GTRecipeElement.label(5, 60,
                Component.literal(LocalizationUtils.format("gtceu.jei.ore_vein_diagram.weight", weight))));
        addChild(GTRecipeElement.label(5, 70,
                Component.literal(LocalizationUtils.format("gtceu.jei.ore_vein_diagram.dimensions"))));
        setupDimensionMarker(80);
    }

    private void setupText(BedrockFluidDefinition ignored) {
        addTextTexture(5, 0, width - 10, 16, translationKey);
        addChild(GTRecipeElement.label(5, 40,
                Component.literal(LocalizationUtils.format("gtceu.jei.ore_vein_diagram.weight", weight))));
        addChild(GTRecipeElement.label(5, 50,
                Component.literal(LocalizationUtils.format("gtceu.jei.ore_vein_diagram.dimensions"))));
        setupDimensionMarker(60);
    }

    private void setupText(BedrockOreDefinition ignored) {
        addTextTexture(5, 0, width - 10, 16, translationKey);
        addChild(GTRecipeElement.label(5, 40,
                Component.literal(LocalizationUtils.format("gtceu.jei.ore_vein_diagram.weight", weight))));
        addChild(GTRecipeElement.label(5, 50,
                Component.literal(LocalizationUtils.format("gtceu.jei.ore_vein_diagram.dimensions"))));
        setupDimensionMarker(60);
    }

    private void addTextTexture(int x, int y, int width, int height, String key) {
        addChild(GTRecipeElement.fixed(x, y, width, height).style(style -> style.background(
                new TextTexture(key).setType(TextTexture.TextType.LEFT_ROLL).setWidth(width))));
    }

    private void setupDimensionMarker(int yPosition) {
        if (this.dimensionFilter != null) {
            int interval = 2;
            int rowSlots = (width - 10 + interval) / (16 + interval);

            DimensionMarker[] dimMarkers = dimensionFilter.stream()
                    .map(dimension -> GTRegistries.DIMENSION_MARKERS.getOptional(dimension.location())
                            .orElseGet(() -> new DimensionMarker(DimensionMarker.MAX_TIER, () -> Blocks.BARRIER,
                                    DimensionCondition.getDimensionName(dimension))))
                    .sorted(Comparator.comparingInt(marker -> marker.tier))
                    .toArray(DimensionMarker[]::new);
            var handler = new CustomItemStackHandler(dimMarkers.length);
            for (int i = 0; i < dimMarkers.length; i++) {
                var dimMarker = dimMarkers[i];
                var icon = dimMarker.getIcon();
                int row = Math.floorDiv(i, rowSlots);
                handler.setStackInSlot(i, icon);
                IGuiTexture background = IGuiTexture.EMPTY;
                if (ConfigHolder.INSTANCE.compat.showDimensionTier) {
                    background = new GuiTextureGroup(IGuiTexture.EMPTY,
                            new TextTexture("T" + (dimMarker.tier >= DimensionMarker.MAX_TIER ? "?" : dimMarker.tier))
                                    .scale(0.75F)
                                    .transform(-3F, 5F));
                }
                addChild(GTRecipeElement.itemSlot(
                        5 + (16 + interval) * (i - row * rowSlots),
                        yPosition + 18 * row,
                        background,
                        "dimension_" + i)
                        .bind(handler, i)
                        .xeiRecipeSlot(IngredientIO.CATALYST, 1, 0, Stream.of(icon)));
            }
        } else {
            addChild(GTRecipeElement.label(5, yPosition, Component.literal("Any")));
        }
    }

    public static List<ItemStack> getContainedOresAndBlocks(GTOreDefinition oreDefinition) {
        return oreDefinition.getVeinGenerator().getAllEntries().stream()
                .flatMap(entry -> entry.map(state -> Stream.of(state.getBlock().asItem().getDefaultInstance()),
                        material -> {
                            Set<ItemStack> ores = new HashSet<>();
                            ores.add(ChemicalHelper.get(TagPrefix.rawOre, material));
                            for (TagPrefix prefix : TagPrefix.ORES.keySet()) {
                                ores.add(ChemicalHelper.get(prefix, material));
                            }
                            return ores.stream();
                        }))
                .toList();
    }

    public static List<ItemStack> getRawMaterialList(GTOreDefinition oreDefinition) {
        return oreDefinition.getVeinGenerator().getAllEntries().stream()
                .map(entry -> entry.map(state -> state.getBlock().asItem().getDefaultInstance(),
                        material -> ChemicalHelper.get(TagPrefix.rawOre, material)))
                .toList();
    }

    public static List<ItemStack> getRawMaterialList(BedrockOreDefinition bedrockOreDefinition) {
        return bedrockOreDefinition.getMaterials().stream()
                .map(entry -> ChemicalHelper.get(TagPrefix.rawOre, entry.material()))
                .toList();
    }

    public static String getOreName(Holder<GTOreDefinition> ore) {
        return ore.getKey().location().toLanguageKey("ore_vein");
    }

    public static String getFluidName(Holder<BedrockFluidDefinition> fluid) {
        return fluid.getKey().location().toLanguageKey("bedrock_fluid");
    }

    public static String getBedrockOreName(Holder<BedrockOreDefinition> bedrockOre) {
        return bedrockOre.getKey().location().toLanguageKey("bedrock_ore");
    }
}
