package com.extfro.extfrocore.common.machine.storage;

import com.extfro.extfrocore.api.EFValues;
import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.capability.GTCapabilityHelper;
import com.extfro.extfrocore.api.capability.IEnergyContainer;
import com.extfro.extfrocore.api.capability.ILaserContainer;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.gui.ModularUIBuilder;
import com.extfro.extfrocore.api.machine.TieredMachine;
import com.extfro.extfrocore.api.machine.feature.IUIMachine;
import com.extfro.extfrocore.api.sync_system.annotations.SaveField;
import com.extfro.extfrocore.common.machine.gui.MachineUIHelper;
import com.extfro.extfrocore.utils.GTUtil;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import com.lowdragmc.lowdraglib2.gui.ColorPattern;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Selector;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

public class CreativeEnergyContainerMachine extends TieredMachine implements ILaserContainer, IUIMachine {

    @SaveField
    private long voltage = 0;
    @SaveField
    private int amps = 1;
    @SaveField
    private int setTier = 0;
    @SaveField
    private boolean active = false;
    @SaveField
    private boolean source = true;
    @SaveField
    private long energyIOPerSec = 0;
    private long lastAverageEnergyIOPerTick = 0;
    private long ampsReceived = 0;
    private boolean doExplosion = false;

    public CreativeEnergyContainerMachine(BlockEntityCreationInfo info) {
        super(info, EFValues.MAX);
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////

    @Override
    public void onLoad() {
        super.onLoad();
        subscribeServerTick(this::updateEnergyTick);
    }

    //////////////////////////////////////
    // ********** MISC ***********//
    //////////////////////////////////////

    protected void updateEnergyTick() {
        if (getOffsetTimer() % 20 == 0) {
            this.setIOSpeed(energyIOPerSec / 20);
            energyIOPerSec = 0;
            if (doExplosion) {
                getLevel().explode(null, getBlockPos().getX() + 0.5, getBlockPos().getY() + 0.5,
                        getBlockPos().getZ() + 0.5,
                        1, Level.ExplosionInteraction.NONE);
                doExplosion = false;
            }
        }
        ampsReceived = 0;
        if (!active || !source || voltage <= 0 || amps <= 0) return;
        int ampsUsed = 0;
        for (var facing : GTUtil.DIRECTIONS) {
            var opposite = facing.getOpposite();
            IEnergyContainer container = GTCapabilityHelper.getEnergyContainer(getLevel(),
                    getBlockPos().relative(facing),
                    opposite);
            // Try to get laser capability
            if (container == null)
                container = GTCapabilityHelper.getLaser(getLevel(), getBlockPos().relative(facing), opposite);

            if (container != null && container.inputsEnergy(opposite) && container.getEnergyCanBeInserted() > 0) {
                ampsUsed += container.acceptEnergyFromNetwork(opposite, voltage, amps - ampsUsed);
                if (ampsUsed >= amps) {
                    break;
                }
            }
        }
        energyIOPerSec += ampsUsed * voltage;
    }

    @Override
    public long acceptEnergyFromNetwork(Direction side, long voltage, long amperage) {
        if (source || !active || ampsReceived >= amps) {
            return 0;
        }
        if (voltage > this.voltage) {
            if (doExplosion)
                return 0;
            doExplosion = true;
            return Math.min(amperage, getInputAmperage() - ampsReceived);
        }
        long amperesAccepted = Math.min(amperage, getInputAmperage() - ampsReceived);
        if (amperesAccepted > 0) {
            ampsReceived += amperesAccepted;
            energyIOPerSec += amperesAccepted * voltage;
            return amperesAccepted;
        }
        return 0;
    }

    @Override
    public boolean inputsEnergy(Direction side) {
        return !source;
    }

    @Override
    public boolean outputsEnergy(Direction side) {
        return source;
    }

    @Override
    public long changeEnergy(long differenceAmount) {
        if (source || !active) {
            return 0;
        }
        energyIOPerSec += differenceAmount;
        return differenceAmount;
    }

    @Override
    public long getEnergyStored() {
        return 69;
    }

    @Override
    public long getEnergyCapacity() {
        return 420;
    }

    @Override
    public long getInputAmperage() {
        return source ? 0 : amps;
    }

    @Override
    public long getInputVoltage() {
        return source ? 0 : voltage;
    }

    @Override
    public long getOutputVoltage() {
        return source ? voltage : 0;
    }

    @Override
    public long getOutputAmperage() {
        return source ? amps : 0;
    }

    public void setIOSpeed(long energyIOPerSec) {
        if (this.lastAverageEnergyIOPerTick != energyIOPerSec) {
            this.lastAverageEnergyIOPerTick = energyIOPerSec;
        }
    }

    //////////////////////////////////////
    // *********** GUI ***********//
    //////////////////////////////////////

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUIBuilder(176, 166, this, entityPlayer)
                .background(GuiTextures.BACKGROUND)
                .widget(MachineUIHelper.label(7, 32, "gtceu.creative.energy.voltage"))
                .widget(MachineUIHelper.longTextField(9, 47, 152, 16, () -> voltage,
                        value -> {
                            voltage = Long.parseLong(value);
                            setTier = GTUtil.getTierByVoltage(voltage);
                        }, 0L, Long.MAX_VALUE))
                .widget(MachineUIHelper.label(7, 74, "gtceu.creative.energy.amperage"))
                .widget(MachineUIHelper.textButton(7, 87, 20, 20, () -> Component.literal("-"),
                        event -> amps = --amps == -1 ? 0 : amps))
                .widget(MachineUIHelper.intTextField(31, 89, 114, 16, () -> amps,
                        value -> amps = Integer.parseInt(value), 0, Integer.MAX_VALUE))
                .widget(MachineUIHelper.textButton(149, 87, 20, 20, () -> Component.literal("+"),
                        event -> {
                            if (amps < Integer.MAX_VALUE) {
                                amps++;
                            }
                        }))
                .widget(MachineUIHelper.literalLabel(7, 110,
                        () -> "Average Energy I/O per tick: " + this.lastAverageEnergyIOPerTick))
                .widget(MachineUIHelper.textButton(7, 139, 77, 20,
                        () -> Component.translatable(active ? "gtceu.creative.activity.on" :
                                "gtceu.creative.activity.off"),
                        event -> active = !active))
                .widget(MachineUIHelper.textButton(85, 139, 77, 20,
                        () -> Component.translatable(source ? "gtceu.creative.energy.source" :
                                "gtceu.creative.energy.sink"),
                        event -> {
                            source = !source;
                            if (source) {
                                voltage = 0;
                                amps = 0;
                                setTier = 0;
                            } else {
                                voltage = EFValues.V[14];
                                amps = Integer.MAX_VALUE;
                                setTier = 14;
                            }
                        }))
                .widget(createTierSelector());
    }

    private Selector<String> createTierSelector() {
        var selector = new Selector<String>();
        selector.layout(layout -> layout.left(7).top(7).width(50).height(20));
        selector.setCandidates(Arrays.stream(EFValues.VNF).toList());
        selector.setValue(EFValues.VNF[setTier], false);
        selector.setOnValueChanged(tier -> {
            setTier = ArrayUtils.indexOf(EFValues.VNF, tier);
            voltage = EFValues.VEX[setTier];
        });
        selector.style(style -> style.background(ColorPattern.BLACK.rectTexture()));
        return selector;
    }
}
