package com.extfro.extfrocore.api.gui.widget;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.machine.trait.NotifiableFluidTank;
import com.extfro.extfrocore.api.transfer.fluid.IFluidHandlerModifiable;
import com.extfro.extfrocore.client.TooltipsHandler;
import com.extfro.extfrocore.integration.xei.entry.fluid.FluidEntryList;
import com.extfro.extfrocore.integration.xei.entry.fluid.FluidStackList;
import com.extfro.extfrocore.integration.xei.entry.fluid.FluidTagList;
import com.extfro.extfrocore.integration.xei.handlers.fluid.CycleFluidEntryHandler;
import com.extfro.extfrocore.utils.FormattingUtil;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigSetter;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.data.FillDirection;
import com.lowdragmc.lowdraglib2.gui.ui.elements.FluidSlot;
import com.lowdragmc.lowdraglib2.gui.ui.event.HoverTooltips;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import com.lowdragmc.lowdraglib2.integration.xei.IngredientIO;
import com.lowdragmc.lowdraglib2.math.Position;
import com.lowdragmc.lowdraglib2.math.Size;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import dev.emi.emi.api.neoforge.NeoForgeEmiStack;
import dev.emi.emi.api.stack.EmiIngredient;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

@SuppressWarnings("unused")
@LDLRegister(name = "gtm_fluid_slot", group = "widget.gtm_container", priority = 50, registry = "ldlib2:ui_element")
public class TankWidget extends FluidSlot {

    public static final IGuiTexture FLUID_SLOT_TEXTURE = SpriteTexture.of("ldlib2:textures/gui/fluid_slot.png")
            .setBorder(1);

    @Nullable
    protected IFluidHandler fluidTank;
    protected int tank;
    @Configurable(name = "ldlib.gui.editor.name.showAmount")
    protected boolean showAmount = true;
    @Configurable(name = "ldlib.gui.editor.name.drawHoverOverlay")
    protected boolean drawHoverOverlay = true;
    @Configurable(name = "ldlib.gui.editor.name.drawHoverTips")
    protected boolean drawHoverTips = true;
    protected BiConsumer<TankWidget, List<Component>> onAddedTooltips;
    @Getter
    protected IngredientIO ingredientIO = IngredientIO.NONE;
    @Getter
    protected float XEIChance = 1f;
    protected Runnable changeListener;
    protected boolean showAmountOverlay = true;

    public TankWidget() {
        this(null, 0, 0, 18, 18, true, true);
    }

    public void initTemplate() {
        setBackground(FLUID_SLOT_TEXTURE);
        setFillDirection(FillDirection.DOWN_TO_UP);
    }

    public TankWidget(IFluidHandler fluidTank, int x, int y, boolean allowClickContainerFilling,
                      boolean allowClickContainerEmptying) {
        this(fluidTank, x, y, 18, 18, allowClickContainerFilling, allowClickContainerEmptying);
    }

    public TankWidget(@Nullable IFluidHandler fluidTank, int x, int y, int width, int height,
                      boolean allowClickContainerFilling, boolean allowClickContainerEmptying) {
        this(fluidTank, 0, x, y, width, height, allowClickContainerFilling, allowClickContainerEmptying);
    }

    public TankWidget(IFluidHandler fluidHandler, int tank, int x, int y, boolean allowClickContainerFilling,
                      boolean allowClickContainerEmptying) {
        this(fluidHandler, tank, x, y, 18, 18, allowClickContainerFilling, allowClickContainerEmptying);
    }

    public TankWidget(@Nullable IFluidHandler fluidHandler, int tank, int x, int y, int width, int height,
                      boolean allowClickContainerFilling, boolean allowClickContainerEmptying) {
        super();
        layout(layout -> layout.left(x).top(y).width(width).height(height));
        setFluidTank(fluidHandler, tank);
        setAllowClickFilled(allowClickContainerFilling);
        setAllowClickDrained(allowClickContainerEmptying);
        setDrawHoverTips(true);
        setDrawHoverOverlay(true);
        addEventListener(com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents.HOVER_TOOLTIPS, this::addExtraTooltips);
        registerValueListener(value -> onValueChanged());
    }

    public TankWidget setFluidTank(IFluidHandler fluidTank) {
        return setFluidTank(fluidTank, 0);
    }

    public TankWidget setFluidTank(@Nullable IFluidHandler fluidTank, int tank) {
        if (fluidTank instanceof NotifiableFluidTank notifiable) {
            this.fluidTank = notifiable.getStorages()[tank];
            this.tank = 0;
        } else {
            this.fluidTank = fluidTank;
            this.tank = tank;
        }
        bind(this.fluidTank, this.tank);
        refreshFromTank();
        return this;
    }

