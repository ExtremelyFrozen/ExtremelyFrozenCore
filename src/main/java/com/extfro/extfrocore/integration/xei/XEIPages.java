package com.extfro.extfrocore.integration.xei;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.integration.xei.circuit.CircuitDisplay;
import com.extfro.extfrocore.integration.xei.circuit.CircuitDisplayRegistry;
import com.extfro.extfrocore.integration.xei.multipage.MultiblockInfoDisplay;
import com.extfro.extfrocore.integration.xei.multipage.XEIMultiblockInfoRegistry;
import com.extfro.extfrocore.integration.xei.oreprocessing.OreProcessingDisplay;
import com.extfro.extfrocore.integration.xei.oreprocessing.OreProcessingRegistry;
import com.extfro.extfrocore.integration.xei.orevein.XEIOreVeinDisplay;
import com.extfro.extfrocore.integration.xei.orevein.XEIOreVeinRegistry;
import com.extfro.extfrocore.integration.xei.page.XEIPageDefinition;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public final class XEIPages {

    public static final XEIPageDefinition<XEIOreVeinDisplay> ORE_VEIN = XEIPageDefinition
            .builder(ExtForCore.id("ore_vein"), Component.translatable("extfrocore.xei.ore_vein"),
                    XEIOreVeinDisplay.class)
            .icon(new ItemStack(Items.RAW_IRON))
            .size(176, 96)
            .displays(XEIOreVeinRegistry::getAllDisplays)
            .build();

    public static final XEIPageDefinition<OreProcessingDisplay> ORE_PROCESSING = XEIPageDefinition
            .builder(OreProcessingRegistry.DEFAULT_CATEGORY_ID, Component.translatable("extfrocore.xei.ore_processing"),
                    OreProcessingDisplay.class)
            .icon(OreProcessingRegistry.DEFAULT_CATEGORY.icon())
            .size(OreProcessingRegistry.DEFAULT_CATEGORY.width(), OreProcessingRegistry.DEFAULT_CATEGORY.height())
            .displays(OreProcessingRegistry::getDisplays)
            .catalysts(() -> OreProcessingRegistry.getCategories().stream()
                    .flatMap(category -> category.catalysts().stream())
                    .map(ItemStack::copy)
                    .toList())
            .build();

    public static final XEIPageDefinition<CircuitDisplay> CIRCUIT = XEIPageDefinition
            .builder(ExtForCore.id("circuit"), Component.translatable("extfrocore.xei.circuit"), CircuitDisplay.class)
            .icon(new ItemStack(Items.COMPARATOR))
            .size(176, 96)
            .displays(CircuitDisplayRegistry::getDisplays)
            .catalysts(() -> CircuitDisplayRegistry.getDisplays().stream()
                    .map(CircuitDisplay::firstOutput)
                    .filter(stack -> stack != null && !stack.isEmpty())
                    .map(ItemStack::copy)
                    .toList())
            .build();

    public static final XEIPageDefinition<MultiblockInfoDisplay> MULTIBLOCK_INFO = XEIPageDefinition
            .builder(XEIMultiblockInfoRegistry.CATEGORY_ID, XEIMultiblockInfoRegistry.CATEGORY_TITLE,
                    MultiblockInfoDisplay.class)
            .icon(new ItemStack(Items.STRUCTURE_BLOCK))
            .size(176, 96)
            .displays(XEIMultiblockInfoRegistry::getAllDisplays)
            .catalysts(() -> XEIMultiblockInfoRegistry.getAllDisplays().stream()
                    .map(MultiblockInfoDisplay::icon)
                    .filter(stack -> !stack.isEmpty())
                    .map(ItemStack::copy)
                    .toList())
            .build();

    private XEIPages() {}

    public static List<XEIPageDefinition<?>> getBuiltinPages() {
        List<XEIPageDefinition<?>> pages = new ArrayList<>();
        pages.add(ORE_VEIN);
        pages.add(ORE_PROCESSING);
        pages.add(CIRCUIT);
        pages.add(MULTIBLOCK_INFO);
        return List.copyOf(pages);
    }
}
