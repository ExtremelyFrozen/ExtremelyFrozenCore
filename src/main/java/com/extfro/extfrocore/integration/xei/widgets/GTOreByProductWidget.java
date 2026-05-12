package com.extfro.extfrocore.integration.xei.widgets;

import com.extfro.extfrocore.api.data.chemical.material.Material;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.recipe.content.Content;
import com.extfro.extfrocore.api.transfer.fluid.CustomFluidTank;
import com.extfro.extfrocore.api.transfer.item.CustomItemStackHandler;
import com.extfro.extfrocore.integration.xei.entry.fluid.FluidEntryList;
import com.extfro.extfrocore.integration.xei.entry.item.ItemEntryList;
import com.extfro.extfrocore.integration.xei.handlers.fluid.CycleFluidEntryHandler;
import com.extfro.extfrocore.integration.xei.handlers.item.CycleItemEntryHandler;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.event.HoverTooltips;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.integration.xei.IngredientIO;
import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import it.unimi.dsi.fastutil.ints.IntImmutableList;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.util.List;
import java.util.stream.Stream;

public class GTOreByProductWidget extends UIElement {

    // XY positions of every item and fluid, in three enormous lists
    protected final static IntImmutableList ITEM_INPUT_LOCATIONS = IntImmutableList.of(
            3, 3,       // ore
            23, 3,      // furnace (direct smelt)
            3, 24,      // macerator (ore -> crushed)
            23, 71,     // macerator (crushed -> impure)
            50, 80,     // centrifuge (impure -> dust)
            24, 25,     // ore washer
            97, 71,     // thermal centrifuge
            70, 80,     // macerator (centrifuged -> dust)
            114, 48,    // macerator (crushed purified -> purified)
            133, 71,    // centrifuge (purified -> dust)
            3, 123,     // cauldron / simple washer (crushed)
            41, 145,    // cauldron (impure)
            102, 145,   // cauldron (purified)
            24, 48,     // chem bath
            155, 71,    // electro separator
            101, 25     // sifter
    );

    protected final static IntImmutableList ITEM_OUTPUT_LOCATIONS = IntImmutableList.of(
            46, 3,      // smelt result: 0
            3, 47,      // ore -> crushed: 2
            3, 65,      // byproduct: 4
            23, 92,     // crushed -> impure: 6
            23, 110,    // byproduct: 8
            50, 101,    // impure -> dust: 10
            50, 119,    // byproduct: 12
            64, 25,     // crushed -> crushed purified (wash): 14
            82, 25,     // byproduct: 16
            97, 92,     // crushed/crushed purified -> centrifuged: 18
            97, 110,    // byproduct: 20
            70, 101,    // centrifuged -> dust: 22
            70, 119,    // byproduct: 24
            137, 47,    // crushed purified -> purified: 26
            155, 47,    // byproduct: 28
            133, 92,    // purified -> dust: 30
            133, 110,   // byproduct: 32
            3, 105,     // crushed cauldron: 34
            3, 145,     // -> purified crushed: 36
            23, 145,    // impure cauldron: 38
            63, 145,    // -> dust: 40
            84, 145,    // purified cauldron: 42
            124, 145,   // -> dust: 44
            64, 48,     // crushed -> crushed purified (chem bath): 46
            82, 48,     // byproduct: 48
            155, 92,    // purified -> dust (electro separator): 50
            155, 110,   // byproduct 1: 52
            155, 128,   // byproduct 2: 54
            119, 3,     // sifter outputs... : 56
            137, 3,     // 58
            155, 3,     // 60
            119, 21,    // 62
            137, 21,    // 64
            155, 21     // 66
    );

    protected final static IntImmutableList FLUID_LOCATIONS = IntImmutableList.of(
            42, 25, // washer in
            42, 48  // chem bath in
    );

    // Used to set intermediates as both input and output
    protected final static IntSet FINAL_OUTPUT_INDICES = IntSet.of(
            0, 4, 8, 10, 12, 16, 20, 22, 24, 28, 30, 32, 40, 44, 48, 50, 52, 54, 56, 58, 60, 62, 64, 66);

    public GTOreByProductWidget(Material material) {
        layout(layout -> layout.width(176).height(166));
        setRecipe(new GTOreByProduct(material));
    }

