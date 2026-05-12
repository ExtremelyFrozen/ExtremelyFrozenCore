package com.extfro.extfrocore.common.cover.detector;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.capability.ICoverable;
import com.extfro.extfrocore.api.capability.IEnergyInfoProvider;
import com.extfro.extfrocore.api.cover.CoverDefinition;
import com.extfro.extfrocore.api.cover.IUICover;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.gui.widget.LongInputWidget;
import com.extfro.extfrocore.api.gui.widget.ToggleButtonWidget;
import com.extfro.extfrocore.api.sync_system.annotations.SaveField;
import com.extfro.extfrocore.utils.GTMath;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

import com.lowdragmc.lowdraglib2.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib2.gui.widget.TextBoxWidget;
import com.lowdragmc.lowdraglib2.gui.widget.Widget;
import com.lowdragmc.lowdraglib2.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib2.utils.LocalizationUtils;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;
import java.util.List;

import static com.extfro.extfrocore.utils.RedstoneUtil.computeLatchedRedstoneBetweenValues;

public class AdvancedEnergyDetectorCover extends EnergyDetectorCover implements IUICover {

    private static final int DEFAULT_MIN_PERCENT = 33;
    private static final int DEFAULT_MAX_PERCENT = 66;

    @SaveField
    @Getter
    @Setter
    public long minValue, maxValue;

    @SaveField
    @Getter
    private boolean usePercent;

    private LongInputWidget minValueInput;
    private LongInputWidget maxValueInput;

    public AdvancedEnergyDetectorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
        this.minValue = DEFAULT_MIN_PERCENT;
        this.maxValue = DEFAULT_MAX_PERCENT;
        this.usePercent = true;
    }

    @Override
    protected void update() {
        if (!shouldUpdate()) return;

        IEnergyInfoProvider energyInfoProvider = getEnergyInfoProvider();
        if (energyInfoProvider == null) return;

        IEnergyInfoProvider.EnergyInfo energyInfo = energyInfoProvider.getEnergyInfo();
        boolean isBigInt = energyInfoProvider.supportsBigIntEnergyValues();

        if (isBigInt) {
            if (usePercent) {
                if (energyInfo.capacity().compareTo(BigInteger.ZERO) > 0) {
                    float ratio = GTMath.ratio(energyInfo.stored(), energyInfo.capacity());
                    setRedstoneSignalOutput(computeLatchedRedstoneBetweenValues(ratio * 100, maxValue,
                            minValue, isInverted(), redstoneSignalOutput));
                } else {
                    setRedstoneSignalOutput(isInverted() ? 15 : 0);
                }
            } else {
                setRedstoneSignalOutput(computeLatchedRedstoneBetweenValues(energyInfo.stored(),
                        BigInteger.valueOf(this.maxValue), BigInteger.valueOf(this.minValue),
                        isInverted(), redstoneSignalOutput));
            }
        } else {
            if (usePercent) {
                if (energyInfo.capacity().longValue() > 0) {
                    float ratio = energyInfo.stored().floatValue() / energyInfo.capacity().floatValue();
                    setRedstoneSignalOutput(computeLatchedRedstoneBetweenValues(ratio * 100, maxValue,
                            minValue, isInverted(), redstoneSignalOutput));
                } else {
                    setRedstoneSignalOutput(isInverted() ? 15 : 0);
                }
            } else {
                setRedstoneSignalOutput(computeLatchedRedstoneBetweenValues(energyInfo.stored().longValue(),
                        this.maxValue, this.minValue,
                        isInverted(), redstoneSignalOutput));
            }
        }
    }

    public void setUsePercent(boolean usePercent) {
        var wasPercent = this.usePercent;
        this.usePercent = usePercent;

        initializeMinMaxInputs(wasPercent);
    }

    //////////////////////////////////////
    // *********** GUI ***********//
    //////////////////////////////////////

    @Override
    public Widget createUIWidget() {
        WidgetGroup group = new WidgetGroup(0, 0, 176, 105);
        group.addWidget(new LabelWidget(10, 5, "cover.advanced_energy_detector.label"));

        group.addWidget(new TextBoxWidget(10, 55, 25,
                List.of(LocalizationUtils.format("cover.advanced_energy_detector.min"))));

        group.addWidget(new TextBoxWidget(10, 80, 25,
                List.of(LocalizationUtils.format("cover.advanced_energy_detector.max"))));

        minValueInput = new LongInputWidget(40, 50, 176 - 40 - 10, 20, this::getMinValue, this::setMinValue);
        maxValueInput = new LongInputWidget(40, 75, 176 - 40 - 10, 20, this::getMaxValue, this::setMaxValue);
        initializeMinMaxInputs(usePercent);
        group.addWidget(minValueInput);
        group.addWidget(maxValueInput);

        // Invert Redstone Output Toggle:
        group.addWidget(new ToggleButtonWidget(
                9, 20, 20, 20,
                GuiTextures.INVERT_REDSTONE_BUTTON, this::isInverted, this::setInverted)
                .isMultiLang()
                .setTooltipText("cover.advanced_energy_detector.invert"));

        // Mode (EU / Percent) Toggle:
        group.addWidget(new ToggleButtonWidget(
                176 - 29, 20, 20, 20,
                GuiTextures.ENERGY_DETECTOR_COVER_MODE_BUTTON, this::isUsePercent, this::setUsePercent)
                .isMultiLang()
                .setTooltipText("cover.advanced_energy_detector.use_percent"));

        return group;
    }

    private void initializeMinMaxInputs(boolean wasPercent) {
        if (ExtForCore.isClientThread() || minValueInput == null || maxValueInput == null)
            return;

        long energyCapacity;
        try {
            energyCapacity = getEnergyInfoProvider().getEnergyInfo().capacity().longValueExact();
        } catch (ArithmeticException e) {
            energyCapacity = Long.MAX_VALUE;
        }

        minValueInput.setMin(0L);
        maxValueInput.setMin(0L);

        if (usePercent) {
            // This needs to be before setting the maximum, because otherwise the value would be limited to 100 EU
            // before converting to percent.
            if (!wasPercent) {
                minValueInput.setValue(GTMath.clamp((long) (((double) minValue / energyCapacity) * 100), 0, 100));
                maxValueInput.setValue(GTMath.clamp((long) (((double) maxValue / energyCapacity) * 100), 0, 100));
            }

            minValueInput.setMax(100L);
            maxValueInput.setMax(100L);
        } else {
            minValueInput.setMax(energyCapacity);
            maxValueInput.setMax(energyCapacity);

            // This needs to be after setting the maximum, because otherwise the converted value would be
            // limited to 100.
            if (wasPercent) {
                minValueInput.setValue(
                        GTMath.clamp((long) Math.ceil((minValue / 100.0) * energyCapacity), 0, energyCapacity));
                maxValueInput.setValue(
                        GTMath.clamp((long) Math.ceil((maxValue / 100.0) * energyCapacity), 0, energyCapacity));
            }
        }
    }

    @Override
    public CompoundTag copyConfig(CompoundTag tag) {
        tag.putLong("min", minValue);
        tag.putLong("max", maxValue);
        tag.putBoolean("percent", usePercent);
        return super.copyConfig(tag);
    }

    @Override
    public void pasteConfig(ServerPlayer player, CompoundTag tag) {
        setMinValue(tag.getLong("min"));
        setMaxValue(tag.getLong("max"));
        setUsePercent(tag.getBoolean("percent"));
        super.pasteConfig(player, tag);
    }
}
