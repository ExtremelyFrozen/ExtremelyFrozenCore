package com.extfro.extfrocore.common.machine.electric;

import com.extfro.extfrocore.api.EFValues;
import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.capability.GTCapabilityHelper;
import com.extfro.extfrocore.api.capability.IWorkable;
import com.extfro.extfrocore.api.capability.recipe.IO;
import com.extfro.extfrocore.api.cover.filter.ItemFilter;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.machine.TickableSubscription;
import com.extfro.extfrocore.api.machine.TieredEnergyMachine;
import com.extfro.extfrocore.api.machine.feature.IFancyUIMachine;
import com.extfro.extfrocore.api.machine.property.GTMachineModelProperties;
import com.extfro.extfrocore.api.machine.trait.AutoOutputTrait;
import com.extfro.extfrocore.api.machine.trait.NotifiableItemStackHandler;
import com.extfro.extfrocore.api.sync_system.annotations.RerenderOnChanged;
import com.extfro.extfrocore.api.sync_system.annotations.SaveField;
import com.extfro.extfrocore.api.sync_system.annotations.SyncToClient;
import com.extfro.extfrocore.api.transfer.item.CustomItemStackHandler;
import com.extfro.extfrocore.common.data.GTItems;
import com.extfro.extfrocore.config.ConfigHolder;
import com.extfro.extfrocore.utils.ISubscription;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import com.lowdragmc.lowdraglib2.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author h3tr
 * @date 2023/7/13
 * @implNote FisherMachine
 */
