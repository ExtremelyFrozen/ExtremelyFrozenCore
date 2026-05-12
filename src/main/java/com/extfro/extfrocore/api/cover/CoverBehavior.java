package com.extfro.extfrocore.api.cover;

import com.extfro.extfrocore.api.blockentity.ICopyable;
import com.extfro.extfrocore.api.capability.ICoverable;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.gui.factory.CoverUIFactory;
import com.extfro.extfrocore.api.gui.fancy.IFancyConfigurator;
import com.extfro.extfrocore.api.item.tool.GTToolType;
import com.extfro.extfrocore.api.item.tool.IToolGridHighlight;
import com.extfro.extfrocore.api.machine.MetaMachine;
import com.extfro.extfrocore.api.sync_system.ISyncManaged;
import com.extfro.extfrocore.api.sync_system.ManagedSyncBlockEntity;
import com.extfro.extfrocore.api.sync_system.SyncDataHolder;
import com.extfro.extfrocore.api.sync_system.annotations.SaveField;
import com.extfro.extfrocore.api.sync_system.annotations.SyncToClient;
import com.extfro.extfrocore.api.transfer.fluid.IFluidHandlerModifiable;
import com.extfro.extfrocore.client.renderer.cover.ICoverRenderer;
import com.extfro.extfrocore.client.renderer.cover.IDynamicCoverRenderer;
import com.extfro.extfrocore.utils.ExtendedUseOnContext;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.mojang.datafixers.util.Pair;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Represents a cover instance attached on a specific side of a machine
 */
public abstract class CoverBehavior implements ISyncManaged, IToolGridHighlight, ICopyable {

    protected final SyncDataHolder syncDataHolder = new SyncDataHolder(this);

    public final CoverDefinition coverDefinition;
    public final ICoverable coverHolder;
    public final Direction attachedSide;
    @SaveField
    @SyncToClient
    protected ItemStack attachItem = ItemStack.EMPTY;
    @SaveField
    protected int redstoneSignalOutput = 0;

    public CoverBehavior(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        this.coverDefinition = definition;
        this.coverHolder = coverHolder;
        this.attachedSide = attachedSide;
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////
    public void scheduleRenderUpdate() {
        coverHolder.scheduleRenderUpdate();
    }

    @Override
    public SyncDataHolder getSyncDataHolder() {
        return syncDataHolder;
    }

    public ItemStack getAttachItem() {
        return attachItem;
    }

    public int getRedstoneSignalOutput() {
        return redstoneSignalOutput;
    }

    @Override
    public void markAsChanged() {
        if (coverHolder instanceof ManagedSyncBlockEntity syncEntity) {
            syncEntity.markAsChanged();
        }
    }

    /**
     * Called on server side to check whether cover can be attached to given cover holder.
     * it will be called before {@link CoverBehavior#onAttached(ItemStack, ServerPlayer)}
     *
     * @return true if cover can be attached, false otherwise
     */
    @MustBeInvokedByOverriders
    public boolean canAttach() {
        var machine = MetaMachine.getMachine(coverHolder.getLevel(), coverHolder.getBlockPos());
        return machine == null ||
                (machine.getDefinition().isAllowCoverOnFront() || !machine.hasFrontFacing() ||
                        coverHolder.getFrontFacing() != attachedSide);
    }

    /**
     * Will be called on server side after the cover attachment to the machine
     * Cover can change it's internal state here and return initial data as nbt.
     *
     * @param itemStack the item cover was attached from
     */
    public void onAttached(ItemStack itemStack, @Nullable ServerPlayer player) {
        attachItem = itemStack.copy();
        attachItem.setCount(1);
        syncDataHolder.markClientSyncFieldDirty("attachItem");
    }

    public void onLoad() {}

    public void onUnload() {}

    //////////////////////////////////////
    // ********** Misc ***********//
    //////////////////////////////////////
    public ItemStack getPickItem() {
        return attachItem;
    }

    /**
     * Append additional drops. It doesn't include itself.
     */
    public List<ItemStack> getAdditionalDrops() {
        return new ArrayList<>();
    }

    /**
     * Called prior to cover removing on the server side
     * Will also be called during machine dismantling, as machine loses installed covers after that
     */
    public void onRemoved() {}

    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {}

    public void setRedstoneSignalOutput(int redstoneSignalOutput) {
        if (this.redstoneSignalOutput == redstoneSignalOutput) return;
        this.redstoneSignalOutput = redstoneSignalOutput;
        coverHolder.notifyBlockUpdate();
    }

    public boolean canConnectRedstone() {
        return false;
    }

    //////////////////////////////////////
    // ******* Interaction *******//
    //////////////////////////////////////

    public final Pair<@Nullable GTToolType, InteractionResult> onToolClick(ExtendedUseOnContext context) {
        var toolType = context.getToolType();
        if (toolType.contains(GTToolType.SCREWDRIVER)) {
            return Pair.of(GTToolType.SCREWDRIVER, onScrewdriverClick(context));
        } else if (toolType.contains(GTToolType.SOFT_MALLET)) {
            return Pair.of(GTToolType.SOFT_MALLET, onSoftMalletClick(context));
        }
        return Pair.of(null, InteractionResult.PASS);
    }

    public InteractionResult onScrewdriverClick(ExtendedUseOnContext context) {
        if (this instanceof IUICover) {
            if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
                CoverUIFactory.INSTANCE.openUI(this, serverPlayer);
            }
            return InteractionResult.sidedSuccess(coverHolder.isRemote());
        }
        return InteractionResult.PASS;
    }

