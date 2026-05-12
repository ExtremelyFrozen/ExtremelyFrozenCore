package com.extfro.extfrocore.common.machine.storage;

import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.capability.IControllable;
import com.extfro.extfrocore.api.capability.recipe.IO;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.gui.widget.PhantomFluidWidget;
import com.extfro.extfrocore.api.gui.widget.TankWidget;
import com.extfro.extfrocore.api.gui.widget.ToggleButtonWidget;
import com.extfro.extfrocore.api.machine.*;
import com.extfro.extfrocore.api.machine.feature.IFancyUIMachine;
import com.extfro.extfrocore.api.machine.trait.AutoOutputTrait;
import com.extfro.extfrocore.api.machine.trait.MachineTrait;
import com.extfro.extfrocore.api.machine.trait.MachineTraitType;
import com.extfro.extfrocore.api.sync_system.annotations.SaveField;
import com.extfro.extfrocore.api.sync_system.annotations.SyncToClient;
import com.extfro.extfrocore.api.transfer.fluid.CustomFluidTank;
import com.extfro.extfrocore.api.transfer.fluid.IFluidHandlerModifiable;
import com.extfro.extfrocore.utils.ExtendedUseOnContext;
import com.extfro.extfrocore.utils.FormattingUtil;
import com.extfro.extfrocore.utils.GTMath;
import com.extfro.extfrocore.utils.GTTransferUtils;

import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import com.lowdragmc.lowdraglib2.gui.ColorPattern;
import com.lowdragmc.lowdraglib2.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib2.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib2.gui.widget.Widget;
import com.lowdragmc.lowdraglib2.gui.widget.WidgetGroup;
import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import lombok.Getter;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

