package com.extfro.extfrocore.api.machine.steam;

import com.extfro.extfrocore.api.EFValues;
import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.capability.recipe.IO;
import com.extfro.extfrocore.api.capability.recipe.ItemRecipeCapability;
import com.extfro.extfrocore.api.capability.recipe.RecipeCapability;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.gui.UITemplate;
import com.extfro.extfrocore.api.gui.widget.PredicatedImageWidget;
import com.extfro.extfrocore.api.machine.MetaMachine;
import com.extfro.extfrocore.api.machine.feature.IUIMachine;
import com.extfro.extfrocore.api.machine.property.GTMachineModelProperties;
import com.extfro.extfrocore.api.machine.trait.NotifiableItemStackHandler;
import com.extfro.extfrocore.api.machine.trait.RecipeHandlerList;
import com.extfro.extfrocore.api.pattern.util.RelativeDirection;
import com.extfro.extfrocore.api.recipe.GTRecipe;
import com.extfro.extfrocore.api.recipe.RecipeHelper;
import com.extfro.extfrocore.api.recipe.modifier.ModifierFunction;
import com.extfro.extfrocore.api.recipe.modifier.RecipeModifier;
import com.extfro.extfrocore.api.sync_system.annotations.SaveField;
import com.extfro.extfrocore.client.model.machine.MachineRenderState;
import com.extfro.extfrocore.common.machine.gui.MachineUIHelper;
import com.extfro.extfrocore.common.machine.trait.ExhaustVentMachineTrait;
import com.extfro.extfrocore.common.recipe.condition.VentCondition;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import com.google.common.collect.Tables;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.math.Position;
import lombok.Getter;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * A singleblock steam machine with recipe logic and item IO.
 */
public class SimpleSteamMachine extends SteamWorkableMachine implements IUIMachine {

    @SaveField
    public final NotifiableItemStackHandler importItems;
    @SaveField
    public final NotifiableItemStackHandler exportItems;

    @Getter
    private final ExhaustVentMachineTrait exhaustVentTrait;

    public SimpleSteamMachine(BlockEntityCreationInfo info, boolean isHighPressure) {
        super(info, isHighPressure);
        this.importItems = attachTrait(createImportItemHandler());
        this.exportItems = attachTrait(createExportItemHandler());

        this.exhaustVentTrait = attachTrait(new ExhaustVentMachineTrait());
        exhaustVentTrait.setVentingDamageAmount(isHighPressure() ? 12F : 6F);
        MachineRenderState renderState = getRenderState();
        if (renderState.hasProperty(GTMachineModelProperties.VENT_DIRECTION)) {
            // outputFacing will always be opposite the front facing on init
            setRenderState(renderState.setValue(GTMachineModelProperties.VENT_DIRECTION, RelativeDirection.BACK));
        }
    }

    //////////////////////////////////////
    // ***** Initialization *****//
    //////////////////////////////////////

    protected NotifiableItemStackHandler createImportItemHandler() {
        return new NotifiableItemStackHandler(getRecipeType().getMaxInputs(ItemRecipeCapability.CAP), IO.IN);
    }

    protected NotifiableItemStackHandler createExportItemHandler() {
        return new NotifiableItemStackHandler(getRecipeType().getMaxOutputs(ItemRecipeCapability.CAP), IO.OUT);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        exhaustVentTrait.setVentingDirection(Objects.requireNonNull(getOutputFacing()));
        // Simulate an EU machine via a SteamEnergyHandler
        this.addHandlerList(RecipeHandlerList.of(IO.IN, new SteamEnergyRecipeHandler(steamTank, getConversionRate())));
    }

    //////////////////////////////////////
    // ****** Venting Logic ******//
    //////////////////////////////////////

    public void updateModelVentDirection() {
        MachineRenderState renderState = getRenderState();
        if (renderState.hasProperty(GTMachineModelProperties.VENT_DIRECTION)) {
            Direction upwardsDir = getUpwardsFacing();
            // the up facing is already rotated if extended facing is enabled for the machine
            if (getFrontFacing() == Direction.UP && !allowExtendedFacing()) {
                upwardsDir = upwardsDir.getOpposite();
            }
            var relative = RelativeDirection.findRelativeOf(getFrontFacing(), exhaustVentTrait.getVentingDirection(),
                    upwardsDir);
            setRenderState(renderState.setValue(GTMachineModelProperties.VENT_DIRECTION, relative));
        }
    }

