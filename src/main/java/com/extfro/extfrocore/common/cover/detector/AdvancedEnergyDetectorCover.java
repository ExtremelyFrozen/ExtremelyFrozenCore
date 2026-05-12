package com.extfro.extfrocore.common.cover.detector;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.capability.ICoverable;
import com.extfro.extfrocore.api.capability.IEnergyInfoProvider;
import com.extfro.extfrocore.api.cover.CoverDefinition;
import com.extfro.extfrocore.api.cover.IUICover;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.sync_system.annotations.SaveField;
import com.extfro.extfrocore.data.lang.LangHandler;
import com.extfro.extfrocore.utils.GTMath;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import com.lowdragmc.lowdraglib2.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

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

    private TextField minValueInput;
    private TextField maxValueInput;

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

    public void setMinValue(long minValue) {
        this.minValue = minValue;
    }

    public void setMaxValue(long maxValue) {
        this.maxValue = maxValue;
    }

    //////////////////////////////////////
    // *********** GUI ***********//
    //////////////////////////////////////

    @Override
    public UIElement createUIElement() {
        UIElement group = new UIElement().layout(layout -> layout.width(176).height(105));
        group.addChild(label(10, 5, "cover.advanced_energy_detector.label", 156));
        group.addChild(label(10, 55, "cover.advanced_energy_detector.min", 25));
        group.addChild(label(10, 80, "cover.advanced_energy_detector.max", 25));

        minValueInput = longInput(40, 50, 176 - 40 - 10, minValue, this::setMinValue);
        maxValueInput = longInput(40, 75, 176 - 40 - 10, maxValue, this::setMaxValue);
        initializeMinMaxInputs(usePercent);
        group.addChild(minValueInput);
        group.addChild(maxValueInput);

        // Invert Redstone Output Toggle:
        group.addChild(toggleButton(9, 20, 20, 20,
                GuiTextures.INVERT_REDSTONE_BUTTON, this::isInverted, this::setInverted,
                "cover.advanced_energy_detector.invert"));

        // Mode (EU / Percent) Toggle:
        group.addChild(toggleButton(176 - 29, 20, 20, 20,
                GuiTextures.ENERGY_DETECTOR_COVER_MODE_BUTTON, () -> usePercent, this::setUsePercent,
                "cover.advanced_energy_detector.use_percent"));

        return group;
    }

    private Label label(int x, int y, String translationKey, int width) {
        Label label = new Label();
        label.setValue(Component.translatable(translationKey));
        label.layout(layout -> layout.left(x).top(y).width(width).height(10));
        label.textStyle(style -> style.textColor(0x404040).textShadow(false));
        return label;
    }

    private TextField longInput(int x, int y, int width, long value, Consumer<Long> setter) {
        TextField field = new TextField();
        field.layout(layout -> layout.left(x).top(y).width(width).height(20));
        field.style(style -> style.background(GuiTextures.DISPLAY));
        field.textFieldStyle(style -> style.textColor(0x404040).textShadow(false));
        field.setNumbersOnlyLong(0L, Long.MAX_VALUE);
        field.setText(String.valueOf(value));
        field.setTextResponder(text -> {
            if (!text.isBlank()) {
                setter.accept(Long.parseLong(text));
            }
        });
        return field;
    }

    private Button toggleButton(int x, int y, int width, int height, IGuiTexture icon, BooleanSupplier getter,
                                Consumer<Boolean> setter, String tooltipPrefix) {
        Button button = new Button().noText();
        button.layout(layout -> layout.left(x).top(y).width(width).height(height));
        Consumer<Button> update = b -> {
            IGuiTexture texture = new GuiTextureGroup(GuiTextures.VANILLA_BUTTON.copy()
                    .setColor(getter.getAsBoolean() ? 0xffa0ffa0 : -1), icon);
            b.buttonStyle(style -> style.baseTexture(texture).hoverTexture(texture).pressedTexture(texture));
            b.style(style -> style.tooltips(LangHandler.getMultiLang(tooltipPrefix + "." +
                    (getter.getAsBoolean() ? "enabled" : "disabled")).toArray(Component[]::new)));
        };
        update.accept(button);
        button.setOnServerClick(event -> {
            setter.accept(!getter.getAsBoolean());
            update.accept(button);
        });
        return button;
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

        if (usePercent) {
            // This needs to be before setting the maximum, because otherwise the value would be limited to 100 EU
            // before converting to percent.
            if (!wasPercent) {
                minValue = GTMath.clamp((long) (((double) minValue / energyCapacity) * 100), 0, 100);
                maxValue = GTMath.clamp((long) (((double) maxValue / energyCapacity) * 100), 0, 100);
            }

            minValueInput.setNumbersOnlyLong(0L, 100L);
            maxValueInput.setNumbersOnlyLong(0L, 100L);
        } else {
            minValueInput.setNumbersOnlyLong(0L, energyCapacity);
            maxValueInput.setNumbersOnlyLong(0L, energyCapacity);

            // This needs to be after setting the maximum, because otherwise the converted value would be
            // limited to 100.
            if (wasPercent) {
                minValue = GTMath.clamp((long) Math.ceil((minValue / 100.0) * energyCapacity), 0, energyCapacity);
                maxValue = GTMath.clamp((long) Math.ceil((maxValue / 100.0) * energyCapacity), 0, energyCapacity);
            }
        }
        minValueInput.setText(String.valueOf(minValue), false);
        maxValueInput.setText(String.valueOf(maxValue), false);
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
