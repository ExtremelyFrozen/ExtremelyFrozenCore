package com.extfro.extfrocore.common.cover.detector;

import com.extfro.extfrocore.api.capability.ICoverable;
import com.extfro.extfrocore.api.cover.CoverDefinition;
import com.extfro.extfrocore.api.cover.IUICover;
import com.extfro.extfrocore.api.cover.filter.FilterHandler;
import com.extfro.extfrocore.api.cover.filter.FilterHandlers;
import com.extfro.extfrocore.api.cover.filter.FluidFilter;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.sync_system.annotations.SaveField;
import com.extfro.extfrocore.api.sync_system.annotations.SyncToClient;
import com.extfro.extfrocore.data.lang.LangHandler;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import com.lowdragmc.lowdraglib2.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import lombok.Getter;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.List;

import static com.extfro.extfrocore.utils.RedstoneUtil.computeLatchedRedstoneBetweenValues;
import static com.extfro.extfrocore.utils.RedstoneUtil.computeRedstoneBetweenValues;

public class AdvancedFluidDetectorCover extends FluidDetectorCover implements IUICover {

    private static final int DEFAULT_MIN = 64;
    private static final int DEFAULT_MAX = 512;
    @SaveField
    @Getter
    private int minValue, maxValue;

    @SaveField
    @SyncToClient
    @Getter
    private boolean isLatched;
    @SaveField
    @SyncToClient
    @Getter
    protected final FilterHandler<FluidStack, FluidFilter> filterHandler;

    public AdvancedFluidDetectorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);

        this.minValue = DEFAULT_MIN;
        this.maxValue = DEFAULT_MAX;

        filterHandler = FilterHandlers.fluid(this);
    }

    public void setLatched(boolean latched) {
        isLatched = latched;
        syncDataHolder.markClientSyncFieldDirty("isLatched");
    }

    @Override
    public List<ItemStack> getAdditionalDrops() {
        var list = super.getAdditionalDrops();
        if (!filterHandler.getFilterItem().isEmpty()) {
            list.add(filterHandler.getFilterItem());
        }
        return list;
    }

    @Override
    protected void update() {
        if (!shouldUpdate())
            return;

        FluidFilter filter = filterHandler.getFilter();
        IFluidHandler fluidHandler = getFluidHandler();
        if (fluidHandler == null)
            return;

        long storedFluid = 0;

        for (int tank = 0; tank < fluidHandler.getTanks(); tank++) {
            FluidStack content = fluidHandler.getFluidInTank(tank);

            if (!content.isEmpty() && filter.test(content))
                storedFluid += content.getAmount();
        }

        if (isLatched) {
            setRedstoneSignalOutput(computeLatchedRedstoneBetweenValues(storedFluid, maxValue, minValue,
                    isInverted(), redstoneSignalOutput));
        } else {
            setRedstoneSignalOutput(computeRedstoneBetweenValues(storedFluid, maxValue, minValue, isInverted()));
        }
    }

    public void setMinValue(int minValue) {
        this.minValue = Mth.clamp(minValue, 0, maxValue - 1);
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = Math.max(maxValue, 0);
    }

    //////////////////////////////////////
    // *********** GUI ***********//
    //////////////////////////////////////

    @Override
    public UIElement createUIElement() {
        UIElement group = new UIElement().layout(layout -> layout.width(176).height(170));
        group.addChild(label(10, 5, "cover.advanced_fluid_detector.label", 156));
        group.addChild(label(10, 55, "cover.advanced_fluid_detector.min", 65));
        group.addChild(label(10, 80, "cover.advanced_fluid_detector.max", 65));
        group.addChild(intInput(80, 50, 176 - 80 - 10, minValue, this::setMinValue));
        group.addChild(intInput(80, 75, 176 - 80 - 10, maxValue, this::setMaxValue));

        // Invert Redstone Output Toggle:
        group.addChild(toggleButton(9, 20, 20, 20,
                GuiTextures.INVERT_REDSTONE_BUTTON, this::isInverted, this::setInverted,
                "cover.advanced_fluid_detector.invert"));

        group.addChild(toggleButton(31, 21, 18, 18,
                GuiTextures.BUTTON_LOCK, () -> isLatched, this::setLatched,
                "cover.advanced_detector.latch"));

        group.addChild(filterHandler.createFilterSlotUI(148, 100));
        group.addChild(filterHandler.createFilterConfigUI(10, 100, 156, 60));

        return group;
    }

    private Label label(int x, int y, String translationKey, int width) {
        Label label = new Label();
        label.setValue(Component.translatable(translationKey));
        label.layout(layout -> layout.left(x).top(y).width(width).height(10));
        label.textStyle(style -> style.textColor(0x404040).textShadow(false));
        return label;
    }

    private TextField intInput(int x, int y, int width, int value, Consumer<Integer> setter) {
        TextField field = new TextField();
        field.layout(layout -> layout.left(x).top(y).width(width).height(20));
        field.style(style -> style.background(GuiTextures.DISPLAY));
        field.textFieldStyle(style -> style.textColor(0x404040).textShadow(false));
        field.setNumbersOnlyInt(0, Integer.MAX_VALUE);
        field.setText(String.valueOf(value));
        field.setTextResponder(text -> {
            if (!text.isBlank()) {
                setter.accept(Integer.parseInt(text));
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

    @Override
    public CompoundTag copyConfig(CompoundTag tag) {
        tag.putInt("min", minValue);
        tag.putInt("max", maxValue);
        tag.putBoolean("latched", isLatched);
        tag.put("filter", filterHandler.getFilterItem().save(coverHolder.getLevel().registryAccess()));
        return super.copyConfig(tag);
    }

    @Override
    public void pasteConfig(ServerPlayer player, CompoundTag tag) {
        setMinValue(tag.getInt("min"));
        setMaxValue(tag.getInt("max"));
        setLatched(tag.getBoolean("latched"));
        filterHandler.setFilterItem(ItemStack.parse(coverHolder.getLevel().registryAccess(), tag.getCompound("filter"))
                .orElse(ItemStack.EMPTY));
        super.pasteConfig(player, tag);
    }
}