    @Nullable
    public IFluidHandler getFluidTank() {
        return fluidTank;
    }

    public int getTank() {
        return tank;
    }

    public FluidStack getFluid() {
        return getValue();
    }

    public FluidStack getLastFluidInTank() {
        return getValue();
    }

    @Override
    public TankWidget setFluid(FluidStack fluidStack) {
        return setFluid(fluidStack, true);
    }

    @Override
    public TankWidget setFluid(FluidStack fluidStack, boolean notify) {
        if (fluidTank instanceof IFluidHandlerModifiable modifiable) {
            modifiable.setFluidInTank(tank, fluidStack);
        }
        setValue(fluidStack, notify);
        refreshCapacityFromTank();
        return this;
    }

    public TankWidget setBackground(IGuiTexture background) {
        style(style -> style.backgroundTexture(background));
        return this;
    }

    public TankWidget setOverlay(IGuiTexture overlay) {
        style(style -> style.overlay(overlay));
        return this;
    }

    public Position getPosition() {
        return Position.of(Math.round(getPositionX()), Math.round(getPositionY()));
    }

    public Size getSize() {
        return Size.of(Math.round(getSizeWidth()), Math.round(getSizeHeight()));
    }

    public Rect2i toRectangleBox() {
        var pos = getPosition();
        var size = getSize();
        return new Rect2i(pos.x, pos.y, size.width, size.height);
    }

    @ConfigSetter(field = "showAmount")
    public TankWidget setShowAmount(boolean showAmount) {
        this.showAmount = showAmount;
        amountLabel.setDisplay(showAmount && showAmountOverlay);
        return this;
    }

    @Override
    @ConfigSetter(field = "allowClickFilled")
    public TankWidget setAllowClickFilled(boolean allowClickFilled) {
        super.setAllowClickFilled(allowClickFilled);
        return this;
    }

    @Override
    @ConfigSetter(field = "allowClickDrained")
    public TankWidget setAllowClickDrained(boolean allowClickDrained) {
        super.setAllowClickDrained(allowClickDrained);
        return this;
    }

    public TankWidget setDrawHoverOverlay(boolean drawHoverOverlay) {
        this.drawHoverOverlay = drawHoverOverlay;
        slotStyle(style -> style.hoverOverlay(drawHoverOverlay ? new ColorRectTexture(0x80FFFFFF) : IGuiTexture.EMPTY));
        return this;
    }

    public TankWidget setDrawHoverTips(boolean drawHoverTips) {
        this.drawHoverTips = drawHoverTips;
        slotStyle(style -> style.showFluidTooltips(drawHoverTips));
        return this;
    }

    public TankWidget setFillDirection(FillDirection fillDirection) {
        slotStyle(style -> style.fillDirection(fillDirection));
        return this;
    }

    public TankWidget setFillDirection(Enum<?> fillDirection) {
        if (fillDirection != null) {
            setFillDirection(FillDirection.valueOf(fillDirection.name()));
        }
        return this;
    }

    public TankWidget setOnAddedTooltips(BiConsumer<TankWidget, List<Component>> onAddedTooltips) {
        this.onAddedTooltips = onAddedTooltips;
        return this;
    }

    public TankWidget setIngredientIO(IngredientIO ingredientIO) {
        this.ingredientIO = ingredientIO == null ? IngredientIO.NONE : ingredientIO;
        xeiRecipeIngredient(this.ingredientIO);
        return this;
    }

    public TankWidget setXEIChance(float XEIChance) {
        this.XEIChance = XEIChance;
        return this;
    }

    public TankWidget setChangeListener(Runnable changeListener) {
        this.changeListener = changeListener;
        return this;
    }

    public TankWidget setShowAmountOverlay(boolean showAmountOverlay) {
        this.showAmountOverlay = showAmountOverlay;
        amountLabel.setDisplay(showAmount && showAmountOverlay);
        return this;
    }

    public TankWidget setHoverTooltips(String... tooltips) {
        style(style -> style.tooltips(tooltips));
        return this;
    }

    public TankWidget setHoverTooltips(Component... tooltips) {
        style(style -> style.tooltips(tooltips));
        return this;
    }

