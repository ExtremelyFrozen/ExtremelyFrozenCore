package com.extfro.extfrocore.common.machine.electric;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.EFValues;
import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.blockentity.PipeBlockEntity;
import com.extfro.extfrocore.api.capability.IControllable;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.item.tool.GTToolType;
import com.extfro.extfrocore.api.machine.MetaMachine;
import com.extfro.extfrocore.api.machine.TickableSubscription;
import com.extfro.extfrocore.api.machine.TieredEnergyMachine;
import com.extfro.extfrocore.api.machine.property.GTMachineModelProperties;
import com.extfro.extfrocore.api.machine.trait.NotifiableEnergyContainer;
import com.extfro.extfrocore.api.sync_system.annotations.RerenderOnChanged;
import com.extfro.extfrocore.api.sync_system.annotations.SaveField;
import com.extfro.extfrocore.api.sync_system.annotations.SyncToClient;
import com.extfro.extfrocore.config.ConfigHolder;
import com.extfro.extfrocore.utils.ExtendedUseOnContext;
import com.extfro.extfrocore.utils.GTUtil;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import it.unimi.dsi.fastutil.objects.Object2BooleanFunction;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WorldAcceleratorMachine extends TieredEnergyMachine implements IControllable {

    private static final Map<String, Class<?>> blacklistedClasses = new Object2ObjectOpenHashMap<>();
    private static final Object2BooleanFunction<Class<? extends BlockEntity>> blacklistCache = new Object2BooleanOpenHashMap<>();
    private static boolean gatheredClasses = false;

    // Hard-coded blacklist for blockentities
    private static final List<String> blockEntityClassNamesBlackList = new ArrayList<>();

    public static final BooleanProperty RANDOM_TICK_PROPERTY = GTMachineModelProperties.IS_RANDOM_TICK_MODE;

    private static final long blockEntityAmperage = 6;
    private static final long randomTickAmperage = 3;
    // Variables for Random Tick mode optimization
    // limit = ((tier - min) / (max - min)) * 2^tier
    private static final int[] SUCCESS_LIMITS = { 1, 8, 27, 64, 125, 216, 343, 512 };

    private final int speed;
    private final int successLimit;
    private final int randRange;
    @Getter
    @SaveField
    @SyncToClient
    private boolean isWorkingEnabled = true;
    @Getter
    @SaveField
    @SyncToClient
    private boolean isRandomTickMode = true;
    @Getter
    @SaveField
    @SyncToClient
    @RerenderOnChanged
    private boolean active = false;
    private TickableSubscription tickSubs;

    public WorldAcceleratorMachine(BlockEntityCreationInfo info, int tier) {
        super(info, tier, (TieredEnergyMachine machine) -> {
            long tierVoltage = EFValues.V[machine.getTier()];
            return new NotifiableEnergyContainer(machine, tierVoltage * 256L, tierVoltage, 8, 0L, 0L);
        });
        this.speed = (int) Math.pow(2, tier);
        this.successLimit = SUCCESS_LIMITS[tier - 1];
        this.randRange = (getTier() << 1) + 1;
    }

    public void updateSubscription() {
        if (isWorkingEnabled && drainEnergy(true)) {
            tickSubs = subscribeServerTick(tickSubs, this::update);
            setRenderState(getRenderState().setValue(GTMachineModelProperties.IS_ACTIVE, true));
            if (!active) {
                active = true;
                syncDataHolder.markClientSyncFieldDirty("active");
            }
        } else if (tickSubs != null) {
            tickSubs.unsubscribe();
            tickSubs = null;
            setRenderState(getRenderState().setValue(GTMachineModelProperties.IS_ACTIVE, false));
            if (active) {
                active = false;
                syncDataHolder.markClientSyncFieldDirty("active");
            }
        }
    }

    public void update() {
        drainEnergy(false);
        // handle random tick mode
        if (isRandomTickMode) {
            BlockPos cornerPos = new BlockPos(
                    getBlockPos().getX() - getTier(),
                    getBlockPos().getY() - getTier(),
                    getBlockPos().getZ() - getTier());
            int attempts = successLimit * 3;

            for (int i = 0, j = 0; i < successLimit && j < attempts; j++) {
                BlockPos randomPos = cornerPos.offset(
                        EFValues.RNG.nextInt(randRange),
                        EFValues.RNG.nextInt(randRange),
                        EFValues.RNG.nextInt(randRange));
                if (randomPos.getY() > getLevel().getMaxBuildHeight() ||
                        randomPos.getY() < getLevel().getMinBuildHeight() || !getLevel().isLoaded(randomPos) ||
                        randomPos.equals(getBlockPos()))
                    continue;
                if (getLevel().getBlockState(randomPos).isRandomlyTicking()) {
                    getLevel().getBlockState(randomPos).randomTick((ServerLevel) this.getLevel(), randomPos,
                            EFValues.RNG);
                }
                i++;
            }
        } else {
            // else handle block entity mode
            for (Direction dir : GTUtil.DIRECTIONS) {
                BlockEntity blockEntity = this.getLevel().getBlockEntity(this.getBlockPos().relative(dir));
                if (blockEntity != null && canAccelerate(blockEntity)) {
                    tickBlockEntity(blockEntity);
                }
            }
        }
        updateSubscription();
    }

    public boolean drainEnergy(boolean simulate) {
        long toDrain = (isRandomTickMode ? randomTickAmperage : blockEntityAmperage) * EFValues.V[tier];
        long resultEnergy = energyContainer.getEnergyStored() - toDrain;
        if (resultEnergy >= 0L && resultEnergy <= energyContainer.getEnergyCapacity()) {
            if (!simulate) {
                energyContainer.removeEnergy(toDrain);
            }
            return true;
        }
        return false;
    }

    private <T extends BlockEntity> void tickBlockEntity(@NotNull T blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        // noinspection unchecked
        BlockEntityTicker<T> blockEntityTicker = this.getLevel().getBlockState(pos).getTicker(this.getLevel(),
                (BlockEntityType<T>) blockEntity.getType());
        if (blockEntityTicker == null) return;
        for (int i = 0; i < speed - 1; i++) {
            blockEntityTicker.tick(blockEntity.getLevel(), blockEntity.getBlockPos(), blockEntity.getBlockState(),
                    blockEntity);
        }
    }

    private boolean canAccelerate(BlockEntity blockEntity) {
        if (blockEntity instanceof PipeBlockEntity || blockEntity instanceof MetaMachine) return false;

        generateWorldAcceleratorBlacklist();
        final Class<? extends BlockEntity> blockEntityClass = blockEntity.getClass();
        if (blacklistCache.containsKey(blockEntityClass)) {
            return blacklistCache.getBoolean(blockEntityClass);
        }

        for (Class<?> clazz : blacklistedClasses.values()) {
            if (clazz.isAssignableFrom(blockEntityClass)) {
                // Is a subclass, so it cannot be accelerated
                blacklistCache.put(blockEntityClass, false);
                return false;
            }
        }

        blacklistCache.put(blockEntityClass, true);
        return true;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            energyContainer.addChangedListener(this::updateSubscription);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (tickSubs != null) {
            tickSubs.unsubscribe();
            tickSubs = null;
        }
    }

    public void setWorkingEnabled(boolean workingEnabled) {
        isWorkingEnabled = workingEnabled;
        setRenderState(getRenderState().setValue(GTMachineModelProperties.IS_WORKING_ENABLED, isWorkingEnabled));
        syncDataHolder.markClientSyncFieldDirty("isWorkingEnabled");
        updateSubscription();
    }

    @Override
    public IGuiTexture sideTips(Player player, BlockPos pos, BlockState state, Set<GTToolType> toolTypes,
                                ItemStack held, Direction side) {
        if (toolTypes.contains(GTToolType.SOFT_MALLET)) {
            return isWorkingEnabled ? GuiTextures.TOOL_PAUSE : GuiTextures.TOOL_START;
        }
        return super.sideTips(player, pos, state, toolTypes, held, side);
    }

    @Override
    protected InteractionResult onScrewdriverClick(ExtendedUseOnContext context) {
        if (!isRemote()) {
            isRandomTickMode = !isRandomTickMode;
            setRenderState(getRenderState().setValue(GTMachineModelProperties.IS_RANDOM_TICK_MODE, isRandomTickMode));
            syncDataHolder.markClientSyncFieldDirty("isRandomTickMode");
            context.getPlayer().sendSystemMessage(Component.translatable(isRandomTickMode ?
                    "gtceu.machine.world_accelerator.mode_entity" : "gtceu.machine.world_accelerator.mode_tile"));
            scheduleRenderUpdate();
        }
        return InteractionResult.CONSUME;
    }

    private static void generateWorldAcceleratorBlacklist() {
        if (!gatheredClasses) {
            for (String name : ConfigHolder.INSTANCE.machines.worldAcceleratorBlacklist) {
                if (!blacklistedClasses.containsKey(name)) {
                    try {
                        blacklistedClasses.put(name, Class.forName(name));
                    } catch (ClassNotFoundException ignored) {
                        ExtForCore.LOGGER.warn("Could not find class {} for World Accelerator Blacklist!", name);
                    }
                }
            }

            for (String className : blockEntityClassNamesBlackList) {
                try {
                    blacklistedClasses.put(className, Class.forName(className));
                } catch (ClassNotFoundException ignored) {}
            }

            gatheredClasses = true;
        }
    }
}
