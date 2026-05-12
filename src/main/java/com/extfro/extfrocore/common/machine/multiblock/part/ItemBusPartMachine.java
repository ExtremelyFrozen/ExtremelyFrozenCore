package com.extfro.extfrocore.common.machine.multiblock.part;

import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.blockentity.IPaintable;
import com.extfro.extfrocore.api.capability.recipe.IO;
import com.extfro.extfrocore.api.cover.filter.FilterHandler;
import com.extfro.extfrocore.api.cover.filter.FilterHandlers;
import com.extfro.extfrocore.api.cover.filter.ItemFilter;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.gui.fancy.ConfiguratorPanel;
import com.extfro.extfrocore.api.gui.widget.SlotWidget;
import com.extfro.extfrocore.api.machine.MachineDefinition;
import com.extfro.extfrocore.api.machine.TickableSubscription;
import com.extfro.extfrocore.api.machine.fancyconfigurator.CircuitFancyConfigurator;
import com.extfro.extfrocore.api.machine.feature.IHasCircuitSlot;
import com.extfro.extfrocore.api.machine.feature.multiblock.IDistinctPart;
import com.extfro.extfrocore.api.machine.multiblock.MultiblockControllerMachine;
import com.extfro.extfrocore.api.machine.multiblock.part.TieredIOPartMachine;
import com.extfro.extfrocore.api.machine.trait.NotifiableItemStackHandler;
import com.extfro.extfrocore.api.sync_system.annotations.SaveField;
import com.extfro.extfrocore.api.sync_system.annotations.SyncToClient;
import com.extfro.extfrocore.common.data.GTMachines;
import com.extfro.extfrocore.common.item.behavior.IntCircuitBehaviour;
import com.extfro.extfrocore.config.ConfigHolder;
import com.extfro.extfrocore.utils.ExtendedUseOnContext;
import com.extfro.extfrocore.utils.GTTransferUtils;
import com.extfro.extfrocore.utils.ISubscription;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import com.lowdragmc.lowdraglib2.gui.widget.Widget;
import com.lowdragmc.lowdraglib2.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib2.integration.xei.IngredientIO;
import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

