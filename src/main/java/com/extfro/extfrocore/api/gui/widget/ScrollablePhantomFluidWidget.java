package com.extfro.extfrocore.api.gui.widget;

import com.extfro.extfrocore.api.transfer.fluid.IFluidHandlerModifiable;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ScrollablePhantomFluidWidget extends PhantomFluidWidget {

    public ScrollablePhantomFluidWidget(@Nullable IFluidHandlerModifiable fluidTank, int tank, int x, int y, int width,
                                        int height, Supplier<FluidStack> phantomFluidGetter,
                                        Consumer<FluidStack> phantomFluidSetter) {
        super(fluidTank, tank, x, y, width, height, phantomFluidGetter, phantomFluidSetter);
        addServerEventListener(UIEvents.MOUSE_WHEEL, this::onMouseWheel);
    }

    private void onMouseWheel(UIEvent event) {
        if (event.deltaY == 0) {
            return;
        }
        int delta = getModifiedChangeAmount(event.deltaY > 0 ? 1 : -1, event);
        handleScrollAction(delta);
        event.stopPropagation();
    }

    private int getModifiedChangeAmount(int amount, UIEvent event) {
        if (event.isShiftDown()) {
            amount *= 10;
        }
        if (event.isCtrlDown()) {
            amount *= 100;
        }
        if (!event.isAltDown()) {
            amount *= 1000;
        }
        return amount;
    }

    private void handleScrollAction(int delta) {
        if (!(getFluidTank() instanceof IFluidHandlerModifiable fluidTank)) {
            return;
        }
        FluidStack fluid = fluidTank.getFluidInTank(getTank()).copy();
        if (fluid.isEmpty()) {
            return;
        }
        int amount = Math.min(Math.max(fluid.getAmount() + delta, 0), fluidTank.getTankCapacity(getTank()));
        if (amount <= 0) {
            fluidTank.setFluidInTank(getTank(), FluidStack.EMPTY);
        } else {
            fluid.setAmount(amount);
            fluidTank.setFluidInTank(getTank(), fluid);
        }
        setFluid(fluidTank.getFluidInTank(getTank()), true);
        fluidTank.drain(0, IFluidHandler.FluidAction.SIMULATE);
    }
}
