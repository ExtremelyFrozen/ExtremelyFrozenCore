package com.extfro.extfrocore.api.gui.widget;

import com.extfro.extfrocore.ExtForCore;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigSetter;
import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.DataBindingBuilder;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import dev.emi.emi.api.stack.EmiStack;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

@LDLRegister(name = "gtm_phantom_fluid_slot", group = "widget.gtm_container", priority = 50, registry = "ldlib2:ui_element")
public class PhantomFluidWidget extends TankWidget {

    private Supplier<FluidStack> phantomFluidGetter;
    private Consumer<FluidStack> phantomFluidSetter;

    @Nullable
    @Getter
    protected FluidStack lastPhantomStack;

    public PhantomFluidWidget() {
        super();
        this.phantomFluidGetter = () -> FluidStack.EMPTY;
        this.phantomFluidSetter = stack -> {};
        setupPhantom();
    }

    public PhantomFluidWidget(@Nullable IFluidHandler fluidTank, int tank, int x, int y, int width, int height,
                              Supplier<FluidStack> phantomFluidGetter, Consumer<FluidStack> phantomFluidSetter) {
        super(fluidTank, tank, x, y, width, height, false, false);
        this.phantomFluidGetter = phantomFluidGetter == null ? () -> FluidStack.EMPTY : phantomFluidGetter;
        this.phantomFluidSetter = phantomFluidSetter == null ? stack -> {} : phantomFluidSetter;
        setupPhantom();
    }

    private void setupPhantom() {
        setAllowClickFilled(false);
        setAllowClickDrained(false);
        xeiPhantom();
        bind(DataBindingBuilder.fluidStack(this::getPhantomFluid, this::setPhantomFluid).build());
        addServerEventListener(UIEvents.MOUSE_DOWN, this::onPhantomMouseDown);
    }

    public PhantomFluidWidget setPhantomFluidGetter(Supplier<FluidStack> phantomFluidGetter) {
        this.phantomFluidGetter = phantomFluidGetter == null ? () -> FluidStack.EMPTY : phantomFluidGetter;
        return this;
    }

    public PhantomFluidWidget setPhantomFluidSetter(Consumer<FluidStack> phantomFluidSetter) {
        this.phantomFluidSetter = phantomFluidSetter == null ? stack -> {} : phantomFluidSetter;
        return this;
    }

    @ConfigSetter(field = "allowClickFilled")
    @Override
    public PhantomFluidWidget setAllowClickFilled(boolean v) {
        super.setAllowClickFilled(false);
        return this;
    }

    @ConfigSetter(field = "allowClickDrained")
    @Override
    public PhantomFluidWidget setAllowClickDrained(boolean v) {
        super.setAllowClickDrained(false);
        return this;
    }

    public FluidStack getPhantomFluid() {
        FluidStack stack = phantomFluidGetter.get();
        return stack == null ? FluidStack.EMPTY : stack;
    }

    public void setPhantomFluid(FluidStack stack) {
        if (stack == null) {
            stack = FluidStack.EMPTY;
        }
        phantomFluidSetter.accept(stack);
        setLastPhantomStack(stack);
        setValue(stack, false);
    }

    protected void setLastPhantomStack(FluidStack fluid) {
        if (fluid != null && !fluid.isEmpty()) {
            this.lastPhantomStack = fluid.copy();
            this.lastPhantomStack.setAmount(1);
        } else {
            this.lastPhantomStack = null;
        }
    }

    public static FluidStack drainFrom(Object ingredient) {
        ingredient = convertIngredient(ingredient);
        if (ingredient instanceof Ingredient ing) {
            var items = ing.getItems();
            if (items.length > 0) {
                ingredient = items[0];
            }
        }
        if (ingredient instanceof FluidStack fluidStack) {
            return fluidStack;
        }
        if (ingredient instanceof ItemStack itemStack) {
            return FluidUtil.getFluidHandler(itemStack)
                    .map(h -> h.drain(Integer.MAX_VALUE, FluidAction.SIMULATE))
                    .orElse(FluidStack.EMPTY);
        }
        return FluidStack.EMPTY;
    }

    @Nullable
    private static Object convertIngredient(Object ingredient) {
        if (ExtForCore.Mods.isEMILoaded()) {
            ingredient = EMICallWrapper.tryWrap(ingredient);
        }
        return ingredient;
    }

    @Override
    public FluidStack getValue() {
        var fluid = getPhantomFluid();
        return fluid.isEmpty() ? super.getValue() : fluid;
    }

    @Override
    public TankWidget setFluid(FluidStack fluidStack, boolean notify) {
        setPhantomFluid(fluidStack);
        return this;
    }

    private void onPhantomMouseDown(UIEvent event) {
        if (event.button != 0) {
            return;
        }
        var mui = getModularUI();
        if (mui == null || mui.getMenu() == null) {
            return;
        }
        ItemStack itemStack = mui.getMenu().getCarried();
        FluidStack fluid = FluidUtil.getFluidContained(itemStack)
                .map(f -> f.copyWithAmount(FluidType.BUCKET_VOLUME))
                .orElse(FluidStack.EMPTY);
        setPhantomFluid(fluid);
        event.stopPropagation();
    }

    private static class EMICallWrapper {

        private static Object tryWrap(Object ingredient) {
            if (ingredient instanceof EmiStack emiStack) {
                var key = emiStack.getKey();
                if (key instanceof Fluid f) {
                    int amount = emiStack.getAmount() == 0 ? 1000 : (int) emiStack.getAmount();
                    ingredient = new FluidStack(f.builtInRegistryHolder(), amount, emiStack.getComponentChanges());
                } else if (key instanceof Item i) {
                    ingredient = new ItemStack(i, (int) emiStack.getAmount());
                    ((ItemStack) ingredient).applyComponents(emiStack.getComponentChanges());
                } else {
                    ingredient = null;
                }
            }
            return ingredient;
        }
    }
}
