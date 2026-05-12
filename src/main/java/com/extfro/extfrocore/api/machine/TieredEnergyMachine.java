package com.extfro.extfrocore.api.machine;

import com.extfro.extfrocore.api.EFValues;
import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.machine.feature.ITieredMachine;
import com.extfro.extfrocore.api.machine.trait.EnvironmentalExplosionTrait;
import com.extfro.extfrocore.api.machine.trait.NotifiableEnergyContainer;
import com.extfro.extfrocore.api.sync_system.annotations.SaveField;
import com.extfro.extfrocore.api.sync_system.annotations.SyncToClient;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.SlotItemHandler;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.FillDirection;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ProgressBar;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

public class TieredEnergyMachine extends TieredMachine implements ITieredMachine {

    @SaveField
    @SyncToClient
    public final NotifiableEnergyContainer energyContainer;
    @Getter
    protected final EnvironmentalExplosionTrait environmentalExplosionTrait;

    public TieredEnergyMachine(BlockEntityCreationInfo info, int tier,
                               Function<TieredEnergyMachine, NotifiableEnergyContainer> energyContainerSupplier) {
        super(info, tier);
        energyContainer = energyContainerSupplier.apply(this);
        environmentalExplosionTrait = new EnvironmentalExplosionTrait(this, tier, tier * 10,
                () -> energyContainer.getEnergyStored() > 0);
    }

    public TieredEnergyMachine(BlockEntityCreationInfo info, int tier, NotifiableEnergyContainer energyContainer) {
        this(info, tier, machine -> machine.attachTrait(energyContainer));
    }

    public TieredEnergyMachine(BlockEntityCreationInfo info, int tier) {
        super(info, tier);

        long tierVoltage = EFValues.V[tier];
        if (isEnergyEmitter()) {
            energyContainer = NotifiableEnergyContainer.emitterContainer(this,
                    tierVoltage * 64L, tierVoltage, getMaxInputOutputAmperage());
        } else {
            energyContainer = NotifiableEnergyContainer.receiverContainer(this,
                    tierVoltage * 64L, tierVoltage, getMaxInputOutputAmperage());
        }
        environmentalExplosionTrait = new EnvironmentalExplosionTrait(this, tier, tier * 10,
                () -> energyContainer.getEnergyStored() > 0);
    }

    //////////////////////////////////////
    // ********** MISC ***********//
    //////////////////////////////////////
    @Override
    public int getAnalogOutputSignal() {
        long energyStored = energyContainer.getEnergyStored();
        long energyCapacity = energyContainer.getEnergyCapacity();
        float f = energyCapacity == 0L ? 0.0f : energyStored / (energyCapacity * 1.0f);
        return Mth.floor(f * 14.0f) + (energyStored > 0 ? 1 : 0);
    }

    /**
     * Determines max input or output amperage used by this meta tile entity
     * if emitter, it determines size of energy packets it will emit at once
     * if receiver, it determines max input energy per request
     *
     * @return max amperage received or emitted by this machine
     */
    protected long getMaxInputOutputAmperage() {
        return 1L;
    }

    /**
     * Determines if this meta tile entity is in energy receiver or emitter mode
     *
     * @return true if machine emits energy to network, false it it accepts energy from network
     */
    protected boolean isEnergyEmitter() {
        return false;
    }

    /**
     * Create an energy bar widget.
     */
    protected static EnergyBarUI createEnergyBar() {
        return new EnergyBarUI();
    }

    protected static ProgressBar createEnergyBar(TieredEnergyMachine machine) {
        ProgressBar progressBar = new ProgressBar() {

            @Override
            public void screenTick() {
                long capacity = machine.energyContainer.getEnergyCapacity();
                setProgress(capacity <= 0 ? 0f : machine.energyContainer.getEnergyStored() * 1f / capacity);
                super.screenTick();
            }
        };
        progressBar.setId("energy_container");
        progressBar.layout(layout -> layout.width(18).height(60));
        progressBar.style(style -> style.background(GuiTextures.ENERGY_BAR_BACKGROUND));
        progressBar.bar(bar -> bar.style(style -> style.background(GuiTextures.ENERGY_BAR_BASE)));
        progressBar.progressBarStyle(style -> style.fillDirection(FillDirection.DOWN_TO_UP));
        progressBar.label(Label::disabled);
        progressBar.setRange(0, 1);
        return progressBar;
    }

    protected static class EnergyBarUI {

        public ProgressBar createDefault() {
            ProgressBar progressBar = new ProgressBar();
            progressBar.layout(layout -> layout.width(18).height(60));
            progressBar.style(style -> style.background(GuiTextures.ENERGY_BAR_BACKGROUND));
            progressBar.bar(bar -> bar.style(style -> style.background(GuiTextures.ENERGY_BAR_BASE)));
            progressBar.progressBarStyle(style -> style.fillDirection(FillDirection.DOWN_TO_UP));
            progressBar.label(Label::disabled);
            progressBar.setRange(0, 1);
            return progressBar;
        }

        public void setupUI(UIElement template, TieredEnergyMachine machine) {
            template.addChild(createEnergyBar(machine));
        }
    }

    protected ItemSlot itemSlot(IItemHandlerModifiable itemHandler, int index, int x, int y, IGuiTexture background,
                                boolean canTakeItems, boolean canPutItems, Component... tooltips) {
        Slot slotReference = new SlotItemHandler(itemHandler, index, 0, 0) {

            @Override
            public boolean mayPlace(ItemStack stack) {
                return canPutItems && super.mayPlace(stack);
            }

            @Override
            public boolean mayPickup(@Nullable Player player) {
                return canTakeItems && super.mayPickup(player);
            }
        };
        ItemSlot slot = new ItemSlot(slotReference);
        slot.layout(layout -> layout.left(x).top(y).width(18).height(18));
        slot.style(style -> {
            style.background(background);
            if (tooltips.length > 0) {
                style.tooltips(tooltips);
            }
        });
        return slot;
    }

    protected Label label(int x, int y, int width, Component component) {
        Label label = new Label();
        label.setValue(component);
        label.layout(layout -> layout.left(x).top(y).width(width).height(10));
        label.textStyle(style -> style.textColor(0x404040).textShadow(false));
        return label;
    }

    protected TextField intInput(int x, int y, int width, int value, int min, int max, Consumer<Integer> setter) {
        TextField field = new TextField();
        field.layout(layout -> layout.left(x).top(y).width(width).height(20));
        field.style(style -> style.background(GuiTextures.DISPLAY));
        field.textFieldStyle(style -> style.textColor(0x404040).textShadow(false));
        field.setNumbersOnlyInt(min, max);
        field.setText(String.valueOf(value));
        field.setTextResponder(text -> {
            if (!text.isBlank()) {
                setter.accept(Mth.clamp(Integer.parseInt(text), min, max));
            }
        });
        return field;
    }

    protected UIElement panel(int width, int height, IGuiTexture background) {
        UIElement panel = new UIElement();
        panel.layout(layout -> layout.width(width).height(height));
        panel.style(style -> style.background(background));
        return panel;
    }
}
