package com.extfro.extfrocore.api.machine;

import com.extfro.extfrocore.api.EFValues;
import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.capability.recipe.*;
import com.extfro.extfrocore.api.machine.feature.IFancyUIMachine;
import com.extfro.extfrocore.api.recipe.GTRecipe;
import com.extfro.extfrocore.api.recipe.content.ContentModifier;
import com.extfro.extfrocore.api.recipe.modifier.ModifierFunction;
import com.extfro.extfrocore.api.recipe.modifier.ParallelLogic;
import com.extfro.extfrocore.api.recipe.modifier.RecipeModifier;
import com.extfro.extfrocore.common.data.GTMedicalConditions;
import com.extfro.extfrocore.common.machine.trait.hazard.EnvironmentalHazardEmitterTrait;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.FluidSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ProgressBar;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import lombok.Getter;

/**
 * All singleblock generators are implemented here.
 */
public class SimpleGeneratorMachine extends WorkableTieredMachine
                                    implements IFancyUIMachine {

    @Getter
    private final EnvironmentalHazardEmitterTrait hazardEmitter;

    public SimpleGeneratorMachine(BlockEntityCreationInfo info, int tier,
                                  float hazardStrengthPerOperation, Int2IntFunction tankScalingFunction) {
        super(info, tier, tankScalingFunction);

        energyContainer.setSideOutputCondition(side -> !hasFrontFacing() || side == getFrontFacing());
        this.hazardEmitter = attachTrait(
                new EnvironmentalHazardEmitterTrait(GTMedicalConditions.CARBON_MONOXIDE_POISONING,
                        hazardStrengthPerOperation));
    }

    public SimpleGeneratorMachine(BlockEntityCreationInfo info, int tier, Int2IntFunction tankScalingFunction) {
        this(info, tier, 0.25f, tankScalingFunction);
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////

    @Override
    protected boolean isEnergyEmitter() {
        return true;
    }

    @Override
    public int tintColor(int index) {
        if (index == 2) {
            return EFValues.VC[getTier()];
        }
        return super.tintColor(index);
    }

    //////////////////////////////////////
    // ****** RECIPE LOGIC *******//
    //////////////////////////////////////

    /**
     * Recipe Modifier for <b>Simple Generator Machines</b> - can be used as a valid {@link RecipeModifier}
     * <p>
     * Recipe is fast parallelized up to {@code desiredEUt / recipeEUt} times.
     * </p>
     *
     * @param machine a {@link SimpleGeneratorMachine}
     * @param recipe  recipe
     * @return A {@link ModifierFunction} for the given Simple Generator
     */
    public static ModifierFunction recipeModifier(MetaMachine machine, GTRecipe recipe) {
        if (!(machine instanceof SimpleGeneratorMachine generator)) {
            return RecipeModifier.nullWrongType(SimpleGeneratorMachine.class, machine);
        }
        long EUt = recipe.getOutputEUt().getTotalEU();
        if (EUt <= 0) return ModifierFunction.NULL;

        int maxParallel = (int) (generator.getOverclockVoltage() / EUt);
        int parallels = ParallelLogic.getParallelAmountFast(generator, recipe, maxParallel);

        return ModifierFunction.builder()
                .inputModifier(ContentModifier.multiplier(parallels))
                .outputModifier(ContentModifier.multiplier(parallels))
                .eutMultiplier(parallels)
                .parallels(parallels)
                .build();
    }

    @Override
    public boolean regressWhenWaiting() {
        return false;
    }

    @Override
    public boolean canVoidRecipeOutputs(RecipeCapability<?> capability) {
        return capability != EURecipeCapability.CAP;
    }

    @Override
    public void afterWorking() {
        super.afterWorking();
        hazardEmitter.emitHazard();
    }

    @Override
    public long getDisplayRecipeVoltage() {
        return EFValues.V[this.tier];
    }

    //////////////////////////////////////
    // *********** GUI ***********//
    //////////////////////////////////////

    @Override
    public UIElement createUIWidget() {
        UIElement template = createRecipeTemplate();
        int templateWidth = (int) template.getSizeWidth();
        int templateHeight = (int) template.getSizeHeight();
        UIElement energyBar = createEnergyBar(this);
        int groupWidth = templateWidth + 18 + 12;
        int groupHeight = Math.max(templateHeight + 8, 68);
        UIElement group = new UIElement().layout(layout -> layout.width(groupWidth).height(groupHeight));
        energyBar.layout(layout -> layout.left(3).top((groupHeight - 60) / 2f).width(18).height(60));
        template.layout(layout -> layout.left((groupWidth - 18 - 4 - templateWidth) / 2f + 24)
                .top((groupHeight - templateHeight) / 2f));
        group.addChild(energyBar);
        group.addChild(template);
        return group;
    }

    private UIElement createRecipeTemplate() {
        var recipeUI = getRecipeType().getRecipeUI();
        UIElement template = recipeUI.createXEIElement(false, false);
        bindRecipeSlots(template);
        var progress = template.selectRegex("^progress$", ProgressBar.class).findFirst();
        UIElement wrapper = new UIElement() {

            @Override
            public void screenTick() {
                progress.ifPresent(progressBar -> progressBar.setProgress(recipeLogic.getProgressPercent()));
                super.screenTick();
            }
        };
        wrapper.layout(layout -> layout.width(template.getSizeWidth()).height(template.getSizeHeight()));
        template.layout(layout -> layout.left(0).top(0));
        wrapper.addChild(template);
        return wrapper;
    }

    private void bindRecipeSlots(UIElement template) {
        bindItemSlots(template, IO.IN, importItems.storage);
        bindItemSlots(template, IO.OUT, exportItems.storage);
        bindFluidSlots(template, IO.IN, importFluids);
        bindFluidSlots(template, IO.OUT, exportFluids);
    }

    private void bindItemSlots(UIElement template, IO io, net.neoforged.neoforge.items.IItemHandlerModifiable handler) {
        String regex = "^%s_[0-9]+$".formatted(ItemRecipeCapability.CAP.slotName(io));
        template.selectRegex(regex, ItemSlot.class).forEach(slot -> {
            int index = slotIndex(slot.getId());
            if (index >= 0 && index < handler.getSlots()) {
                slot.bind(handler, index);
            }
        });
    }

    private void bindFluidSlots(UIElement template, IO io,
                                com.extfro.extfrocore.api.transfer.fluid.IFluidHandlerModifiable handler) {
        String regex = "^%s_[0-9]+$".formatted(FluidRecipeCapability.CAP.slotName(io));
        template.selectRegex(regex, FluidSlot.class).forEach(slot -> {
            int index = slotIndex(slot.getId());
            if (index >= 0 && index < handler.getTanks()) {
                slot.bind(handler, index);
            }
        });
    }

    private int slotIndex(String id) {
        int idx = id.lastIndexOf('_');
        if (idx < 0 || idx == id.length() - 1) return -1;
        try {
            return Integer.parseInt(id.substring(idx + 1));
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }
}
