package com.extfro.extfrocore.common.machine.storage;

import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.capability.IControllable;
import com.extfro.extfrocore.api.capability.recipe.IO;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.gui.widget.PhantomSlotWidget;
import com.extfro.extfrocore.api.gui.widget.SlotWidget;
import com.extfro.extfrocore.api.gui.widget.ToggleButtonWidget;
import com.extfro.extfrocore.api.item.datacomponents.LargeItemContent;
import com.extfro.extfrocore.api.item.tool.GTToolType;
import com.extfro.extfrocore.api.machine.MetaMachine;
import com.extfro.extfrocore.api.machine.TieredMachine;
import com.extfro.extfrocore.api.machine.feature.IFancyUIMachine;
import com.extfro.extfrocore.api.machine.trait.AutoOutputTrait;
import com.extfro.extfrocore.api.machine.trait.MachineTrait;
import com.extfro.extfrocore.api.machine.trait.MachineTraitType;
import com.extfro.extfrocore.api.sync_system.annotations.SaveField;
import com.extfro.extfrocore.api.sync_system.annotations.SyncToClient;
import com.extfro.extfrocore.api.transfer.fluid.IFluidHandlerModifiable;
import com.extfro.extfrocore.api.transfer.item.CustomItemStackHandler;
import com.extfro.extfrocore.common.data.item.GTDataComponents;
import com.extfro.extfrocore.common.machine.gui.MachineUIHelper;
import com.extfro.extfrocore.utils.ExtendedUseOnContext;
import com.extfro.extfrocore.utils.FormattingUtil;
import com.extfro.extfrocore.utils.GTMath;
import com.extfro.extfrocore.utils.GTTransferUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import com.lowdragmc.lowdraglib2.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

