package com.extfro.extfrocore.api.capability;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.blockentity.ICopyable;
import com.extfro.extfrocore.api.blockentity.ITickSubscription;
import com.extfro.extfrocore.api.cover.CoverBehavior;
import com.extfro.extfrocore.api.cover.CoverDefinition;
import com.extfro.extfrocore.api.machine.MetaMachine;
import com.extfro.extfrocore.api.machine.TickableSubscription;
import com.extfro.extfrocore.api.registry.EFRegistries;
import com.extfro.extfrocore.api.sync_system.ISyncManaged;
import com.extfro.extfrocore.api.sync_system.SyncTagMap;
import com.extfro.extfrocore.api.transfer.fluid.IFluidHandlerModifiable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public interface ICoverable extends ITickSubscription, ISyncManaged, ICopyable {

    Direction[] DIRECTIONS = Direction.values();

    MetaMachine getHolder();

    default Level getLevel() {
        return getHolder().getLevel();
    }

    default BlockPos getBlockPos() {
        return getHolder().getBlockPos();
    }

    default BlockState getBlockState() {
        return getHolder().getBlockState();
    }

    default long getOffsetTimer() {
        return getHolder().getOffsetTimer();
    }

    default boolean isRemoved() {
        return getHolder().isRemoved();
    }

    default void notifyBlockUpdate() {
        getHolder().notifyBlockUpdate();
    }

    default void scheduleRenderUpdate() {
        getHolder().scheduleRenderUpdate();
    }

    default void scheduleNeighborShapeUpdate() {
        getHolder().scheduleNeighborShapeUpdate();
    }

    @Override
    default void markAsChanged() {
        getHolder().markAsChanged();
    }

    @Nullable
    @Override
    default TickableSubscription subscribeServerTick(Runnable runnable) {
        return getHolder().subscribeServerTick(runnable);
    }

    @Override
    default void unsubscribe(@Nullable TickableSubscription current) {
        getHolder().unsubscribe(current);
    }

    boolean canPlaceCoverOnSide(CoverDefinition definition, Direction side);

    double getCoverPlateThickness();

    Direction getFrontFacing();

    boolean shouldRenderBackSide();

    @Nullable
    IItemHandlerModifiable getItemHandlerCap(@Nullable Direction side, boolean useCoverCapability);

    @Nullable
    IFluidHandlerModifiable getFluidHandlerCap(@Nullable Direction side, boolean useCoverCapability);

    @ApiStatus.Internal
    void setCoverAtSide(@Nullable CoverBehavior coverBehavior, Direction side);

    @Nullable
    CoverBehavior getCoverAtSide(Direction side);

    default boolean placeCoverOnSide(Direction side, ItemStack itemStack, CoverDefinition coverDefinition,
                                     @Nullable ServerPlayer player) {
        CoverBehavior coverBehavior = coverDefinition.createCoverBehavior(this, side);
        if (!canPlaceCoverOnSide(coverDefinition, side) || !coverBehavior.canAttach()) {
            return false;
        }
        if (getCoverAtSide(side) != null) {
            removeCover(side, player);
        }
        coverBehavior.onAttached(itemStack, player);
        coverBehavior.onLoad();
        setCoverAtSide(coverBehavior, side);
        notifyBlockUpdate();
        scheduleNeighborShapeUpdate();
        return true;
    }

    default boolean removeCover(boolean dropItself, Direction side, @Nullable Player player) {
        CoverBehavior coverBehavior = getCoverAtSide(side);
        if (coverBehavior == null) {
            return false;
        }
        List<ItemStack> drops = coverBehavior.getAdditionalDrops();
        if (dropItself) {
            drops.add(coverBehavior.getPickItem());
        }
        coverBehavior.onRemoved();
        setCoverAtSide(null, side);
        for (ItemStack dropStack : drops) {
            if (player != null && player.getInventory().add(dropStack)) {
                continue;
            }
            Block.popResource(getLevel(), getBlockPos(), dropStack);
        }
        notifyBlockUpdate();
        scheduleNeighborShapeUpdate();
        return true;
    }

    default void dropAllCovers() {
        for (Direction side : DIRECTIONS) {
            removeCover(side, null);
        }
    }

    default boolean removeCover(Direction side, @Nullable Player player) {
        return removeCover(true, side, player);
    }

    default List<CoverBehavior> getCovers() {
        return Arrays.stream(DIRECTIONS).map(this::getCoverAtSide).filter(Objects::nonNull).toList();
    }

    default void onLoad() {
        for (CoverBehavior cover : getCovers()) {
            cover.onLoad();
        }
    }

    default void onUnload() {
        for (CoverBehavior cover : getCovers()) {
            cover.onUnload();
        }
    }

    default void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        for (CoverBehavior cover : getCovers()) {
            cover.onNeighborChanged(block, fromPos, isMoving);
        }
    }

    default boolean hasAnyCover() {
        for (Direction facing : DIRECTIONS) {
            if (getCoverAtSide(facing) != null) {
                return true;
            }
        }
        return false;
    }

    default boolean hasCover(Direction facing) {
        return getCoverAtSide(facing) != null;
    }

    default boolean isRemote() {
        return getLevel() == null ? ExtForCore.isClientThread() : getLevel().isClientSide;
    }

    default VoxelShape[] addCoverCollisionBoundingBox() {
        double plateThickness = getCoverPlateThickness();
        List<VoxelShape> shapes = new ArrayList<>();
        if (plateThickness > 0.0) {
            for (Direction side : DIRECTIONS) {
                if (getCoverAtSide(side) != null) {
                    shapes.add(getCoverPlateBox(side, plateThickness));
                }
            }
        }
        return shapes.toArray(VoxelShape[]::new);
    }

    static boolean doesCoverCollide(Direction side, List<VoxelShape> collisionBox, double plateThickness) {
        if (side == null) {
            return false;
        }
        if (plateThickness > 0.0) {
            VoxelShape coverPlateBox = getCoverPlateBox(side, plateThickness);
            for (AABB aabb : coverPlateBox.toAabbs()) {
                if (Shapes.collide(side.getAxis(), aabb, collisionBox, plateThickness) < plateThickness) {
                    return true;
                }
            }
        }
        return false;
    }

    @Nullable
    static Direction rayTraceCoverableSide(ICoverable coverable, Player player) {
        HitResult rayTrace = player.pick(player.blockInteractionRange(), 0, false);
        if (rayTrace.getType() != HitResult.Type.BLOCK) {
            return null;
        }
        return traceCoverSide((BlockHitResult) rayTrace);
    }

    default boolean hasDynamicCovers() {
        for (Direction face : DIRECTIONS) {
            CoverBehavior cover = getCoverAtSide(face);
            if (cover != null && cover.getDynamicRenderer().get() != null) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    static Direction traceCoverSide(@Nullable BlockHitResult result) {
        return result == null ? null : result.getDirection();
    }

    static VoxelShape getCoverPlateBox(Direction side, double plateThickness) {
        return switch (side) {
            case UP -> Shapes.box(0.0, 1.0 - plateThickness, 0.0, 1.0, 1.0, 1.0);
            case DOWN -> Shapes.box(0.0, 0.0, 0.0, 1.0, plateThickness, 1.0);
            case NORTH -> Shapes.box(0.0, 0.0, 0.0, 1.0, 1.0, plateThickness);
            case SOUTH -> Shapes.box(0.0, 0.0, 1.0 - plateThickness, 1.0, 1.0, 1.0);
            case WEST -> Shapes.box(0.0, 0.0, 0.0, plateThickness, 1.0, 1.0);
            case EAST -> Shapes.box(1.0 - plateThickness, 0.0, 0.0, 1.0, 1.0, 1.0);
        };
    }

    static boolean canPlaceCover(CoverDefinition coverDef, ICoverable coverable) {
        for (Direction facing : DIRECTIONS) {
            if (coverable.canPlaceCoverOnSide(coverDef, facing)) {
                CoverBehavior cover = coverDef.createCoverBehavior(coverable, facing);
                if (cover.canAttach()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Nullable
    default BlockState getBlockAppearance(BlockState state, BlockAndTintGetter level, BlockPos pos, Direction side,
                                          BlockState sourceState, BlockPos sourcePos) {
        CoverBehavior cover = getCoverAtSide(side);
        return cover == null ? null : cover.getAppearance(sourceState, sourcePos);
    }

    private SyncTagMap createCoverConfigTag(@Nullable CoverBehavior cover, HolderLookup.Provider registries) {
        SyncTagMap tag = SyncTagMap.empty();
        if (cover == null) {
            return tag;
        }
        ResourceLocation id = EFRegistries.COVERS.getKey(cover.coverDefinition);
        if (id == null) {
            return tag;
        }
        tag.put("id", StringTag.valueOf(id.toString()));
        ItemStack.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), cover.getAttachItem())
                .result()
                .ifPresent(item -> tag.put("item", item));
        SyncTagMap data = cover.getSyncDataHolder().serializeToSaveData(registries);
        data.merge(cover.copyConfig(SyncTagMap.empty()));
        tag.put("data", data.toTag());
        return tag;
    }

    private void applyCoverConfigTag(ServerPlayer player, Direction dir, SyncTagMap tag,
                                     HolderLookup.Provider registries) {
        if (tag.isEmpty()) {
            return;
        }
        StringTag idTag = tag.get("id") instanceof StringTag stringTag ? stringTag : null;
        if (idTag == null) {
            return;
        }
        ResourceLocation id = ResourceLocation.tryParse(idTag.getAsString());
        if (id == null) {
            return;
        }
        CoverDefinition def = EFRegistries.COVERS.get(id);
        if (def == null) {
            return;
        }
        ItemStack stack = ItemStack.EMPTY;
        if (tag.get("item") != null) {
            stack = ItemStack.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag.get("item"))
                    .result()
                    .orElse(ItemStack.EMPTY);
        }
        placeCoverOnSide(dir, stack, def, player);

        CoverBehavior placedCover = getCoverAtSide(dir);
        SyncTagMap data = tag.get("data") == null ? null : SyncTagMap.tryRead(tag.get("data"));
        if (placedCover != null && data != null && !data.isEmpty()) {
            placedCover.getSyncDataHolder().deserializeData(registries, data, false);
            placedCover.pasteConfig(player, data);
        }
    }

    @Override
    default SyncTagMap copyConfig(SyncTagMap tag) {
        HolderLookup.Provider registries = getLevel().registryAccess();
        for (Direction dir : DIRECTIONS) {
            tag.put(dir.getName(), createCoverConfigTag(getCoverAtSide(dir), registries).toTag());
        }
        return tag;
    }

    @Override
    default void pasteConfig(ServerPlayer player, SyncTagMap tag) {
        for (Direction side : DIRECTIONS) {
            removeCover(side, player);
        }

        HolderLookup.Provider registries = getLevel().registryAccess();
        for (Direction dir : DIRECTIONS) {
            SyncTagMap sideTag = tag.get(dir.getName()) == null ? null : SyncTagMap.tryRead(tag.get(dir.getName()));
            if (sideTag != null) {
                applyCoverConfigTag(player, dir, sideTag, registries);
            }
        }
    }

    @Override
    default List<ItemStack> getItemsRequiredToPaste() {
        Map<Item, Integer> allDrops = new HashMap<>();
        List<ItemStack> rawDrops = new ArrayList<>();

        for (Direction side : DIRECTIONS) {
            CoverBehavior cover = getCoverAtSide(side);
            if (cover != null) {
                rawDrops.add(cover.getAttachItem());
                rawDrops.addAll(cover.getAdditionalDrops());
            }
        }

        for (ItemStack drop : rawDrops) {
            allDrops.merge(drop.getItem(), drop.getCount(), Integer::sum);
        }

        List<ItemStack> mergedStacks = new ArrayList<>();
        allDrops.forEach((item, count) -> mergedStacks.add(new ItemStack(item, count)));
        return mergedStacks;
    }
}
