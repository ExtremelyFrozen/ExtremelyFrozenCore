package com.extfro.extfrocore.common.cover.ender;

import com.extfro.extfrocore.api.capability.ICoverable;
import com.extfro.extfrocore.api.cover.CoverDefinition;
import com.extfro.extfrocore.api.cover.filter.FilterHandler;
import com.extfro.extfrocore.api.cover.filter.FilterHandlers;
import com.extfro.extfrocore.api.cover.filter.FluidFilter;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.misc.virtualregistry.EntryTypes;
import com.extfro.extfrocore.api.misc.virtualregistry.VirtualEnderRegistry;
import com.extfro.extfrocore.api.misc.virtualregistry.VirtualEntry;
import com.extfro.extfrocore.api.misc.virtualregistry.entries.VirtualTank;
import com.extfro.extfrocore.api.sync_system.annotations.SaveField;
import com.extfro.extfrocore.api.sync_system.annotations.SyncToClient;
import com.extfro.extfrocore.api.transfer.fluid.IFluidHandlerModifiable;
import com.extfro.extfrocore.utils.GTTransferUtils;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.FluidSlot;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class EnderFluidLinkCover extends AbstractEnderLinkCover<VirtualTank> {

    public static final int TRANSFER_RATE = 8000; // mB/t

    @SaveField
    @SyncToClient
    protected VirtualTank visualTank;

    @Getter
    @SaveField
    @SyncToClient
    protected final FilterHandler<FluidStack, FluidFilter> filterHandler;
    protected int mBLeftToTransferLastSecond;

    public EnderFluidLinkCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
        this.mBLeftToTransferLastSecond = TRANSFER_RATE * 20;
        filterHandler = FilterHandlers.fluid(this);
        if (!isRemote()) setEntry(VirtualEnderRegistry.getInstance()
                .getOrCreateEntry(getOwner(), EntryTypes.ENDER_FLUID, getChannelName()));
    }

    @Override
    protected VirtualTank getEntry() {
        return visualTank;
    }

    @Override
    protected void setEntry(VirtualEntry entry) {
        visualTank = (VirtualTank) entry;
        syncDataHolder.markClientSyncFieldDirty("visualTank");
    }

    @Override
    public boolean canAttach() {
        return FluidUtil.getFluidHandler(coverHolder.getLevel(), coverHolder.getBlockPos(), attachedSide).isPresent();
    }

    @Override
    protected EntryTypes<VirtualTank> getEntryType() {
        return EntryTypes.ENDER_FLUID;
    }

    @Override
    protected String identifier() {
        return "EFLink#";
    }

    @Override
    protected void transfer() {
        long timer = coverHolder.getOffsetTimer();
        if (mBLeftToTransferLastSecond > 0) {
            int platformTransferredFluid = doTransferFluids(mBLeftToTransferLastSecond);
            this.mBLeftToTransferLastSecond -= platformTransferredFluid;
        }

        if (timer % 20 == 0) {
            this.mBLeftToTransferLastSecond = TRANSFER_RATE * 20;
        }
    }

    protected @Nullable IFluidHandlerModifiable getOwnFluidHandler() {
        return coverHolder.getFluidHandlerCap(attachedSide, false);
    }

    private int doTransferFluids(int platformTransferLimit) {
        var ownFluidHandler = getOwnFluidHandler();

        if (ownFluidHandler != null) {
            return switch (io) {
                case IN -> GTTransferUtils.transferFluidsFiltered(ownFluidHandler, visualTank.getFluidTank(),
                        filterHandler.getFilter(), platformTransferLimit);
                case OUT -> GTTransferUtils.transferFluidsFiltered(visualTank.getFluidTank(), ownFluidHandler,
                        filterHandler.getFilter(), platformTransferLimit);
                default -> 0;
            };

        }
        return 0;
    }

    @Override
    public CompoundTag copyConfig(CompoundTag tag) {
        tag.put("filter", filterHandler.getFilterItem().save(coverHolder.getLevel().registryAccess()));
        return super.copyConfig(tag);
    }

    @Override
    public void pasteConfig(ServerPlayer player, CompoundTag tag) {
        filterHandler.setFilterItem(
                ItemStack.parseOptional(coverHolder.getLevel().registryAccess(), tag.getCompound("filter")));
        super.pasteConfig(player, tag);
    }

    @Override
    public @NotNull List<ItemStack> getAdditionalDrops() {
        var list = super.getAdditionalDrops();
        if (!filterHandler.getFilterItem().isEmpty()) {
            list.add(filterHandler.getFilterItem());
        }
        return list;
    }

    //////////////////////////////////////
    // ************ GUI ************ //
    //////////////////////////////////////

    @Override
    protected UIElement addVirtualEntryWidget(VirtualEntry entry, int x, int y, int width, int height, boolean canClick) {
        FluidSlot slot = new FluidSlot();
        slot.bind(((VirtualTank) entry).getFluidTank(), 0);
        slot.layout(layout -> layout.left(x).top(y).width(width).height(height));
        slot.style(style -> style.background(GuiTextures.FLUID_SLOT));
        slot.setAllowClickFilled(canClick);
        slot.setAllowClickDrained(canClick);
        return slot;
    }

    @NotNull
    @Override
    protected String getUITitle() {
        return "cover.ender_fluid_link.title";
    }
}