public class ItemCollectorMachine extends TieredEnergyMachine
                                  implements IFancyUIMachine, IWorkable {

    @Getter
    private static final int[] INVENTORY_SIZES = { 4, 9, 16, 25, 25 };
    private static final double MOTION_MULTIPLIER = 0.04;
    private static final int BASE_EU_CONSUMPTION = 6;

    @SaveField
    protected final NotifiableItemStackHandler output;

    @Getter
    @SaveField
    protected final CustomItemStackHandler chargerInventory;
    @SaveField
    protected final CustomItemStackHandler filterInventory;

    @Nullable
    protected TickableSubscription batterySubs, collectionSubs;
    @Nullable
    protected ISubscription energySubs;
    private final long energyPerTick;

    private final int inventorySize;

    private @Nullable AABB aabb;

    @SaveField
    @Getter
    @SyncToClient
    private int range;

    private boolean rangeDirty = false;

    private final int maxRange;

    @Getter
    @SaveField
    @SyncToClient
    private boolean isWorkingEnabled = true;

    @SyncToClient
    @SaveField
    @Getter
    @RerenderOnChanged
    private boolean active = false;

    @SaveField
    @SyncToClient
    public final AutoOutputTrait autoOutput;

    public ItemCollectorMachine(BlockEntityCreationInfo info, int tier) {
        super(info, tier);
        this.inventorySize = INVENTORY_SIZES[Mth.clamp(getTier(), 0, INVENTORY_SIZES.length - 1)];
        this.energyPerTick = (long) BASE_EU_CONSUMPTION * (1L << (tier - 1));
        this.output = attachTrait(createOutputItemHandler());
        this.chargerInventory = createChargerItemHandler();
        this.filterInventory = createFilterItemHandler();
        environmentalExplosionTrait.setEnableEnvironmentalExplosions(false);
        this.autoOutput = attachTrait(AutoOutputTrait.ofItems(output));
        maxRange = (int) Math.pow(2, tier + 2);
        range = maxRange;
    }

    //////////////////////////////////////
    // ***** Initialization *****//
    //////////////////////////////////////

    protected CustomItemStackHandler createChargerItemHandler() {
        var handler = new CustomItemStackHandler();
        handler.setFilter(item -> GTCapabilityHelper.getElectricItem(item) != null ||
                (ConfigHolder.INSTANCE.compat.energy.nativeEUToFE &&
                        GTCapabilityHelper.getForgeEnergyItem(item) != null));
        return handler;
    }

    protected CustomItemStackHandler createFilterItemHandler() {
        var handler = new CustomItemStackHandler();
        handler.setFilter(
                item -> item.is(GTItems.ITEM_FILTER.asItem()) || item.is(GTItems.TAG_FILTER.asItem()));
        return handler;
    }

    protected NotifiableItemStackHandler createOutputItemHandler() {
        return new NotifiableItemStackHandler(inventorySize, IO.BOTH, IO.OUT);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (isRemote()) return;

        if (getLevel() instanceof ServerLevel serverLevel) {

            serverLevel.getServer().tell(new TickTask(0, this::updateCollectionSubscription));
        }

        energySubs = energyContainer.addChangedListener(() -> {
            this.updateBatterySubscription();
            this.updateCollectionSubscription();
        });
        chargerInventory.setOnContentsChanged(this::updateBatterySubscription);
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (energySubs != null) {
            energySubs.unsubscribe();
            energySubs = null;
        }
    }

    @Override
    public void onMachineDestroyed() {
        super.onMachineDestroyed();
        chargerInventory.dropInventoryInWorld(getLevel(), getBlockPos());
    }

    //////////////////////////////////////
    // ********* Logic **********//
    //////////////////////////////////////

    public void updateCollectionSubscription() {
        if (drainEnergy(true) && isWorkingEnabled) {
            collectionSubs = subscribeServerTick(collectionSubs, this::update);
            setActive(true);
            active = true;
        } else if (collectionSubs != null) {
            collectionSubs.unsubscribe();
            collectionSubs = null;
            active = false;
        }
    }

    public void setActive(boolean active) {
        this.active = active;
        setRenderState(getRenderState().setValue(GTMachineModelProperties.IS_ACTIVE, active));
    }

    public void update() {
        if (drainEnergy(false)) {
            if (aabb == null || rangeDirty) {
                rangeDirty = false;
                BlockPos pos1 = getBlockPos().offset(-range, 0, -range);
                BlockPos pos2 = getBlockPos().offset(range, 2, range);
                this.aabb = AABB.of(BoundingBox.fromCorners(pos1, pos2));
            }
            moveItemsInRange();
            updateCollectionSubscription();
        }
    }

    public void moveItemsInRange() {
        ItemFilter filter = null;
        if (!filterInventory.getStackInSlot(0).isEmpty())
            filter = ItemFilter.loadFilter(filterInventory.getStackInSlot(0));
        BlockPos centerPos = self().getBlockPos().above();

        List<ItemEntity> itemEntities = getLevel().getEntitiesOfClass(ItemEntity.class, aabb);
        for (ItemEntity itemEntity : itemEntities) {
            if (!itemEntity.isAlive()) continue;
            if (filter != null && !filter.test(itemEntity.getItem())) continue;
            double distX = (centerPos.getX() + 0.5) - itemEntity.position().x;
            double distZ = (centerPos.getZ() + 0.5) - itemEntity.position().z;
            double dist = Math.sqrt(Math.pow(distX, 2) + Math.pow(distZ, 2));
            if (dist >= 0.7f) {
                // ItemEntity.INFINITE_PICKUP_DELAY = 32767
                if (itemEntity.pickupDelay == 32767) continue;
                double dirX = distX / dist;
                double dirZ = distZ / dist;
                Vec3 delta = itemEntity.getDeltaMovement();
                itemEntity.setDeltaMovement(dirX * MOTION_MULTIPLIER * tier, delta.y, dirZ * MOTION_MULTIPLIER * tier);
                itemEntity.setPickUpDelay(1);
            } else {
                ItemStack stack = itemEntity.getItem();
                if (!canFillOutput(stack)) continue;

                ItemStack remainder = fillOutput(stack);
                if (remainder.isEmpty())
                    itemEntity.kill();
                else if (stack.getCount() > remainder.getCount())
                    itemEntity.setItem(remainder);
            }
        }
    }

    private boolean canFillOutput(ItemStack stack) {
        for (int i = 0; i < output.getSlots(); i++) {
            if (output.insertItemInternal(i, stack, true).getCount() < stack.getCount())
                return true;
        }

        return false;
    }

    private ItemStack fillOutput(ItemStack stack) {
        for (int i = 0; i < output.getSlots(); i++) {
            if (output.insertItemInternal(i, stack, true).getCount() < stack.getCount())
                return output.insertItemInternal(i, stack, false);
        }

        return ItemStack.EMPTY;
    }

    public boolean drainEnergy(boolean simulate) {
        long resultEnergy = energyContainer.getEnergyStored() - energyPerTick;
        if (resultEnergy >= 0L && resultEnergy <= energyContainer.getEnergyCapacity()) {
            if (!simulate)
                energyContainer.removeEnergy(energyPerTick);
            return true;
        }
        return false;
    }

    protected void updateBatterySubscription() {
        if (energyContainer.dischargeOrRechargeEnergyContainers(chargerInventory, 0, true))
            batterySubs = subscribeServerTick(batterySubs, this::chargeBattery);
        else if (batterySubs != null) {
            batterySubs.unsubscribe();
            batterySubs = null;
        }
    }

    protected void chargeBattery() {
        if (!energyContainer.dischargeOrRechargeEnergyContainers(chargerInventory, 0, false)) {
            updateBatterySubscription();
        }
    }

    @Override
    public int getProgress() {
        return 0;
    }

    @Override
    public int getMaxProgress() {
        return 0;
    }

    public void setRange(int range) {
        this.range = range;
        syncDataHolder.markClientSyncFieldDirty("range");
        rangeDirty = true;
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        isWorkingEnabled = workingEnabled;
        syncDataHolder.markClientSyncFieldDirty("isWorkingEnabled");
        updateCollectionSubscription();
    }

    //////////////////////////////////////
    // ********** GUI ***********//
    //////////////////////////////////////

    @Override
    public UIElement createUIWidget() {
        int rowSize = (int) Math.sqrt(inventorySize);
        int templateWidth = rowSize * 18 + 8 + 25;
        int templateHeight = rowSize * 18 + 8;
        UIElement template = panel(templateWidth, templateHeight, GuiTextures.BACKGROUND_INVERSE);
        for (int y = 0; y < rowSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                int index = y * rowSize + x;
                template.addChild(itemSlot(output, index, 24 + x * 18, 4 + y * 18, GuiTextures.SLOT, true, false));
            }
        }
        template.addChild(itemSlot(filterInventory, 0, 4, (templateHeight - 18) / 2,
                new GuiTextureGroup(GuiTextures.SLOT, GuiTextures.FILTER_SLOT_OVERLAY), true, true));

        UIElement energyGroup = new UIElement().layout(layout -> layout.width(18).height(79));
        energyGroup.addChild(createEnergyBar(this));
        energyGroup.addChild(itemSlot(chargerInventory, 0, 0, 61,
                new GuiTextureGroup(GuiTextures.SLOT, GuiTextures.CHARGER_OVERLAY), true, true,
                Component.translatable("gtceu.gui.charger_slot.tooltip", EFValues.VNF[getTier()],
                        EFValues.VNF[getTier()])));

        int groupWidth = Math.max(18 + templateWidth + 4 + 8, 172);
        int groupHeight = Math.max(templateHeight + 38, 87);
        UIElement group = new UIElement().layout(layout -> layout.width(groupWidth).height(groupHeight));
        group.addChild(intInput((groupWidth - 80) / 2, 5, 80, getRange(), 1, maxRange, this::setRange));
        energyGroup.layout(layout -> layout.left(3).top((groupHeight - 79) / 2).width(18).height(79));
        template.layout(layout -> layout.left((groupWidth - 18 - 4 - templateWidth) / 2 + 22)
                .top((groupHeight - templateHeight) / 2 + 15).width(templateWidth).height(templateHeight));
        group.addChild(energyGroup);
        group.addChild(template);
        return group;
    }
}