@NotNullByDefault
public class QuantumTankMachine extends TieredMachine implements IControllable,
                                IFancyUIMachine {

    public static Object2LongMap<MachineDefinition> TANK_CAPACITY = Util.make(new Object2LongArrayMap<>(), map -> {
        map.defaultReturnValue(-1L);
    });

    @SaveField
    private boolean isVoiding;

    @Getter
    private final long maxAmount;
    protected final FluidCache cache;
    @SyncToClient
    @SaveField
    private final CustomFluidTank lockedFluid;

    @Getter
    @SyncToClient
    @SaveField
    protected FluidStack stored = FluidStack.EMPTY;
    @Getter
    @SyncToClient
    @SaveField
    protected long storedAmount = 0;

    @SaveField
    @SyncToClient
    public final AutoOutputTrait autoOutput;

    public QuantumTankMachine(BlockEntityCreationInfo info, int tier, long maxAmount) {
        super(info, tier);
        this.maxAmount = maxAmount;
        this.cache = createCacheFluidHandler();
        this.lockedFluid = new CustomFluidTank(1000);
        this.autoOutput = AutoOutputTrait.ofFluids(this, cache);
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////

    protected FluidCache createCacheFluidHandler() {
        return new FluidCache(this);
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    protected void onFluidChanged() {
        if (!isRemote()) {
            syncDataHolder.markClientSyncFieldDirty("storedAmount");
            syncDataHolder.markClientSyncFieldDirty("stored");
        }
    }

    //////////////////////////////////////
    // ****** Capability ********//
    //////////////////////////////////////

    @Override
    public @Nullable IItemHandlerModifiable getItemHandlerCap(@Nullable Direction side, boolean useCoverCapability) {
        if (side == getFrontFacing()) {
            return null;
        }
        return super.getItemHandlerCap(side, useCoverCapability);
    }

    @Override
    public @Nullable IFluidHandlerModifiable getFluidHandlerCap(@Nullable Direction side, boolean useCoverCapability) {
        if (side == getFrontFacing()) {
            return null;
        }
        return super.getFluidHandlerCap(side, useCoverCapability);
    }

    @Override
    public boolean isWorkingEnabled() {
        return autoOutput.isAutoOutputFluids();
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        autoOutput.setAllowAutoOutputFluids(isWorkingAllowed);
    }

    //////////////////////////////////////
    // ******* Interaction *******//
    //////////////////////////////////////

    @Override
    public InteractionResult onUseWithItem(ExtendedUseOnContext context) {
        if (context.getClickedFace() == getFrontFacing() && !isRemote()) {
            if (FluidUtil.interactWithFluidHandler(context.getPlayer(), context.getHand(), cache)) {
                return InteractionResult.SUCCESS;
            }
        }
        return super.onUseWithItem(context);
    }

    public boolean isLocked() {
        return !lockedFluid.isEmpty();
    }

    protected void setLocked(boolean locked) {
        if (!stored.isEmpty() && locked) {
            var copied = stored.copyWithAmount(FluidType.BUCKET_VOLUME);
            lockedFluid.setFluid(copied);
        } else if (!locked) {
            lockedFluid.setFluid(FluidStack.EMPTY);
        }
        syncDataHolder.markClientSyncFieldDirty("lockedFluid");
    }

    protected void setLocked(FluidStack fluid) {
        if (fluid.isEmpty()) setLocked(false);
        else if (stored.isEmpty()) lockedFluid.setFluid(fluid);
        else if (stored.is(fluid.getFluid())) setLocked(true);
        syncDataHolder.markClientSyncFieldDirty("lockedFluid");
    }

    public FluidStack getLockedFluid() {
        return lockedFluid.getFluid();
    }

    //////////////////////////////////////
    // *********** GUI ***********//
    //////////////////////////////////////
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 90, 63);
        group.addWidget(new ImageWidget(4, 4, 82, 55, GuiTextures.DISPLAY))
                .addWidget(new LabelWidget(8, 8, "gtceu.gui.fluid_amount"))
                .addWidget(new LabelWidget(8, 18, () -> FormattingUtil.formatBuckets(storedAmount))
                        .setTextColor(-1)
                        .setDropShadow(false))
                .addWidget(new TankWidget(cache, 0, 68, 23, true, true)
                        .setShowAmount(false)
                        .setBackground(GuiTextures.FLUID_SLOT))
                .addWidget(new PhantomFluidWidget(lockedFluid, 0, 68, 41, 18, 18,
                        this::getLockedFluid, this::setLocked)
                        .setShowAmount(false)
                        .setBackground(ColorPattern.T_GRAY.rectTexture()))
                .addWidget(new ToggleButtonWidget(4, 41, 18, 18,
                        GuiTextures.BUTTON_FLUID_OUTPUT, this.autoOutput::isAutoOutputFluids,
                        this.autoOutput::setAllowAutoOutputFluids)
                        .setShouldUseBaseBackground()
                        .setTooltipText("gtceu.gui.fluid_auto_output.tooltip"))
                .addWidget(new ToggleButtonWidget(22, 41, 18, 18,
                        GuiTextures.BUTTON_LOCK, this::isLocked, this::setLocked)
                        .setShouldUseBaseBackground()
                        .setTooltipText("gtceu.gui.fluid_lock.tooltip"))
                .addWidget(new ToggleButtonWidget(40, 41, 18, 18,
                        GuiTextures.BUTTON_VOID, () -> isVoiding, (b) -> isVoiding = b)
                        .setShouldUseBaseBackground()
                        .setTooltipText("gtceu.gui.fluid_voiding_partial.tooltip"));
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    protected class FluidCache extends MachineTrait implements IFluidHandler {

        public static final MachineTraitType<FluidCache> TYPE = new MachineTraitType<>(FluidCache.class);

        @Override
        public MachineTraitType<FluidCache> getTraitType() {
            return TYPE;
        }

        private final Predicate<FluidStack> filter = f -> !isLocked() || getLockedFluid().isFluidEqual(f);

        public FluidCache(MetaMachine holder) {
            super(holder);
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return stored.copyWithAmount(GTMath.saturatedCast(storedAmount));
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            long free = isVoiding ? Long.MAX_VALUE : maxAmount - storedAmount;
            long canFill = 0;
            if ((stored.isEmpty() || FluidStack.isSameFluidSameComponents(resource, stored)) && filter.test(resource)) {
                canFill = Math.min(resource.getAmount(), free);
            }
            if (action.execute() && canFill > 0) {
                if (stored.isEmpty()) stored = resource.copyWithAmount(FluidType.BUCKET_VOLUME);
                storedAmount = Math.min(maxAmount, storedAmount + canFill);
                onFluidChanged();
            }
            return (int) canFill;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            if (stored.isEmpty()) return FluidStack.EMPTY;
            long toDrain = Math.min(storedAmount, maxDrain);
            var copy = stored.copyWithAmount((int) toDrain);
            if (action.execute() && toDrain > 0) {
                storedAmount -= toDrain;
                if (storedAmount == 0) stored = FluidStack.EMPTY;
                onFluidChanged();
            }
            return copy.isEmpty() ? FluidStack.EMPTY : copy;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (!FluidStack.isSameFluidSameComponents(resource, stored)) return FluidStack.EMPTY;
            return drain(resource.getAmount(), action);
        }

        @Override
        public int getTankCapacity(int tank) {
            return GTMath.saturatedCast(maxAmount);
        }

        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return filter.test(stack);
        }

        public void exportToNearby(Direction... facings) {
            if (stored.isEmpty()) return;
            var level = getMachine().getLevel();
            var pos = getMachine().getBlockPos();
            for (Direction facing : facings) {
                var filter = getMachine().getFluidCapFilter(facing, IO.OUT);
                GTTransferUtils.getAdjacentFluidHandler(level, pos, facing)
                        .ifPresent(adj -> GTTransferUtils.transferFluidsFiltered(this, adj, filter));
            }
        }
    }
}
