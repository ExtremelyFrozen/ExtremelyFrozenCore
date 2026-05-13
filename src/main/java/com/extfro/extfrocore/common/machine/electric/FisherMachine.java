package com.extfro.extfrocore.common.machine.electric;

import com.extfro.extfrocore.api.EFValues;
import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.capability.GTCapabilityHelper;
import com.extfro.extfrocore.api.capability.IWorkable;
import com.extfro.extfrocore.api.capability.recipe.IO;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.machine.TickableSubscription;
import com.extfro.extfrocore.api.machine.TieredEnergyMachine;
import com.extfro.extfrocore.api.machine.feature.IFancyUIMachine;
import com.extfro.extfrocore.api.machine.trait.AutoOutputTrait;
import com.extfro.extfrocore.api.machine.trait.NotifiableItemStackHandler;
import com.extfro.extfrocore.api.sync_system.annotations.SaveField;
import com.extfro.extfrocore.api.sync_system.annotations.SyncToClient;
import com.extfro.extfrocore.api.transfer.item.CustomItemStackHandler;
import com.extfro.extfrocore.config.ConfigHolder;
import com.extfro.extfrocore.utils.ISubscription;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import com.lowdragmc.lowdraglib2.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

/**
 * @author h3tr
 * @date 2023/7/13
 * @implNote FisherMachine
 */