    public InteractionResult onSoftMalletClick(ExtendedUseOnContext context) {
        return InteractionResult.PASS;
    }

    //////////////////////////////////////
    // ******* Rendering ********//
    //////////////////////////////////////

    /**
     * @return If the pipe this is placed on and a pipe on the other side should be able to connect
     */
    public boolean canPipePassThrough() {
        return true;
    }

    public boolean shouldRenderPlate() {
        return true;
    }

    public @Nullable Supplier<ICoverRenderer> getCoverRenderer() {
        return coverDefinition.getCoverRenderer();
    }

    public @Nullable IFancyConfigurator getConfigurator() {
        return null;
    }

    @Override
    public boolean shouldRenderGrid(Player player, BlockPos pos, BlockState state, ItemStack held,
                                    Set<GTToolType> toolTypes) {
        return toolTypes.contains(GTToolType.CROWBAR) ||
                ((toolTypes.isEmpty() || toolTypes.contains(GTToolType.SCREWDRIVER)) && this instanceof IUICover);
    }

    @Override
    public @Nullable IGuiTexture sideTips(Player player, BlockPos pos, BlockState state, Set<GTToolType> toolTypes,
                                          ItemStack held, Direction side) {
        if (toolTypes.contains(GTToolType.CROWBAR)) {
            return GuiTextures.TOOL_REMOVE_COVER;
        }
        if ((toolTypes.isEmpty() || toolTypes.contains(GTToolType.SCREWDRIVER)) && this instanceof IUICover) {
            return GuiTextures.TOOL_COVER_SETTINGS;
        }
        return null;
    }

    /**
     * get Appearance. same as IBlockExtension.getAppearance() / IFabricBlock.getAppearance()
     */
    @Nullable
    public BlockState getAppearance(@Nullable BlockState sourceState, @Nullable BlockPos sourcePos) {
        return null;
    }

    public Supplier<IDynamicCoverRenderer> getDynamicRenderer() {
        return () -> null;
    }

    //////////////////////////////////////
    // ******* Capabilities *******//
    //////////////////////////////////////

    @Nullable
    public IItemHandlerModifiable getItemHandlerCap(IItemHandlerModifiable defaultValue) {
        return defaultValue;
    }

    @Nullable
    public IFluidHandlerModifiable getFluidHandlerCap(IFluidHandlerModifiable defaultValue) {
        return defaultValue;
    }
}