public class ItemBusPartMachine extends TieredIOPartMachine
                                implements IDistinctPart, IHasCircuitSlot, IPaintable {

    @Getter
    @SaveField
    private final NotifiableItemStackHandler inventory;
    @Nullable
    protected TickableSubscription autoIOSubs;
    @Nullable
    protected ISubscription inventorySubs;
    @Getter(AccessLevel.PROTECTED)
    private boolean hasCircuitSlot = true;
    @Getter
    @SaveField
    @SyncToClient
    protected boolean circuitSlotEnabled;
    @Getter
    @SaveField
    protected final NotifiableItemStackHandler circuitInventory;
    @Getter
    @SaveField
    @SyncToClient
    private boolean isDistinct = false;
    @SaveField
    @SyncToClient
    @Getter
    protected final FilterHandler<ItemStack, ItemFilter> filterHandler;

    public ItemBusPartMachine(BlockEntityCreationInfo info, int tier, IO io) {
        super(info, tier, io);
        this.inventory = attachTrait(createInventory());
        this.circuitSlotEnabled = true;
        this.circuitInventory = attachTrait(createCircuitItemHandler(io)).shouldSearchContent(false);
        filterHandler = FilterHandlers.item(this);

        inventory.setFilter(this::matchesFilter);
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////

    protected int getInventorySize() {
        int sizeRoot = 1 + Math.min(9, getTier());
        return sizeRoot * sizeRoot;
    }

    protected NotifiableItemStackHandler createInventory() {
        return new NotifiableItemStackHandler(getInventorySize(), io);
    }

    protected boolean matchesFilter(ItemStack stack) {
        if (filterHandler.isFilterPresent())
            return filterHandler.getFilter().test(stack);
        return true;
    }

    protected NotifiableItemStackHandler createCircuitItemHandler(IO io) {
        if (io == IO.IN) {
            return new NotifiableItemStackHandler(1, IO.IN, IO.NONE)
                    .setFilter(IntCircuitBehaviour::isIntegratedCircuit)
                    .shouldDropInventoryInWorld(!ConfigHolder.INSTANCE.machines.ghostCircuit);
        } else {
            hasCircuitSlot = false;
            setCircuitSlotEnabled(false);
            return new NotifiableItemStackHandler(0, IO.NONE);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0, this::updateInventorySubscription));
        }
        getHandlerList().setDistinct(isDistinct);
        getHandlerList().setColor(getPaintingColor());
        inventorySubs = getInventory().addChangedListener(this::updateInventorySubscription);
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (inventorySubs != null) {
            inventorySubs.unsubscribe();
            inventorySubs = null;
        }
    }

    @Override
    public void onPaintingColorChanged(int color) {
        getHandlerList().setColor(color, true);
    }

    @Override
    public void setDistinct(boolean distinct) {
        isDistinct = (io != IO.OUT && distinct);
        syncDataHolder.markClientSyncFieldDirty("isDistinct");
        getHandlerList().setDistinctAndNotify(isDistinct);
    }

    @Override
    public void addedToController(MultiblockControllerMachine controller) {
        if (hasCircuitSlot && !controller.allowCircuitSlots()) {
            if (!ConfigHolder.INSTANCE.machines.ghostCircuit) {
                circuitInventory.dropInventoryInWorld();
            } else {
                circuitInventory.setStackInSlot(0, ItemStack.EMPTY);
            }
            setCircuitSlotEnabled(false);
        }
        super.addedToController(controller);
    }

    @Override
    public void removedFromController(MultiblockControllerMachine controller) {
        super.removedFromController(controller);
        if (!hasCircuitSlot) return;
        for (var c : controllers) {
            if (!c.allowCircuitSlots()) {
                return;
            }
        }
        setCircuitSlotEnabled(true);
    }

    @Override
    public int tintColor(int index) {
        if (index == 9) return getRealColor();
        return -1;
    }

    public void setCircuitSlotEnabled(boolean enabled) {
        circuitSlotEnabled = enabled;
        syncDataHolder.markClientSyncFieldDirty("circuitSlotEnabled");
    }

    //////////////////////////////////////
    // ******** Auto IO *********//
    //////////////////////////////////////

    @Override
    public void onNeighborChanged(net.minecraft.world.level.block.Block block, BlockPos fromPos, boolean isMoving) {
        super.onNeighborChanged(block, fromPos, isMoving);
        updateInventorySubscription();
    }

    @Override
    public void onRotated(Direction oldFacing, Direction newFacing) {
        super.onRotated(oldFacing, newFacing);
        updateInventorySubscription(newFacing);
    }

    protected void updateInventorySubscription() {
        updateInventorySubscription(getFrontFacing());
    }

    protected void updateInventorySubscription(Direction newFacing) {
        if (isWorkingEnabled() && ((io.support(IO.OUT) && !getInventory().isEmpty()) || io.support(IO.IN)) &&
                GTTransferUtils.hasAdjacentItemHandler(getLevel(), getBlockPos(), newFacing)) {
            autoIOSubs = subscribeServerTick(autoIOSubs, this::autoIO);
        } else if (autoIOSubs != null) {
            autoIOSubs.unsubscribe();
            autoIOSubs = null;
        }
    }

    protected void autoIO() {
        if (getOffsetTimer() % 5 == 0) {
            if (isWorkingEnabled()) {
                if (io == IO.OUT) {
                    getInventory().exportToNearby(getFrontFacing());
                } else if (io == IO.IN) {
                    getInventory().importFromNearby(getFrontFacing());
                } else if (io == IO.BOTH) {
                    getInventory().importFromNearby(getFrontFacing());
                    getInventory().exportToNearby(getFrontFacing().getOpposite());
                }
            }
            updateInventorySubscription();
        }
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        super.setWorkingEnabled(workingEnabled);
        updateInventorySubscription();
    }

    @Override
    protected InteractionResult onScrewdriverClick(ExtendedUseOnContext context) {
        InteractionResult superResult = super.onScrewdriverClick(context);
        if (superResult != InteractionResult.PASS) return superResult;
        if (io == IO.BOTH) return InteractionResult.PASS;
        if (context.getPlayer().isShiftKeyDown()) {
            if (swapIO()) {
                return InteractionResult.sidedSuccess(isRemote());
            }
        }
        return InteractionResult.PASS;
    }

    public boolean swapIO() {
        BlockPos blockPos = getBlockPos();
        MachineDefinition newDefinition = null;
        if (io.support(IO.IN)) {
            newDefinition = GTMachines.ITEM_EXPORT_BUS[this.getTier()];
        } else if (io.support(IO.OUT)) {
            newDefinition = GTMachines.ITEM_IMPORT_BUS[this.getTier()];
        }
        if (newDefinition == null) return false;

        BlockState newBlockState = newDefinition.getBlock().defaultBlockState();
        getLevel().setBlockAndUpdate(blockPos, newBlockState);

        if (getLevel().getBlockEntity(blockPos) instanceof ItemBusPartMachine newMachine) {
            // We don't set the circuit or distinct buses, since
            // that doesn't make sense on an output bus.
            // Furthermore, existing inventory items
            // and conveyors will drop to the floor on block override.
            newMachine.setFrontFacing(this.getFrontFacing());
            newMachine.setUpwardsFacing(this.getUpwardsFacing());
            newMachine.setPaintingColor(this.getPaintingColor());
        }
        return true;
    }

    //////////////////////////////////////
    // ********** GUI ***********//
    //////////////////////////////////////

    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        if (this.io.support(IO.OUT)) {
            IDistinctPart.super.superAttachConfigurators(configuratorPanel);
        } else if (this.io.support(IO.IN)) {
            IDistinctPart.super.attachConfigurators(configuratorPanel);
            if (hasCircuitSlot && isCircuitSlotEnabled()) {
                configuratorPanel.attachConfigurators(new CircuitFancyConfigurator(circuitInventory.storage));
            }
        }
    }

    @Override
    public Widget createUIWidget() {
        int rowSize = (int) Math.sqrt(getInventorySize());
        int colSize = rowSize;
        if (getInventorySize() == 8) {
            rowSize = 4;
            colSize = 2;
        }
        var group = new WidgetGroup(0, 0, 18 * rowSize + 16, 18 * colSize + 16);
        var container = new WidgetGroup(4, 4, 18 * rowSize + 8, 18 * colSize + 8);
        int index = 0;
        if (this.io == IO.OUT) {
            group.addWidget(filterHandler.createFilterSlotUI(71 + (18 * rowSize) / 2, 35 + 9 * rowSize)
                    .setHoverTooltips(Component.translatable("cover.item_filter.title")));
        }
        for (int y = 0; y < colSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                container.addWidget(
                        new SlotWidget(getInventory().storage, index++, 4 + x * 18, 4 + y * 18, true, io.support(IO.IN))
                                .setBackgroundTexture(GuiTextures.SLOT)
                                .setIngredientIO(this.io.support(IO.IN) ? IngredientIO.INPUT : IngredientIO.OUTPUT));
            }
        }

        container.setBackground(GuiTextures.BACKGROUND_INVERSE);
        group.addWidget(container);
        return group;
    }
}