public class FisherMachine extends TieredEnergyMachine
                           implements IFancyUIMachine, IWorkable {

    @SaveField
    protected final NotifiableItemStackHandler cache;
    @Getter
    @Setter
    @SaveField
    protected boolean allowInputFromOutputSideItems;
    @SaveField
    protected final NotifiableItemStackHandler baitHandler;

    @Getter
    @SaveField
    protected final CustomItemStackHandler chargerInventory;
    @Nullable
    protected TickableSubscription batterySubs, fishingSubs;
    @Nullable
    protected ISubscription energySubs, baitSubs;
    private final long energyPerTick;

    private final int inventorySize;

    @Getter
    public final int maxProgress;

    @Getter
    @SaveField
    private int progress = 0;

    @Getter
    @SaveField
    @SyncToClient
    private boolean isWorkingEnabled = true;

    @Getter
    @SaveField
    private boolean active = false;
    public static final int WATER_CHECK_SIZE = 5;
    private static final ItemStack fishingRod = new ItemStack(Items.FISHING_ROD);
    private boolean hasWater = false;

    @Getter
    @SaveField
    @SyncToClient
    protected boolean junkEnabled = true;
    @SaveField
    @SyncToClient
    public final AutoOutputTrait autoOutput;

    public FisherMachine(BlockEntityCreationInfo info, int tier) {
        super(info, tier);
        this.inventorySize = (tier + 1) * (tier + 1);
        this.maxProgress = calcMaxProgress(tier);
        this.energyPerTick = EFValues.V[tier - 1];
        this.cache = attachTrait(new NotifiableItemStackHandler(inventorySize, IO.BOTH, IO.OUT));

        this.baitHandler = attachTrait(new NotifiableItemStackHandler(1, IO.BOTH, IO.IN));
        baitHandler.setFilter(item -> item.is(Items.STRING));

        this.chargerInventory = new CustomItemStackHandler();
        chargerInventory.setFilter(item -> GTCapabilityHelper.getElectricItem(item) != null ||
                (ConfigHolder.INSTANCE.compat.energy.nativeEUToFE &&
                        GTCapabilityHelper.getForgeEnergyItem(item) != null));

        autoOutput = attachTrait(AutoOutputTrait.ofItems(cache));
        environmentalExplosionTrait.setEnableEnvironmentalExplosions(false);
    }

    public void setWorkingEnabled(boolean enabled) {
        isWorkingEnabled = enabled;
        syncDataHolder.markClientSyncFieldDirty("isWorkingEnabled");
    }

    public void setJunkEnabled(boolean enabled) {
        junkEnabled = enabled;
        syncDataHolder.markClientSyncFieldDirty("junkEnabled");
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (isRemote()) return;
        energySubs = energyContainer.addChangedListener(() -> {
            this.updateBatterySubscription();
            this.updateFishingUpdateSubscription();
        });
        baitSubs = baitHandler.addChangedListener(this::updateFishingUpdateSubscription);
        chargerInventory.setOnContentsChanged(this::updateBatterySubscription);
        this.updateFishingUpdateSubscription();
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (energySubs != null) {
            energySubs.unsubscribe();
            energySubs = null;
        }
        if (baitSubs != null) {
            baitSubs.unsubscribe();
            baitSubs = null;
        }
    }

    @Override
    public void onMachineDestroyed() {
        super.onMachineDestroyed();
        chargerInventory.dropInventoryInWorld(getLevel(), getBlockPos());
    }

    public static int calcMaxProgress(int tier) {
        return (int) (800.0 - 170 * ((double) tier - 1.0) + (((double) Math.max(0, tier - 4) / 0.012)));
    }

    //////////////////////////////////////
    // ********* Logic **********//
    //////////////////////////////////////

    public void updateFishingUpdateSubscription() {
        if (drainEnergy(true) && this.baitHandler.getStackInSlot(0).is(Items.STRING) && isWorkingEnabled) {
            fishingSubs = subscribeServerTick(fishingSubs, this::fishingUpdate);
            active = true;
            return;
        } else if (fishingSubs != null) {
            fishingSubs.unsubscribe();
            fishingSubs = null;
            active = false;
        }
        progress = 0;
    }

    private void updateHasWater() {
        for (int x = 0; x < WATER_CHECK_SIZE; x++)
            for (int z = 0; z < WATER_CHECK_SIZE; z++) {
                BlockPos waterCheckPos = getBlockPos().below().offset(x - WATER_CHECK_SIZE / 2, 0,
                        z - WATER_CHECK_SIZE / 2);
                if (!getLevel().getBlockState(waterCheckPos).getFluidState().is(Fluids.WATER)) {
                    hasWater = false;
                    return;
                }
            }
        hasWater = true;
    }

    public void fishingUpdate() {
        if (this.getOffsetTimer() % maxProgress == 0L)
            updateHasWater();

        if (!hasWater) return;

        drainEnergy(false);
        if (progress >= maxProgress) {
            var lootTableRegistry = getLevel().registryAccess().registryOrThrow(Registries.LOOT_TABLE);
            LootTable lootTable = lootTableRegistry.get(BuiltInLootTables.FISHING);
            if (!this.junkEnabled) {
                lootTable = lootTableRegistry.get(BuiltInLootTables.FISHING_FISH);
            }

            FishingHook simulatedHook = new FishingHook(EntityType.FISHING_BOBBER, getLevel()) {

                public boolean isOpenWaterFishing() {
                    return true;
                }
            };

            LootParams lootContext = new LootParams.Builder((ServerLevel) getLevel())
                    .withOptionalParameter(LootContextParams.THIS_ENTITY, simulatedHook)
                    .withParameter(LootContextParams.TOOL, fishingRod)
                    .withParameter(LootContextParams.ORIGIN,
                            new Vec3(getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ()))
                    .create(LootContextParamSets.FISHING);

            NonNullList<ItemStack> generatedLoot = NonNullList.create();
            generatedLoot.addAll(lootTable.getRandomItems(lootContext));

            boolean useBait = false;
            for (ItemStack itemStack : generatedLoot)
                useBait |= tryFillCache(itemStack);

            if (useBait && junkEnabled)
                this.baitHandler.storage.extractItem(0, 1, false);
            else if (useBait)
                this.baitHandler.storage.extractItem(0, 2, false);
            updateFishingUpdateSubscription();
            progress = -1;
        }
        progress++;
    }

    private boolean tryFillCache(ItemStack stack) {
        for (int i = 0; i < cache.getSlots(); i++) {
            if (cache.insertItemInternal(i, stack, false).getCount() < stack.getCount()) {
                return true;
            }
        }
        return false;
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
        if (!energyContainer.dischargeOrRechargeEnergyContainers(chargerInventory, 0, false))
            updateBatterySubscription();
    }

    //////////////////////////////////////
    // ********** GUI ***********//
    //////////////////////////////////////

    @Override
    public UIElement createUIWidget() {
        int rowSize = (int) Math.sqrt(inventorySize);
        int templateWidth = rowSize * 18 + 8 + 20;
        int templateHeight = rowSize * 18 + 8;
        UIElement template = panel(templateWidth, templateHeight, GuiTextures.BACKGROUND_INVERSE);
        for (int y = 0; y < rowSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                int index = y * rowSize + x;
                template.addChild(itemSlot(cache, index, 24 + x * 18, 4 + y * 18, GuiTextures.SLOT, true, false));
            }
        }
        template.addChild(itemSlot(baitHandler.storage, 0, 4, (templateHeight - 18) / 2,
                new GuiTextureGroup(GuiTextures.SLOT, GuiTextures.STRING_SLOT_OVERLAY), true, true));

        Button junkButton = new Button().noText();
        junkButton.layout(layout -> layout.left(4).top(templateHeight - 22).width(18).height(18));
        junkButton.buttonStyle(style -> style
                .baseTexture(new GuiTextureGroup(GuiTextures.TOGGLE_BUTTON_BACK,
                        new ItemStackTexture(Items.NAME_TAG).scale(0.9F)))
                .hoverTexture(new GuiTextureGroup(GuiTextures.TOGGLE_BUTTON_BACK,
                        new ItemStackTexture(Items.NAME_TAG).scale(0.9F)))
                .pressedTexture(new GuiTextureGroup(GuiTextures.TOGGLE_BUTTON_BACK,
                        new ItemStackTexture(Items.NAME_TAG).scale(0.9F))));
        junkButton.setOnServerClick(event -> setJunkEnabled(!isJunkEnabled()));
        junkButton.style(style -> style.tooltips(Component.translatable("gtceu.gui.fisher_mode.tooltip",
                EFValues.VNF[getTier()], EFValues.VNF[getTier()])));
        template.addChild(junkButton);

        UIElement energyGroup = new UIElement().layout(layout -> layout.width(18).height(79));
        energyGroup.addChild(createEnergyBar(this));
        energyGroup.addChild(itemSlot(chargerInventory, 0, 0, 61,
                new GuiTextureGroup(GuiTextures.SLOT, GuiTextures.CHARGER_OVERLAY), true, true,
                Component.translatable("gtceu.gui.charger_slot.tooltip", EFValues.VNF[getTier()],
                        EFValues.VNF[getTier()])));

        int groupWidth = Math.max(18 + templateWidth + 4 + 8, 172);
        int groupHeight = Math.max(templateHeight + 8, 87);
        UIElement group = new UIElement().layout(layout -> layout.width(groupWidth).height(groupHeight));
        energyGroup.layout(layout -> layout.left(3).top((groupHeight - 79) / 2).width(18).height(79));
        template.layout(layout -> layout.left((groupWidth - 18 - 4 - templateWidth) / 2 + 2 + 18 + 2)
                .top((groupHeight - templateHeight) / 2).width(templateWidth).height(templateHeight));
        group.addChild(energyGroup);
        group.addChild(template);
        return group;
    }
}