    public void setRecipe(GTOreByProduct recipeWrapper) {
        BooleanList itemOutputExists = new BooleanArrayList();

        // only draw slot on inputs if it is the ore
        addImage(ITEM_INPUT_LOCATIONS.getInt(0), ITEM_INPUT_LOCATIONS.getInt(1), 18, 18, GuiTextures.SLOT);
        boolean hasSifter = recipeWrapper.hasSifter();

        addImage(0, 0, 176, 166, GuiTextures.OREBY_BASE);
        if (recipeWrapper.hasDirectSmelt()) {
            addImage(0, 0, 176, 166, GuiTextures.OREBY_SMELT);
        }
        if (recipeWrapper.hasChemBath()) {
            addImage(0, 0, 176, 166, GuiTextures.OREBY_CHEM);
        }
        if (recipeWrapper.hasSeparator()) {
            addImage(0, 0, 176, 166, GuiTextures.OREBY_SEP);
        }
        if (hasSifter) {
            addImage(0, 0, 176, 166, GuiTextures.OREBY_SIFT);
        }

        List<ItemEntryList> itemInputs = recipeWrapper.itemInputs;
        CycleItemEntryHandler itemInputsHandler = new CycleItemEntryHandler(itemInputs);
        UIElement itemStackGroup = new UIElement().layout(layout -> layout.width(176).height(166));
        for (int i = 0; i < ITEM_INPUT_LOCATIONS.size(); i += 2) {
            final int finalI = i;
            var slot = GTRecipeElement.itemSlot(ITEM_INPUT_LOCATIONS.getInt(i), ITEM_INPUT_LOCATIONS.getInt(i + 1),
                    IGuiTexture.EMPTY, "ore_by_input_" + i / 2, itemInputsHandler, i / 2)
                    .xeiRecipeSlot(IngredientIO.INPUT, 1, 0, itemStackStream(itemInputsHandler, i / 2));
            slot.addEventListener(UIEvents.HOVER_TOOLTIPS, event -> {
                var tooltips = slot.getFullTooltipTexts();
                recipeWrapper.getTooltip(finalI / 2, tooltips);
                event.hoverTooltips = new HoverTooltips(tooltips, null, null, slot.getValue());
            });
            itemStackGroup.addChild(slot);
        }

        NonNullList<ItemStack> itemOutputs = recipeWrapper.itemOutputs;
        CustomItemStackHandler itemOutputsHandler = new CustomItemStackHandler(itemOutputs);
        for (int i = 0; i < ITEM_OUTPUT_LOCATIONS.size(); i += 2) {
            int slotIndex = i / 2;
            float xeiChance = 1.0f;
            Content chance = recipeWrapper.getChance(i / 2 + itemInputs.size());
            IGuiTexture overlay = null;
            if (chance != null) {
                xeiChance = (float) chance.chance / chance.maxChance;
                overlay = chance.createOverlay(false, 0, 0, null);
            }
            if (itemOutputs.get(slotIndex).isEmpty()) {
                itemOutputExists.add(false);
                continue;
            }

            var slot = GTRecipeElement.itemSlot(ITEM_OUTPUT_LOCATIONS.getInt(i), ITEM_OUTPUT_LOCATIONS.getInt(i + 1),
                    IGuiTexture.EMPTY, "ore_by_output_" + slotIndex)
                    .bind(itemOutputsHandler, slotIndex)
                    .xeiRecipeSlot(FINAL_OUTPUT_INDICES.contains(i) ? IngredientIO.OUTPUT : IngredientIO.CATALYST,
                            xeiChance, 0, Stream.of(itemOutputs.get(slotIndex)));
            if (overlay != null) {
                slot.style(style -> style.overlay(overlay));
            }
            slot.addEventListener(UIEvents.HOVER_TOOLTIPS, event -> {
                var tooltips = slot.getFullTooltipTexts();
                recipeWrapper.getTooltip(slotIndex + itemInputs.size(), tooltips);
                event.hoverTooltips = new HoverTooltips(tooltips, null, null, slot.getValue());
            });
            itemStackGroup.addChild(slot);
            itemOutputExists.add(true);
        }

        List<FluidEntryList> fluidInputs = recipeWrapper.fluidInputs;
        CycleFluidEntryHandler fluidInputsHandler = new CycleFluidEntryHandler(fluidInputs);
        UIElement fluidStackGroup = new UIElement().layout(layout -> layout.width(176).height(166));
        for (int i = 0; i < FLUID_LOCATIONS.size(); i += 2) {
            int slotIndex = i / 2;
            if (!fluidInputs.get(slotIndex).isEmpty()) {
                var tank = GTRecipeElement.fluidSlot(FLUID_LOCATIONS.getInt(i), FLUID_LOCATIONS.getInt(i + 1),
                        GuiTextures.FLUID_SLOT, "ore_by_fluid_" + slotIndex)
                        .bind(new CustomFluidTank(fluidInputsHandler.getFluidInTank(slotIndex)), 0)
                        .xeiRecipeSlot(IngredientIO.INPUT, 1, 0, fluidStackStream(fluidInputsHandler, slotIndex));
                tank.amountLabel.disabled();
                fluidStackGroup.addChild(tank);
            }
        }

        this.addChild(itemStackGroup);
        this.addChild(fluidStackGroup);

        for (int i = 0; i < ITEM_OUTPUT_LOCATIONS.size(); i += 2) {
            // stupid hack to show all sifter slots if the first one exists
            if (itemOutputExists.getBoolean(i / 2) || (i > 28 * 2 && itemOutputExists.getBoolean(28) && hasSifter)) {
                addChildAt(GTRecipeElement.fixed(ITEM_OUTPUT_LOCATIONS.getInt(i), ITEM_OUTPUT_LOCATIONS.getInt(i + 1),
                        18, 18).style(style -> style.background(GuiTextures.SLOT)), getChildren().size() - 2);
            }
        }
    }

    private void addImage(int x, int y, int width, int height, IGuiTexture texture) {
        addChild(GTRecipeElement.fixed(x, y, width, height).style(style -> style.background(texture)));
    }

    private Stream<ItemStack> itemStackStream(CycleItemEntryHandler handler, int index) {
        var entry = handler.getEntry(index);
        return entry == null ? Stream.empty() : entry.getStacks().stream();
    }

    private Stream<FluidStack> fluidStackStream(CycleFluidEntryHandler handler, int index) {
        var entry = handler.getEntry(index);
        return entry == null ? Stream.empty() : entry.getStacks().stream();
    }
}
