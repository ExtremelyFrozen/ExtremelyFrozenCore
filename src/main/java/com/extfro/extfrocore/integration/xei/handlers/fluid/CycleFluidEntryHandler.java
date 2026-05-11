package com.extfro.extfrocore.integration.xei.handlers.fluid;

import com.extfro.extfrocore.api.transfer.fluid.IFluidHandlerModifiable;
import com.extfro.extfrocore.integration.xei.entry.fluid.FluidEntryList;
import com.extfro.extfrocore.integration.xei.entry.fluid.FluidStackList;

import net.neoforged.neoforge.fluids.FluidStack;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CycleFluidEntryHandler implements IFluidHandlerModifiable {

    @Getter
    private final List<FluidEntryList> entries;

    @Nullable
    private List<List<FluidStack>> unwrapped;

    public CycleFluidEntryHandler(List<FluidEntryList> entries) {
        this.entries = new ArrayList<>(entries);
    }

    public List<List<FluidStack>> getUnwrapped() {
        if (unwrapped == null) {
            unwrapped = entries.stream()
                    .map(CycleFluidEntryHandler::getStacksNullable)
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        return unwrapped;
    }

    @Nullable
    private static List<FluidStack> getStacksNullable(@Nullable FluidEntryList list) {
        return list == null ? null : list.getStacks();
    }

    public FluidEntryList getEntry(int index) {
        return entries.get(index);
    }

    @Override
    public int getTanks() {
        return entries.size();
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        List<FluidStack> stackList = getUnwrapped().get(tank);
        if (stackList == null || stackList.isEmpty()) {
            return FluidStack.EMPTY;
        }
        int index = Math.abs((int) (System.currentTimeMillis() / 1000) % stackList.size());
        return stackList.get(index);
    }

    @Override
    public void setFluidInTank(int tank, @NotNull FluidStack stack) {
        if (tank >= 0 && tank < entries.size()) {
            entries.set(tank, FluidStackList.of(stack));
            unwrapped = null;
        }
    }

    @Override
    public int getTankCapacity(int tank) {
        return getFluidInTank(tank).getAmount();
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return false;
    }

    @Override
    public int fill(@NotNull FluidStack resource, @NotNull FluidAction action) {
        return 0;
    }

    @Override
    public @NotNull FluidStack drain(@NotNull FluidStack resource, @NotNull FluidAction action) {
        return FluidStack.EMPTY;
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, @NotNull FluidAction action) {
        return FluidStack.EMPTY;
    }

    @Override
    public boolean supportsFill(int tank) {
        return false;
    }

    @Override
    public boolean supportsDrain(int tank) {
        return false;
    }
}
