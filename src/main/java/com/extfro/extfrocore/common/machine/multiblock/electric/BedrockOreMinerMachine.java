package com.extfro.extfrocore.common.machine.multiblock.electric;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.EFValues;
import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.capability.IEnergyContainer;
import com.extfro.extfrocore.api.capability.recipe.EURecipeCapability;
import com.extfro.extfrocore.api.capability.recipe.IO;
import com.extfro.extfrocore.api.data.tag.TagPrefix;
import com.extfro.extfrocore.api.data.worldgen.bedrockore.WeightedMaterial;
import com.extfro.extfrocore.api.machine.feature.ITieredMachine;
import com.extfro.extfrocore.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.extfro.extfrocore.api.misc.EnergyContainerList;
import com.extfro.extfrocore.common.data.GTBlocks;
import com.extfro.extfrocore.common.data.GTMaterialBlocks;
import com.extfro.extfrocore.common.data.GTMaterials;
import com.extfro.extfrocore.common.machine.trait.BedrockOreMinerLogic;
import com.extfro.extfrocore.common.machine.trait.FluidDrillLogic;
import com.extfro.extfrocore.utils.FormattingUtil;
import com.extfro.extfrocore.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import lombok.Getter;

import java.util.List;

public class BedrockOreMinerMachine extends WorkableElectricMultiblockMachine implements ITieredMachine {

    @Getter
    private final int tier;

    public BedrockOreMinerMachine(BlockEntityCreationInfo info, int tier) {
        super(info, new BedrockOreMinerLogic());
        this.tier = tier;
    }

    @Override
    public BedrockOreMinerLogic getRecipeLogic() {
        return (BedrockOreMinerLogic) super.getRecipeLogic();
    }

    public int getEnergyTier() {
        var energyContainers = this.getCapabilitiesFlat(IO.IN, EURecipeCapability.CAP);
        if (energyContainers.isEmpty()) return this.tier;
        var energyCont = new EnergyContainerList(energyContainers.stream().filter(IEnergyContainer.class::isInstance)
                .map(IEnergyContainer.class::cast).toList());
        return Math.min(this.tier + 1, Math.max(this.tier, GTUtil.getFloorTierByVoltage(energyCont.getInputVoltage())));
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        if (isFormed()) {
            int energyContainer = getEnergyTier();
            long maxVoltage = EFValues.V[energyContainer];
            String voltageName = EFValues.VNF[energyContainer];
            textList.add(Component.translatable("gtceu.multiblock.max_energy_per_tick", maxVoltage, voltageName));

            if (getRecipeLogic().getVeinMaterials() != null) {
                // Ore names
                textList.add(Component.translatable("gtceu.multiblock.ore_rig.drilled_ores_list")
                        .withStyle(ChatFormatting.GREEN));
                List<WeightedMaterial> drilledOres = getRecipeLogic().getVeinMaterials();
                for (var entry : drilledOres) {
                    Component fluidInfo = entry.material().getLocalizedName().withStyle(ChatFormatting.GREEN);
                    textList.add(Component.translatable("gtceu.multiblock.ore_rig.drilled_ore_entry", fluidInfo)
                            .withStyle(ChatFormatting.GRAY));
                }

                // Ore amount
                float produced = getRecipeLogic().getOreToProduce() * getLevel().tickRateManager().tickrate();
                produced = Mth.floor(produced / FluidDrillLogic.MAX_PROGRESS);
                Component amountInfo = Component.literal(FormattingUtil.formatNumbers(produced) + "/s")
                        .withStyle(ChatFormatting.BLUE);
                textList.add(Component.translatable("gtceu.multiblock.ore_rig.ore_amount", amountInfo)
                        .withStyle(ChatFormatting.GRAY));
            } else {
                Component noOre = Component.translatable("gtceu.multiblock.fluid_rig.no_fluid_in_area")
                        .withStyle(ChatFormatting.RED);
                textList.add(Component.translatable("gtceu.multiblock.ore_rig.drilled_ores_list")
                        .withStyle(ChatFormatting.GREEN));
                textList.add(Component.translatable("gtceu.multiblock.ore_rig.drilled_ore_entry", noOre)
                        .withStyle(ChatFormatting.GRAY));
            }
        } else {
            Component tooltip = Component.translatable("gtceu.multiblock.invalid_structure.tooltip")
                    .withStyle(ChatFormatting.GRAY);
            textList.add(Component.translatable("gtceu.multiblock.invalid_structure")
                    .withStyle(Style.EMPTY.withColor(ChatFormatting.RED)
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip))));
        }
    }

    public static int getDepletionChance(int tier) {
        if (tier == EFValues.MV)
            return 1;
        if (tier == EFValues.HV)
            return 2;
        if (tier == EFValues.EV)
            return 8;
        return 1;
    }

    public static int getRigMultiplier(int tier) {
        if (tier == EFValues.MV)
            return 1;
        if (tier == EFValues.HV)
            return 4;
        if (tier == EFValues.EV)
            return 16;
        return 1;
    }

    public static net.minecraft.world.level.block.Block getCasingState(int tier) {
        if (tier == EFValues.MV)
            return GTBlocks.CASING_STEEL_SOLID.get();
        if (tier == EFValues.HV)
            return GTBlocks.CASING_TITANIUM_STABLE.get();
        if (tier == EFValues.EV)
            return GTBlocks.CASING_TUNGSTENSTEEL_ROBUST.get();
        return GTBlocks.CASING_STEEL_SOLID.get();
    }

    public static net.minecraft.world.level.block.Block getFrameState(int tier) {
        if (tier == EFValues.MV)
            return GTMaterialBlocks.MATERIAL_BLOCKS.get(TagPrefix.frameGt, GTMaterials.Steel).get();
        if (tier == EFValues.HV)
            return GTMaterialBlocks.MATERIAL_BLOCKS.get(TagPrefix.frameGt, GTMaterials.Titanium).get();
        if (tier == EFValues.EV)
            return GTMaterialBlocks.MATERIAL_BLOCKS.get(TagPrefix.frameGt, GTMaterials.TungstenSteel).get();
        return GTMaterialBlocks.MATERIAL_BLOCKS.get(TagPrefix.frameGt, GTMaterials.Steel).get();
    }

    public static ResourceLocation getBaseTexture(int tier) {
        if (tier == EFValues.MV)
            return ExtForCore.id("block/casings/solid/machine_casing_solid_steel");
        if (tier == EFValues.HV)
            return ExtForCore.id("block/casings/solid/machine_casing_stable_titanium");
        if (tier == EFValues.EV)
            return ExtForCore.id("block/casings/solid/machine_casing_robust_tungstensteel");
        return ExtForCore.id("block/casings/solid/machine_casing_solid_steel");
    }
}
