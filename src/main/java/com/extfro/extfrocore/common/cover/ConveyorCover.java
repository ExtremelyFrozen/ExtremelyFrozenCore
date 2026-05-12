package com.extfro.extfrocore.common.cover;

import com.extfro.extfrocore.api.EFValues;
import com.extfro.extfrocore.api.capability.IControllable;
import com.extfro.extfrocore.api.capability.ICoverable;
import com.extfro.extfrocore.api.capability.recipe.IO;
import com.extfro.extfrocore.api.cover.CoverBehavior;
import com.extfro.extfrocore.api.cover.CoverDefinition;
import com.extfro.extfrocore.api.cover.IIOCover;
import com.extfro.extfrocore.api.cover.IUICover;
import com.extfro.extfrocore.api.cover.filter.FilterHandler;
import com.extfro.extfrocore.api.cover.filter.FilterHandlers;
import com.extfro.extfrocore.api.cover.filter.ItemFilter;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.gui.widget.EnumSelectorWidget;
import com.extfro.extfrocore.api.machine.ConditionalSubscriptionHandler;
import com.extfro.extfrocore.api.sync_system.annotations.RerenderOnChanged;
import com.extfro.extfrocore.api.sync_system.annotations.SaveField;
import com.extfro.extfrocore.api.sync_system.annotations.SyncToClient;
import com.extfro.extfrocore.api.transfer.item.ItemHandlerDelegate;
import com.extfro.extfrocore.common.blockentity.ItemPipeBlockEntity;
import com.extfro.extfrocore.common.cover.data.DistributionMode;
import com.extfro.extfrocore.common.cover.data.ManualIOMode;
import com.extfro.extfrocore.utils.GTTransferUtils;
import com.extfro.extfrocore.utils.ItemStackHashStrategy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import com.lowdragmc.lowdraglib2.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class ConveyorCover extends CoverBehavior implements IIOCover, IUICover, IControllable {

    // 8 32 128 512 1024
    public static final Int2IntFunction CONVEYOR_SCALING = tier -> 2 * (int) Math.pow(4, Math.min(tier, EFValues.LuV));

    public final int tier;
    public final int maxItemTransferRate;
    @SaveField
    @Getter
    protected int transferRate;
    @SaveField
    @SyncToClient
    @Getter
    @RerenderOnChanged
    protected IO io;
    @SaveField
    @SyncToClient
    @Getter
    protected DistributionMode distributionMode;
    @SaveField
    @SyncToClient
    @Getter
    protected ManualIOMode manualIOMode = ManualIOMode.DISABLED;
    @SaveField
    @SyncToClient
    @Getter
    protected boolean isWorkingEnabled = true;
    protected int itemsLeftToTransferLastSecond;
    private Button ioModeSwitch;

    @SaveField
    @SyncToClient
    @Getter
    protected final FilterHandler<ItemStack, ItemFilter> filterHandler;
    protected final ConditionalSubscriptionHandler subscriptionHandler;

    public ConveyorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide, int tier,
                         int maxTransferRate) {
        super(definition, coverHolder, attachedSide);
        this.tier = tier;
        this.maxItemTransferRate = maxTransferRate;
        this.transferRate = maxItemTransferRate;
        this.itemsLeftToTransferLastSecond = transferRate;
        this.io = IO.OUT;
        this.distributionMode = DistributionMode.INSERT_FIRST;

        subscriptionHandler = new ConditionalSubscriptionHandler(coverHolder, this::update, this::isSubscriptionActive);
        filterHandler = FilterHandlers.item(this)
                .onFilterLoaded(f -> configureFilter())
                .onFilterUpdated(f -> configureFilter())
                .onFilterRemoved(f -> configureFilter());
    }

    @Override
    public int getTransferRate() {
        return transferRate;
    }

    @Override
    public IO getIo() {
        return io;
    }

    public DistributionMode getDistributionMode() {
        return distributionMode;
    }

    @Override
    public ManualIOMode getManualIOMode() {
        return manualIOMode;
    }

    @Override
    public boolean isWorkingEnabled() {
        return isWorkingEnabled;
    }

    public FilterHandler<ItemStack, ItemFilter> getFilterHandler() {
        return filterHandler;
    }

    public ConveyorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide, int tier) {
        this(definition, coverHolder, attachedSide, tier, CONVEYOR_SCALING.applyAsInt(tier));
    }

    protected boolean isSubscriptionActive() {
        return isWorkingEnabled() && getAdjacentItemHandler() != null;
    }

    protected @Nullable IItemHandlerModifiable getOwnItemHandler() {
        return coverHolder.getItemHandlerCap(attachedSide, false);
    }

    protected @Nullable IItemHandler getAdjacentItemHandler() {
        return GTTransferUtils.getAdjacentItemHandler(coverHolder.getLevel(), coverHolder.getBlockPos(), attachedSide)
                .orElse(null);
    }

    public void setDistributionMode(DistributionMode mode) {
        distributionMode = mode;
        syncDataHolder.markClientSyncFieldDirty("distributionMode");
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////

    @Override
    public boolean canAttach() {
        return super.canAttach() && getOwnItemHandler() != null;
    }

    public void setTransferRate(int transferRate) {
        if (transferRate <= maxItemTransferRate) {
            this.transferRate = transferRate;
        }
    }

    public void setIo(IO io) {
        if (io == IO.IN || io == IO.OUT) {
            this.io = io;
        }
        subscriptionHandler.updateSubscription();
    }

    protected void setManualIOMode(ManualIOMode manualIOMode) {
        this.manualIOMode = manualIOMode;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        subscriptionHandler.initialize(coverHolder.getLevel());
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        subscriptionHandler.unsubscribe();
    }

    @Override
    public List<ItemStack> getAdditionalDrops() {
        var list = super.getAdditionalDrops();
        if (!filterHandler.getFilterItem().isEmpty()) {
            list.add(filterHandler.getFilterItem());
        }
        return list;
    }

    //////////////////////////////////////
    // ***** Transfer Logic *****//
    //////////////////////////////////////

    @Override
    public void onNeighborChanged(net.minecraft.world.level.block.Block block, BlockPos fromPos, boolean isMoving) {
        subscriptionHandler.updateSubscription();
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        if (this.isWorkingEnabled != isWorkingAllowed) {
            this.isWorkingEnabled = isWorkingAllowed;
            subscriptionHandler.updateSubscription();
        }
    }

    protected void update() {
        long timer = coverHolder.getOffsetTimer();
        if (timer % 5 == 0) {
            if (itemsLeftToTransferLastSecond > 0) {
                var adjacent = getAdjacentItemHandler();
                var self = getOwnItemHandler();

                if (adjacent != null && self != null) {
                    int totalTransferred = switch (io) {
                        case IN -> doTransferItems(adjacent, self, itemsLeftToTransferLastSecond);
                        case OUT -> doTransferItems(self, adjacent, itemsLeftToTransferLastSecond);
                        default -> 0;
                    };
                    this.itemsLeftToTransferLastSecond -= totalTransferred;
                }
            }
            if (timer % 20 == 0) {
                this.itemsLeftToTransferLastSecond = transferRate;
            }
            subscriptionHandler.updateSubscription();
        }
    }

    protected int doTransferItems(IItemHandler sourceInventory, IItemHandler targetInventory, int maxTransferAmount) {
        return moveInventoryItems(sourceInventory, targetInventory, maxTransferAmount);
    }

    protected int moveInventoryItems(IItemHandler sourceInventory, IItemHandler targetInventory,
                                     int maxTransferAmount) {
        ItemFilter filter = filterHandler.getFilter();
        int itemsLeftToTransfer = maxTransferAmount;

        for (int srcIndex = 0; srcIndex < sourceInventory.getSlots(); srcIndex++) {
            ItemStack sourceStack = sourceInventory.extractItem(srcIndex, itemsLeftToTransfer, true);
            if (sourceStack.isEmpty()) {
                continue;
            }

            if (!filter.test(sourceStack)) {
                continue;
            }

            ItemStack remainder = ItemHandlerHelper.insertItem(targetInventory, sourceStack, true);
            int amountToInsert = sourceStack.getCount() - remainder.getCount();

            if (amountToInsert > 0) {
                sourceStack = sourceInventory.extractItem(srcIndex, amountToInsert, false);
                if (!sourceStack.isEmpty()) {
                    ItemHandlerHelper.insertItem(targetInventory, sourceStack, false);
                    itemsLeftToTransfer -= sourceStack.getCount();

                    if (itemsLeftToTransfer == 0) {
                        break;
                    }
                }
            }
        }
        return maxTransferAmount - itemsLeftToTransfer;
    }

    protected static boolean moveInventoryItemsExact(IItemHandler sourceInventory, IItemHandler targetInventory,
                                                     TypeItemInfo itemInfo) {
        // first, compute how much can we extract in reality from the machine,
        // because totalCount is based on what getStackInSlot returns, which may differ from what
        // extractItem() will return
        ItemStack resultStack = itemInfo.itemStack.copy();
        int totalExtractedCount = 0;
        int itemsLeftToExtract = itemInfo.totalCount;

        for (int i = 0; i < itemInfo.slots.size(); i++) {
            int slotIndex = itemInfo.slots.getInt(i);
            ItemStack extractedStack = sourceInventory.extractItem(slotIndex, itemsLeftToExtract, true);
            if (!extractedStack.isEmpty() &&
                    ItemStack.isSameItemSameComponents(resultStack, extractedStack)) {
                totalExtractedCount += extractedStack.getCount();
                itemsLeftToExtract -= extractedStack.getCount();
            }
            if (itemsLeftToExtract == 0) {
                break;
            }
        }
        // if amount of items extracted is not equal to the amount of items we
        // wanted to extract, abort item extraction
        if (totalExtractedCount != itemInfo.totalCount) {
            return false;
        }
        // adjust size of the result stack accordingly
        resultStack.setCount(totalExtractedCount);

        // now, see how much we can insert into destination inventory
        // if we can't insert as much as itemInfo requires, and remainder is empty, abort, abort
        ItemStack remainder = ItemHandlerHelper.insertItem(targetInventory, resultStack, true);
        if (!remainder.isEmpty()) {
            return false;
        }

        // otherwise, perform real insertion and then remove items from the source inventory
        ItemHandlerHelper.insertItem(targetInventory, resultStack, false);

        // perform real extraction of the items from the source inventory now
        itemsLeftToExtract = itemInfo.totalCount;
        for (int i = 0; i < itemInfo.slots.size(); i++) {
            int slotIndex = itemInfo.slots.getInt(i);
            ItemStack extractedStack = sourceInventory.extractItem(slotIndex, itemsLeftToExtract, false);
            if (!extractedStack.isEmpty() &&
                    ItemStack.isSameItemSameComponents(resultStack, extractedStack)) {
                itemsLeftToExtract -= extractedStack.getCount();
            }
            if (itemsLeftToExtract == 0) {
                break;
            }
        }
        return true;
    }

    protected int moveInventoryItems(IItemHandler sourceInventory, IItemHandler targetInventory,
                                     Map<ItemStack, GroupItemInfo> itemInfos, int maxTransferAmount) {
        ItemFilter filter = filterHandler.getFilter();
        int itemsLeftToTransfer = maxTransferAmount;

        for (int i = 0; i < sourceInventory.getSlots(); i++) {
            ItemStack itemStack = sourceInventory.getStackInSlot(i);
            if (itemStack.isEmpty() || !filter.test(itemStack) || !itemInfos.containsKey(itemStack)) {
                continue;
            }

            GroupItemInfo itemInfo = itemInfos.get(itemStack);

            ItemStack extractedStack = sourceInventory.extractItem(i,
                    Math.min(itemInfo.totalCount, itemsLeftToTransfer), true);

            ItemStack remainderStack = ItemHandlerHelper.insertItemStacked(targetInventory, extractedStack, true);
            int amountToInsert = extractedStack.getCount() - remainderStack.getCount();

            if (amountToInsert > 0) {
                extractedStack = sourceInventory.extractItem(i, amountToInsert, false);

                if (!extractedStack.isEmpty()) {

                    ItemHandlerHelper.insertItemStacked(targetInventory, extractedStack, false);
                    itemsLeftToTransfer -= extractedStack.getCount();
                    itemInfo.totalCount -= extractedStack.getCount();

                    if (itemInfo.totalCount == 0) {
                        itemInfos.remove(itemStack);
                        if (itemInfos.isEmpty()) {
                            break;
                        }
                    }
                    if (itemsLeftToTransfer == 0) {
                        break;
                    }
                }
            }
        }
        return maxTransferAmount - itemsLeftToTransfer;
    }

    @NotNull
    protected Map<ItemStack, TypeItemInfo> countInventoryItemsByType(@NotNull IItemHandler inventory) {
        ItemFilter filter = filterHandler.getFilter();
        Map<ItemStack, TypeItemInfo> result = new Object2ObjectOpenCustomHashMap<>(
                ItemStackHashStrategy.comparingAllButCount());

        for (int srcIndex = 0; srcIndex < inventory.getSlots(); srcIndex++) {
            ItemStack itemStack = inventory.getStackInSlot(srcIndex);
            if (itemStack.isEmpty() || !filter.test(itemStack)) {
                continue;
            }

            var itemInfo = result.computeIfAbsent(itemStack, s -> new TypeItemInfo(s, new IntArrayList(), 0));

            itemInfo.totalCount += itemStack.getCount();
            itemInfo.slots.add(srcIndex);
        }

        return result;
    }

    @NotNull
    protected Map<ItemStack, GroupItemInfo> countInventoryItemsByMatchSlot(@NotNull IItemHandler inventory) {
        ItemFilter filter = filterHandler.getFilter();
        Map<ItemStack, GroupItemInfo> result = new Object2ObjectOpenCustomHashMap<>(
                ItemStackHashStrategy.comparingAllButCount());

        for (int srcIndex = 0; srcIndex < inventory.getSlots(); srcIndex++) {
            ItemStack itemStack = inventory.getStackInSlot(srcIndex);
            if (itemStack.isEmpty() || !filter.test(itemStack)) {
                continue;
            }

            var itemInfo = result.computeIfAbsent(itemStack, s -> new GroupItemInfo(s, 0));

            itemInfo.totalCount += itemStack.getCount();
        }
        return result;
    }

    protected static class TypeItemInfo {

        public final ItemStack itemStack;
        public final IntList slots;
        public int totalCount;

        public TypeItemInfo(ItemStack itemStack, IntList slots, int totalCount) {
            this.itemStack = itemStack;
            this.slots = slots;
            this.totalCount = totalCount;
        }
    }

    protected static class GroupItemInfo {

        public final ItemStack itemStack;
        public int totalCount;

        public GroupItemInfo(ItemStack itemStack, int totalCount) {
            this.itemStack = itemStack;
            this.totalCount = totalCount;
        }
    }

    public boolean shouldRespectDistributionMode() {
        return ((io == IO.IN) ?
                (coverHolder.getLevel().getBlockEntity(coverHolder.getBlockPos()) instanceof ItemPipeBlockEntity) :
                (coverHolder.getLevel().getBlockEntity(coverHolder.getBlockPos()
                        .relative(attachedSide)) instanceof ItemPipeBlockEntity));
    }

    //////////////////////////////////////
    // *********** GUI ***********//
    //////////////////////////////////////
    @Override
    public UIElement createUIElement() {
        final var group = new UIElement().layout(layout -> layout.width(176).height(137));
        group.addChild(label(10, 5, Component.translatable(getUITitle(), EFValues.VN[tier]), 156));

        group.addChild(intInput(10, 20, 156, transferRate, 1, maxItemTransferRate, this::setTransferRate));

        final EnumSelectorWidget<DistributionMode> distributionSelector = new EnumSelectorWidget<>(146, 67, 20, 20,
                DistributionMode.values(), distributionMode, this::setDistributionMode);

        distributionSelector.setVisible(shouldRespectDistributionMode());
        group.addChild(distributionSelector);

        ioModeSwitch = button(10, 45, 20, 20);
        ioModeSwitch.setOnServerClick(event -> {
            setIo(io == IO.IN ? IO.OUT : IO.IN);
            updateIOModeSwitch();
        });
        updateIOModeSwitch();
        group.addChild(ioModeSwitch);

        if (shouldDisplayDistributionMode()) {
            group.addChild(new EnumSelectorWidget<>(146, 67, 20, 20,
                    DistributionMode.VALUES, distributionMode, this::setDistributionMode));
        }

        group.addChild(new EnumSelectorWidget<>(146, 107, 20, 20,
                ManualIOMode.VALUES, manualIOMode, this::setManualIOMode));

        group.addChild(filterHandler.createFilterSlotUI(125, 108));
        group.addChild(filterHandler.createFilterConfigUI(10, 72, 156, 60));

        buildAdditionalUI(group);

        return group;
    }

    protected Label label(int x, int y, Component component, int width) {
        Label label = new Label();
        label.setValue(component);
        label.layout(layout -> layout.left(x).top(y).width(width).height(10));
        label.textStyle(style -> style.textColor(0x404040).textShadow(false));
        return label;
    }

    protected Label label(int x, int y, String translationKey, int width) {
        return label(x, y, Component.translatable(translationKey), width);
    }

    protected TextField intInput(int x, int y, int width, int value, int min, int max, Consumer<Integer> setter) {
        TextField field = new TextField();
        field.layout(layout -> layout.left(x).top(y).width(width).height(20));
        field.style(style -> style.background(GuiTextures.DISPLAY));
        field.textFieldStyle(style -> style.textColor(0x404040).textShadow(false));
        field.setNumbersOnlyInt(min, max);
        field.setText(String.valueOf(value));
        field.setTextResponder(text -> {
            if (!text.isBlank()) {
                setter.accept(Integer.parseInt(text));
            }
        });
        return field;
    }

    protected Button button(int x, int y, int width, int height) {
        Button button = new Button().noText();
        button.layout(layout -> layout.left(x).top(y).width(width).height(height));
        return button;
    }

    protected Button toggleButton(int x, int y, int width, int height, IGuiTexture icon, BooleanSupplier getter,
                                  Consumer<Boolean> setter) {
        Button button = button(x, y, width, height);
        Consumer<Button> update = b -> {
            IGuiTexture texture = new GuiTextureGroup(GuiTextures.VANILLA_BUTTON.copy()
                    .setColor(getter.getAsBoolean() ? 0xffa0ffa0 : -1), icon);
            b.buttonStyle(style -> style.baseTexture(texture).hoverTexture(texture).pressedTexture(texture));
        };
        update.accept(button);
        button.setOnServerClick(event -> {
            setter.accept(!getter.getAsBoolean());
            update.accept(button);
        });
        return button;
    }

    private void updateIOModeSwitch() {
        if (ioModeSwitch == null) {
            return;
        }
        IGuiTexture texture = new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, io.icon);
        ioModeSwitch.buttonStyle(style -> style.baseTexture(texture).hoverTexture(texture).pressedTexture(texture));
        ioModeSwitch.style(style -> style.tooltips(Component.translatable("cover.conveyor.mode",
                Component.translatable(io.tooltip))));
    }

    private boolean shouldDisplayDistributionMode() {
        return coverHolder.getLevel().getBlockEntity(coverHolder.getBlockPos()) instanceof ItemPipeBlockEntity ||
                coverHolder.getLevel()
                        .getBlockEntity(
                                coverHolder.getBlockPos().relative(attachedSide)) instanceof ItemPipeBlockEntity;
    }

    @NotNull
    protected String getUITitle() {
        return "cover.conveyor.title";
    }

    protected void buildAdditionalUI(UIElement group) {
        // Do nothing in the base implementation. This is intended to be overridden by subclasses.
    }

    protected void configureFilter() {
        // Do nothing in the base implementation. This is intended to be overridden by subclasses.
    }

    /////////////////////////////////////
    // *** CAPABILITY OVERRIDE ***//
    /////////////////////////////////////

    private CoverableItemHandlerWrapper itemHandlerWrapper;

    @Nullable
    @Override
    public IItemHandlerModifiable getItemHandlerCap(@Nullable IItemHandlerModifiable defaultValue) {
        if (defaultValue == null) {
            return null;
        }
        if (itemHandlerWrapper == null || itemHandlerWrapper.delegate != defaultValue) {
            this.itemHandlerWrapper = new CoverableItemHandlerWrapper(defaultValue);
        }
        return itemHandlerWrapper;
    }

    private class CoverableItemHandlerWrapper extends ItemHandlerDelegate {

        public CoverableItemHandlerWrapper(IItemHandlerModifiable delegate) {
            super(delegate);
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (io == IO.OUT) {
                if (manualIOMode == ManualIOMode.DISABLED) {
                    return stack;
                }
                if (manualIOMode == ManualIOMode.UNFILTERED) {
                    return super.insertItem(slot, stack, simulate);
                }
            }
            if (!filterHandler.test(stack)) {
                return stack;
            }
            return super.insertItem(slot, stack, simulate);
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (io == IO.IN) {
                if (manualIOMode == ManualIOMode.DISABLED) {
                    return ItemStack.EMPTY;
                }
                if (manualIOMode == ManualIOMode.UNFILTERED) {
                    return super.extractItem(slot, amount, simulate);
                }
            }
            ItemStack result = super.extractItem(slot, amount, true);
            if (result.isEmpty() || !filterHandler.test(result)) {
                return ItemStack.EMPTY;
            }
            return simulate ? result : super.extractItem(slot, amount, false);
        }
    }

    @Override
    public CompoundTag copyConfig(CompoundTag tag) {
        tag.putInt("transferRate", getTransferRate());
        tag.putInt("io", getIo().ordinal());
        tag.putInt("distributionMode", getDistributionMode().ordinal());
        tag.putInt("manualIO", getManualIOMode().ordinal());
        tag.put("filter", filterHandler.getFilterItem().save(coverHolder.getLevel().registryAccess()));
        return super.copyConfig(tag);
    }

    @Override
    public void pasteConfig(ServerPlayer player, CompoundTag tag) {
        setTransferRate(tag.getInt("transferRate"));
        setIo(IO.values()[tag.getInt("io")]);
        setDistributionMode(DistributionMode.values()[tag.getInt("distributionMode")]);
        setManualIOMode(ManualIOMode.values()[tag.getInt("manualIO")]);
        filterHandler.setFilterItem(ItemStack.parse(coverHolder.getLevel().registryAccess(), tag.getCompound("filter"))
                .orElse(ItemStack.EMPTY));
        super.pasteConfig(player, tag);
    }
}
