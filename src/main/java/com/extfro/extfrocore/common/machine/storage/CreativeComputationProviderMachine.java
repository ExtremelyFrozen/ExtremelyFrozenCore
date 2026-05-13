package com.extfro.extfrocore.common.machine.storage;

import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.capability.IOpticalComputationProvider;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.gui.ModularUIBuilder;
import com.extfro.extfrocore.api.machine.MetaMachine;
import com.extfro.extfrocore.api.machine.TickableSubscription;
import com.extfro.extfrocore.api.machine.feature.IUIMachine;
import com.extfro.extfrocore.api.sync_system.annotations.SaveField;
import com.extfro.extfrocore.common.machine.gui.MachineUIHelper;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class CreativeComputationProviderMachine extends MetaMachine
                                                implements IUIMachine, IOpticalComputationProvider {

    @SaveField
    private int maxCWUt;
    private int lastRequestedCWUt;
    private int requestedCWUPerSec;
    @SaveField
    @Getter
    private boolean active;
    @Nullable
    private TickableSubscription computationSubs;

    public CreativeComputationProviderMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        updateComputationSubscription();
    }

    protected void updateComputationSubscription() {
        if (active) {
            this.computationSubs = subscribeServerTick(this::updateComputationTick);
        } else if (computationSubs != null) {
            computationSubs.unsubscribe();
            this.computationSubs = null;
            this.lastRequestedCWUt = 0;
            this.requestedCWUPerSec = 0;
        }
    }

    protected void updateComputationTick() {
        if (getOffsetTimer() % 20 == 0) {
            this.lastRequestedCWUt = requestedCWUPerSec / 20;
            this.requestedCWUPerSec = 0;
        }
    }

    @Override
    public int requestCWUt(
                           int cwut, boolean simulate, @NotNull Collection<IOpticalComputationProvider> seen) {
        seen.add(this);
        int requestedCWUt = active ? Math.min(cwut, maxCWUt) : 0;
        if (!simulate) {
            this.requestedCWUPerSec += requestedCWUt;
        }
        return requestedCWUt;
    }

    @Override
    public int getMaxCWUt(@NotNull Collection<IOpticalComputationProvider> seen) {
        seen.add(this);
        return active ? maxCWUt : 0;
    }

    @Override
    public boolean canBridge(@NotNull Collection<IOpticalComputationProvider> seen) {
        seen.add(this);
        return true;
    }

    public void setActive(boolean active) {
        this.active = active;
        updateComputationSubscription();
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUIBuilder(140, 95, this, entityPlayer)
                .background(GuiTextures.BACKGROUND)
                .widget(MachineUIHelper.label(7, 7, () -> Component.literal("CWUt")))
                .widget(MachineUIHelper.intTextField(9, 20, 122, 16, () -> maxCWUt,
                        value -> maxCWUt = Integer.parseInt(value), 0, Integer.MAX_VALUE))
                .widget(MachineUIHelper.label(7, 42, "gtceu.creative.computation.average"))
                .widget(MachineUIHelper.literalLabel(7, 54, () -> String.valueOf(lastRequestedCWUt)))
                .widget(MachineUIHelper.textButton(9, 66, 122, 20,
                        () -> Component.translatable(isActive() ? "gtceu.creative.activity.on" :
                                "gtceu.creative.activity.off"),
                        event -> setActive(!isActive())));
    }
}