public class QuantumChestMachine extends TieredMachine implements IControllable,
                                 IFancyUIMachine {

    /**
     * Sourced from FunctionalStorage's
     * <a
     * href=https://github.com/Buuz135/FunctionalStorage/blob/1.21/src/main/java/com/buuz135/functionalstorage/block/tile/ItemControllableDrawerTile.java>
     * ItemControllerDrawerTile</a>
     */
    public static final Object2LongOpenHashMap<UUID> INTERACTION_LOGGER = new Object2LongOpenHashMap<>();

    @SaveField
    private boolean isVoiding;

    private final long maxAmount;
    protected final ItemCache cache;
    @SyncToClient
    @SaveField
    private final CustomItemStackHandler lockedItem;

    @Getter
    @SyncToClient
    @SaveField
    protected ItemStack stored = ItemStack.EMPTY;
    @Getter
    @SyncToClient
    @SaveField
    protected long storedAmount = 0;

    @SaveField
    @SyncToClient
    public final AutoOutputTrait autoOutput;

    public QuantumChestMachine(BlockEntityCreationInfo info, int tier, long maxAmount) {
        super(info, tier);
        this.maxAmount = maxAmount;
        this.cache = createCacheItemHandler();
        this.lockedItem = new CustomItemStackHandler();
        this.autoOutput = AutoOutputTrait.ofItems(this, cache);
        lockedItem.setOnContentsChanged(() -> syncDataHolder.markClientSyncFieldDirty("lockedItem"));
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////

    protected ItemCache createCacheItemHandler() {
        return new ItemCache(this);
    }

    protected void onItemChanged() {
        if (!isRemote()) {
            syncDataHolder.markClientSyncFieldDirty("storedAmount");
            syncDataHolder.markClientSyncFieldDirty("stored");
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);
        LargeItemContent storage = componentInput.getOrDefault(GTDataComponents.LARGE_ITEM_CONTENT,
                LargeItemContent.EMPTY);
        stored = storage.stored();
        storedAmount = storage.amount();
    }

    @Override
    public void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (!stored.isEmpty())
            components.set(GTDataComponents.LARGE_ITEM_CONTENT, new LargeItemContent(stored, storedAmount));
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

    //////////////////////////////////////
    // ******* Auto Output *******//
    //////////////////////////////////////

    @Override
    public boolean isWorkingEnabled() {
        return autoOutput.isAutoOutputItems();
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        autoOutput.setAllowAutoOutputItems(isWorkingAllowed);
    }

    //////////////////////////////////////
    // ******* Interaction *******//
    //////////////////////////////////////

    @Override
    public InteractionResult onUseWithItem(ExtendedUseOnContext context) {
        if (context.getClickedFace() == getFrontFacing() && !isRemote()) {
            var hit = context.getHitResult();

            var aabb = new AABB(hit.getBlockPos()).deflate(0.12);
            var hitVector = hit.getLocation().relative(getFrontFacing(), -0.5);
            if (!aabb.contains(hitVector)) return InteractionResult.PASS;

            var held = context.getItemInHand();
            var player = context.getPlayer();

            if (cache.canInsert(held)) { // push
                var remaining = cache.insertItem(0, held, false);
                player.setItemInHand(InteractionHand.MAIN_HAND, remaining);
                return InteractionResult.SUCCESS;
            } else if (isDoubleHit(player.getUUID())) {
                for (var stack : player.getInventory().items) {
                    if (!stack.isEmpty() && cache.canInsert(stack)) {
                        stack.setCount(cache.insertItem(0, stack, false).getCount());
                    }
                }
            }
            INTERACTION_LOGGER.put(player.getUUID(), System.currentTimeMillis());
            return InteractionResult.SUCCESS;

        }

        return super.onUseWithItem(context);
    }

    private static boolean isDoubleHit(UUID uuid) {
        return (System.currentTimeMillis() - INTERACTION_LOGGER.getLong(uuid)) < 300;
    }

    @Override
    public boolean onLeftClick(Player player, InteractionHand hand,
                               @Nullable Direction direction) {
        if (direction == getFrontFacing() && !isRemote()) {
            if (GTToolType.WRENCH.matchTags.stream().anyMatch(player.getItemInHand(hand)::is)) return false;
            if (!stored.isEmpty()) { // pull
                var drained = cache.extractItem(0, player.isShiftKeyDown() ? stored.getMaxStackSize() : 1, false);
                if (!drained.isEmpty()) {
                    if (!player.addItem(drained)) {
                        net.minecraft.world.level.block.Block.popResourceFromFace(getLevel(), getBlockPos(),
                                getFrontFacing(), drained);
                    }
                }
            }
        }
        return super.onLeftClick(player, hand, direction);
    }

    public boolean isLocked() {
        return !lockedItem.getStackInSlot(0).isEmpty();
    }

    protected void setLocked(boolean locked) {
        if (!stored.isEmpty() && locked) {
            var copied = stored.copyWithCount(1);
            lockedItem.setStackInSlot(0, copied);
        } else if (!locked) {
            lockedItem.setStackInSlot(0, ItemStack.EMPTY);
        }
    }

    public ItemStack getLockedItem() {
        return lockedItem.getStackInSlot(0);
    }

    //////////////////////////////////////
    // *********** GUI ***********//
    //////////////////////////////////////

    public UIElement createUIWidget() {
        var group = MachineUIHelper.group(109, 63)
                .style(style -> style.background(GuiTextures.BACKGROUND_INVERSE));
        var importItems = createImportItems();
        group.addChild(MachineUIHelper.image(4, 4, 81, 55, GuiTextures.DISPLAY));
        group.addChild(MachineUIHelper.label(8, 8, "gtceu.machine.quantum_chest.items_stored"));
        group.addChild(MachineUIHelper.lightLabel(8, 18,
                () -> Component.literal(FormattingUtil.formatNumbers(storedAmount))));
        group.addChild(new SlotWidget(importItems, 0, 87, 5, false, true)
                .setBackgroundTexture(new GuiTextureGroup(GuiTextures.SLOT, GuiTextures.IN_SLOT_OVERLAY)));
        group.addChild(new SlotWidget(cache, 0, 87, 23, false, false)
                .setItemHook(s -> s.copyWithCount((int) Math.min(storedAmount, s.getMaxStackSize())))
                .setBackgroundTexture(GuiTextures.SLOT));
        group.addChild(createExtractButton(87, 42));
        group.addChild(new PhantomSlotWidget(lockedItem, 0, 58, 41,
                stack -> stored.isEmpty() || ItemStack.isSameItemSameComponents(stack, stored))
                .setMaxStackSize(1));
        group.addChild(new ToggleButtonWidget(4, 41, 18, 18,
                GuiTextures.BUTTON_ITEM_OUTPUT, this.autoOutput::isAutoOutputItems,
                this.autoOutput::setAllowAutoOutputItems)
                .setShouldUseBaseBackground()
                .setTooltipText("gtceu.gui.item_auto_output.tooltip"));
        group.addChild(new ToggleButtonWidget(22, 41, 18, 18,
                GuiTextures.BUTTON_LOCK, this::isLocked, this::setLocked)
                .setShouldUseBaseBackground()
                .setTooltipText("gtceu.gui.item_lock.tooltip"));
        group.addChild(new ToggleButtonWidget(40, 41, 18, 18,
                GuiTextures.BUTTON_VOID, () -> isVoiding, (b) -> isVoiding = b)
                .setShouldUseBaseBackground()
                .setTooltipText("gtceu.gui.item_voiding_partial.tooltip"));
        return group;
    }

    private Button createExtractButton(int x, int y) {
        var button = new Button().noText();
        button.layout(layout -> layout.left(x).top(y).width(18).height(18));
        var texture = new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, Icons.DOWN.scale(0.7f));
        button.buttonStyle(style -> style.baseTexture(texture).hoverTexture(texture).pressedTexture(texture));
        button.setOnServerClick(event -> {
            if (!stored.isEmpty()) {
                var extracted = cache.extractItem(0,
                        (int) Math.min(storedAmount, stored.getMaxStackSize()), false);
                var player = button.getModularUI().player;
                if (!player.addItem(extracted)) {
                    net.minecraft.world.level.block.Block.popResource(player.level(), player.getOnPos(), extracted);
                }
            }
        });
        return button;
    }

    private CustomItemStackHandler createImportItems() {
        var importItems = new CustomItemStackHandler();
        importItems.setFilter(cache::canInsert);
        importItems.setOnContentsChanged(() -> {
            var item = importItems.getStackInSlot(0).copy();
            if (!item.isEmpty()) {
                importItems.setStackInSlot(0, ItemStack.EMPTY);
                importItems.onContentsChanged(0);
                cache.insertItem(0, item.copy(), false);
            }
        });
        return importItems;
    }

    //////////////////////////////////////
    // ******* Rendering ********//
    //////////////////////////////////////

    @Override
    public @Nullable IGuiTexture sideTips(Player player, BlockPos pos, BlockState state,
                                          Set<GTToolType> toolTypes, ItemStack held, Direction side) {
        if (toolTypes.contains(GTToolType.SOFT_MALLET)) {
            if (side == getFrontFacing()) return null;
        }
        return super.sideTips(player, pos, state, toolTypes, held, side);
    }

    protected class ItemCache extends MachineTrait implements IItemHandlerModifiable {

        public static final MachineTraitType<ItemCache> TYPE = new MachineTraitType<>(ItemCache.class);

        @Override
        public MachineTraitType<ItemCache> getTraitType() {
            return TYPE;
        }

        private final Predicate<ItemStack> filter = i -> !isLocked() ||
                ItemStack.isSameItemSameComponents(i, getLockedItem());

        public ItemCache(MetaMachine holder) {
            super(holder);
        }

        @Override
        public void setStackInSlot(int index, ItemStack stack) {
            stored = stack.copyWithCount(1);
            storedAmount = stack.getCount();
            onItemChanged();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return stored.copyWithCount(GTMath.saturatedCast(storedAmount));
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            long free = isVoiding ? Long.MAX_VALUE : maxAmount - storedAmount;
            long canStore = 0;
            if ((stored.isEmpty() || ItemStack.isSameItemSameComponents(stored, stack)) && filter.test(stack)) {
                canStore = Math.min(stack.getCount(), free);
            }
            if (!simulate && canStore > 0) {
                if (stored.isEmpty()) stored = stack.copyWithCount(1);
                storedAmount = Math.min(maxAmount, storedAmount + canStore);
                onItemChanged();
            }
            return stack.copyWithCount((int) (stack.getCount() - canStore));
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (stored.isEmpty()) return ItemStack.EMPTY;
            long toExtract = Math.min(storedAmount, amount);
            var copy = stored.copyWithCount((int) toExtract);
            if (!simulate && toExtract > 0) {
                storedAmount -= toExtract;
                if (storedAmount == 0) stored = ItemStack.EMPTY;
                onItemChanged();
            }
            return copy;
        }

        @Override
        public int getSlotLimit(int slot) {
            return GTMath.saturatedCast(maxAmount);
        }

        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return filter.test(stack);
        }

        public void exportToNearby(Direction... facings) {
            if (stored.isEmpty()) return;
            var level = getMachine().getLevel();
            var pos = getMachine().getBlockPos();
            for (Direction facing : facings) {
                var filter = getMachine().getItemCapFilter(facing, IO.OUT);
                GTTransferUtils.getAdjacentItemHandler(level, pos, facing)
                        .ifPresent(adj -> GTTransferUtils.transferItemsFiltered(this, adj, filter));
            }
        }

        public boolean canInsert(ItemStack stack) {
            return filter.test(stack) && (insertItem(0, stack, true).getCount() != stack.getCount());
        }
    }
}