    @Override
    public void setOutputFacing(Direction outputFacing) {
        var oldFacing = getOutputFacing();
        super.setOutputFacing(outputFacing);
        if (getOutputFacing() != oldFacing) {
            updateModelVentDirection();
            exhaustVentTrait.setVentingDirection(outputFacing);
        }
    }

    @Override
    public void setFrontFacing(Direction facing) {
        var oldFacing = getFrontFacing();
        super.setFrontFacing(facing);
        if (getFrontFacing() != oldFacing) {
            updateModelVentDirection();
        }
    }

    @Override
    public void setUpwardsFacing(Direction upwardsFacing) {
        var oldFacing = getUpwardsFacing();
        super.setUpwardsFacing(upwardsFacing);
        if (getUpwardsFacing() != oldFacing) {
            updateModelVentDirection();
        }
    }

    public double getConversionRate() {
        return isHighPressure() ? 2.0 : 1.0;
    }

    //////////////////////////////////////
    // ****** Recipe Logic ******//
    //////////////////////////////////////

    /**
     * Recipe Modifier for <b>Simple Steam Machines</b> - can be used as a valid {@link RecipeModifier}
     * <p>
     * Recipe is rejected if tier is greater than LV or if machine cannot vent.<br>
     * Duration is multiplied by {@code 2} if the machine is low pressure
     * </p>
     *
     * @param machine a {@link SimpleSteamMachine}
     * @param recipe  recipe
     * @return A {@link ModifierFunction} for the given Steam Machine
     */
    public static ModifierFunction recipeModifier(MetaMachine machine, GTRecipe recipe) {
        if (!(machine instanceof SimpleSteamMachine steamMachine)) {
            return RecipeModifier.nullWrongType(SimpleSteamMachine.class, machine);
        }
        if (RecipeHelper.getRecipeEUtTier(recipe) > EFValues.LV || !steamMachine.exhaustVentTrait.checkVenting()) {
            return ModifierFunction.NULL;
        }

        var builder = ModifierFunction.builder().conditions(VentCondition.INSTANCE);
        if (!steamMachine.isHighPressure) builder.durationMultiplier(2);
        return builder.build();
    }

    @Override
    public void afterWorking() {
        super.afterWorking();
        exhaustVentTrait.setNeedsVenting(true);
        exhaustVentTrait.checkVenting();
    }

    //////////////////////////////////////
    // *********** GUI ***********//
    //////////////////////////////////////

    @Override
    public ModularUI createUI(Player entityPlayer) {
        var storages = Tables.newCustomTable(new EnumMap<>(IO.class), LinkedHashMap<RecipeCapability<?>, Object>::new);
        storages.put(IO.IN, ItemRecipeCapability.CAP, importItems.storage);
        storages.put(IO.OUT, ItemRecipeCapability.CAP, exportItems.storage);

        var group = getRecipeType().getRecipeUI().createUITemplate(recipeLogic::getProgressPercent,
                storages,
                new CompoundTag(),
                Collections.emptyList(),
                true,
                isHighPressure);
        Position pos = new Position((Math.max(group.getSize().width + 4 + 8, 176) - 4 - group.getSize().width) / 2 + 4,
                32);
        group.setSelfPosition(pos);
        return new ModularUI(176, 166, this, entityPlayer)
                .background(GuiTextures.BACKGROUND_STEAM.get(isHighPressure))
                .widget(group)
                .widget(MachineUIHelper.label(5, 5, getBlockState().getBlock().getDescriptionId()))
                .widget(new PredicatedImageWidget(pos.x + group.getSize().width / 2 - 9,
                        pos.y + group.getSize().height / 2 - 9, 18, 18,
                        GuiTextures.INDICATOR_NO_STEAM.get(isHighPressure))
                        .setPredicate(recipeLogic::isWaiting))
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(),
                        GuiTextures.SLOT_STEAM.get(isHighPressure), 7, 84, true));
    }
}
