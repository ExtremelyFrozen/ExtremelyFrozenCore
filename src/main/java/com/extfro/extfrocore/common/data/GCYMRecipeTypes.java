package com.extfro.extfrocore.common.data;

import com.extfro.extfrocore.api.EFAPI;
import com.extfro.extfrocore.api.block.ICoilType;
import com.extfro.extfrocore.api.capability.recipe.IO;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.recipe.GTRecipeType;
import com.extfro.extfrocore.integration.xei.handlers.item.CycleItemEntryHandler;
import com.extfro.extfrocore.integration.xei.widgets.GTRecipeElement;
import com.extfro.extfrocore.utils.FormattingUtil;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib2.utils.LocalizationUtils;

import java.util.ArrayList;
import java.util.List;

import static com.extfro.extfrocore.common.data.GTRecipeTypes.MULTIBLOCK;
import static com.extfro.extfrocore.common.data.GTRecipeTypes.register;
import static com.lowdragmc.lowdraglib2.gui.ui.data.FillDirection.LEFT_TO_RIGHT;

public class GCYMRecipeTypes {

    //////////////////////////////////////
    // ******* Multiblock *******//
    //////////////////////////////////////
    public final static GTRecipeType ALLOY_BLAST_RECIPES = register("alloy_blast_smelter", MULTIBLOCK)
            .setMaxIOSize(9, 0, 3, 1)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSlotOverlay(false, false, false, GuiTextures.FURNACE_OVERLAY_1)
            .setSlotOverlay(false, false, true, GuiTextures.FURNACE_OVERLAY_1)
            .setSlotOverlay(false, true, false, GuiTextures.FURNACE_OVERLAY_2)
            .setSlotOverlay(false, true, true, GuiTextures.FURNACE_OVERLAY_2)
            .setSlotOverlay(true, true, false, GuiTextures.FURNACE_OVERLAY_2)
            .setSlotOverlay(true, true, true, GuiTextures.FURNACE_OVERLAY_2)
            .addDataInfo(data -> {
                int temp = data.getInt("ebf_temp");
                return LocalizationUtils.format("gtceu.recipe.temperature", FormattingUtil.formatTemperature(temp));
            })
            .addDataInfo(data -> {
                int temp = data.getInt("ebf_temp");
                ICoilType requiredCoil = ICoilType.getMinRequiredType(temp);

                if (requiredCoil != null && !requiredCoil.getMaterial().isNull()) {
                    return LocalizationUtils.format("gtceu.recipe.coil.tier",
                            I18n.get(requiredCoil.getMaterial().getUnlocalizedName()));
                }
                return "";
            })
            .setUiBuilder((recipe, widgetGroup) -> {
                int temp = recipe.data.getInt("ebf_temp");
                List<List<ItemStack>> items = new ArrayList<>();
                items.add(EFAPI.HEATING_COILS.entrySet().stream()
                        .filter(coil -> coil.getKey().getCoilTemperature() >= temp)
                        .map(coil -> new ItemStack(coil.getValue().get())).toList());
                widgetGroup.addChild(GTRecipeElement.itemSlot(
                        (int) widgetGroup.getSizeWidth() - 25,
                        (int) widgetGroup.getSizeHeight() - 40,
                        GuiTextures.SLOT,
                        "alloy_blast_coil",
                        CycleItemEntryHandler.createFromStacks(items),
                        0));
            })
            .setSound(GTSoundEntries.ARC);

    public static void init() {}
}