    @Override
    public List<Component> getFullTooltipTexts() {
        refreshCapacityFromTank();
        List<Component> tooltips = new ArrayList<>();
        boolean isPhantom = this instanceof PhantomFluidWidget;
        var fluidStack = getValue();
        if (!fluidStack.isEmpty()) {
            tooltips.add(fluidStack.getHoverName());
            if (!isPhantom && showAmount) {
                tooltips.add(Component.translatable("gtceu.fluid.amount",
                        FormattingUtil.formatNumbers(fluidStack.getAmount()),
                        FormattingUtil.formatNumbers(getCapacity())));
            }
            TooltipsHandler.appendFluidTooltips(fluidStack, tooltips::add,
                    TooltipFlag.NORMAL, Item.TooltipContext.of(getModularUI().player.level()));
        } else {
            tooltips.add(Component.translatable("gtceu.fluid.empty"));
            if (!isPhantom && showAmount) {
                tooltips.add(Component.translatable("gtceu.fluid.amount", 0,
                        FormattingUtil.formatNumbers(getCapacity())));
            }
        }
        tooltips.addAll(getAdditionalTooltips(new ArrayList<>()));
        tooltips.addAll(getStyle().tooltips().asList());
        return tooltips;
    }

    public List<Component> getAdditionalTooltips(List<Component> list) {
        if (this.onAddedTooltips != null) {
            this.onAddedTooltips.accept(this, list);
        }
        return list;
    }

    public List<Object> getXEIIngredients() {
        var fluid = getValue();
        if (fluid.isEmpty()) return Collections.emptyList();
        if (fluidTank instanceof CycleFluidEntryHandler entryHandler) {
            return getXEIIngredientsClickable(entryHandler, tank);
        }
        return List.of(convertIngredient(fluid));
    }

    @Nullable
    public Object getXEIIngredientOverMouse(double mouseX, double mouseY) {
        if (isMouseOverElement((float) mouseX, (float) mouseY)) {
            var ingredients = getXEIIngredients();
            return ingredients.isEmpty() ? null : ingredients.getFirst();
        }
        return null;
    }

    public Object getXEICurrentIngredient() {
        var fluid = getValue();
        return fluid.isEmpty() ? null : convertIngredient(fluid);
    }

    private Object convertIngredient(FluidStack fluidStack) {
        if (ExtForCore.Mods.isEMILoaded()) {
            return EMICallWrapper.getEMIIngredient(fluidStack, getXEIChance());
        }
        return fluidStack;
    }

    private List<Object> getXEIIngredientsClickable(CycleFluidEntryHandler handler, int index) {
        FluidEntryList entryList = handler.getEntry(index);
        if (ExtForCore.Mods.isEMILoaded()) {
            return EMICallWrapper.getEMIIngredients(entryList, getXEIChance());
        }
        return Collections.emptyList();
    }

    private void addExtraTooltips(UIEvent event) {
        if (!drawHoverTips) {
            return;
        }
        event.hoverTooltips = new HoverTooltips(getFullTooltipTexts(), null, null, ItemStack.EMPTY);
    }

    private void onValueChanged() {
        if (changeListener != null) {
            changeListener.run();
        }
    }

    protected void refreshFromTank() {
        if (fluidTank == null || tank < 0 || tank >= fluidTank.getTanks()) {
            setValue(FluidStack.EMPTY, false);
            setCapacity(0);
            return;
        }
        setCapacity(fluidTank.getTankCapacity(tank));
        setValue(fluidTank.getFluidInTank(tank), false);
    }

    protected void refreshCapacityFromTank() {
        if (fluidTank != null && tank >= 0 && tank < fluidTank.getTanks()) {
            setCapacity(fluidTank.getTankCapacity(tank));
        }
    }

    public static final class EMICallWrapper {

        private static EmiIngredient toEMIIngredient(Stream<FluidStack> stream) {
            return EmiIngredient.of(stream.map(NeoForgeEmiStack::of).toList());
        }

        public static List<Object> getEMIIngredients(FluidStackList list, float xeiChance) {
            return List.of(toEMIIngredient(list.stream()).setChance(xeiChance));
        }

        public static List<Object> getEMIIngredients(FluidTagList list, float xeiChance) {
            return List.of(toEMIIngredient(list.getStacks().stream()).setChance(xeiChance));
        }

        public static List<Object> getEMIIngredients(FluidEntryList list, float xeiChance) {
            if (list instanceof FluidTagList tagList) return getEMIIngredients(tagList, xeiChance);
            if (list instanceof FluidStackList stackList) return getEMIIngredients(stackList, xeiChance);
            return Collections.emptyList();
        }

        public static Object getEMIIngredient(FluidStack fluidStack, float xeiChance) {
            return NeoForgeEmiStack.of(fluidStack).setChance(xeiChance);
        }
    }
}
