package com.extfro.extfrocore.api.machine;

import com.extfro.extfrocore.api.capability.ICoverable;
import com.extfro.extfrocore.api.cover.CoverBehavior;
import com.extfro.extfrocore.api.cover.CoverDefinition;
import com.extfro.extfrocore.api.registry.EFRegistries;
import com.extfro.extfrocore.api.sync_system.ISyncManaged;
import com.extfro.extfrocore.api.sync_system.SyncTagMap;
import com.extfro.extfrocore.api.sync_system.annotations.ClientFieldChangeListener;
import com.extfro.extfrocore.api.sync_system.annotations.RerenderOnChanged;
import com.extfro.extfrocore.api.sync_system.annotations.SaveField;
import com.extfro.extfrocore.api.sync_system.annotations.SyncToClient;
import com.extfro.extfrocore.api.sync_system.holder.SyncDataHolder;
import com.extfro.extfrocore.api.transfer.fluid.IFluidHandlerModifiable;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class MachineCoverContainer implements ICoverable, ISyncManaged {

    private static final Codec<CoverSnapshot> COVER_SNAPSHOT_CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    ResourceLocation.CODEC.fieldOf("id").forGetter(CoverSnapshot::id),
                    ItemStack.CODEC.optionalFieldOf("item", ItemStack.EMPTY).forGetter(CoverSnapshot::item),
                    SyncTagMap.CODEC.optionalFieldOf("data", SyncTagMap.empty()).forGetter(CoverSnapshot::data))
            .apply(instance, CoverSnapshot::new));

    @Getter
    private final SyncDataHolder syncDataHolder = new SyncDataHolder(this);
    @Getter
    private final MetaMachine machine;
    @SaveField
    @SyncToClient
    @RerenderOnChanged
    private SyncTagMap covers = SyncTagMap.empty();
    private @Nullable CoverBehavior up, down, north, south, west, east;

    public MachineCoverContainer(MetaMachine machine) {
        this.machine = machine;
    }

    @ClientFieldChangeListener(fieldName = "covers")
    private void onCoversChanged() {
        loadCoversFromStorage(registries(), true);
    }

    public void loadCoversFromStorage(HolderLookup.Provider registries, boolean clientSide) {
        up = down = north = south = west = east = null;
        SyncTagMap stored = covers;
        for (Direction direction : ICoverable.DIRECTIONS) {
            SyncTagMap sideTag = stored.get(direction.getName()) == null ? null :
                    SyncTagMap.tryRead(stored.get(direction.getName()));
            if (sideTag == null || sideTag.isEmpty()) {
                continue;
            }
            CoverSnapshot snapshot = COVER_SNAPSHOT_CODEC.parse(new Dynamic<>(NbtOps.INSTANCE, sideTag.toVanillaTag()))
                    .result()
                    .orElse(null);
            if (snapshot == null) {
                continue;
            }
            CoverDefinition definition = EFRegistries.COVERS.get(snapshot.id());
            if (definition == null) {
                continue;
            }
            CoverBehavior cover = definition.createCoverBehavior(this, direction);
            cover.setAttachItem(snapshot.item());
            cover.getSyncDataHolder().deserializeData(registries, snapshot.data(), clientSide);
            setCoverAtSideRaw(cover, direction);
        }
        covers = createStoredCovers(registries);
    }

    private void refreshStoredCovers() {
        covers = createStoredCovers(registries());
    }

    private SyncTagMap createStoredCovers(HolderLookup.Provider registries) {
        SyncTagMap tag = SyncTagMap.empty();
        for (Direction direction : ICoverable.DIRECTIONS) {
            CoverBehavior cover = getCoverAtSide(direction);
            if (cover == null) {
                tag.put(direction.getName(), SyncTagMap.empty().toVanillaTag());
                continue;
            }
            ResourceLocation id = EFRegistries.COVERS.getKey(cover.coverDefinition);
            if (id == null) {
                tag.put(direction.getName(), SyncTagMap.empty().toVanillaTag());
                continue;
            }
            SyncTagMap sideTag = SyncTagMap.empty();
            sideTag.put("id", StringTag.valueOf(id.toString()));
            sideTag.put("item", ItemStack.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE),
                    cover.getAttachItem()).getOrThrow());
            SyncTagMap coverData = cover.getSyncDataHolder().serializeToSaveData(registries);
            coverData.merge(cover.getSyncDataHolder().serializeFullClientSyncData(registries));
            sideTag.put("data", coverData.toVanillaTag());
            tag.put(direction.getName(), sideTag.toVanillaTag());
        }
        return tag;
    }

    private HolderLookup.Provider registries() {
        return machine.getLevel() == null ? EFRegistries.builtinRegistry() : machine.getLevel().registryAccess();
    }

    @Override
    public MetaMachine getHolder() {
        return machine;
    }

    @Override
    public boolean canPlaceCoverOnSide(CoverDefinition definition, Direction side) {
        ArrayList<VoxelShape> collisionList = new ArrayList<>();
        machine.addCollisionBoundingBox(collisionList);
        return !ICoverable.doesCoverCollide(side, collisionList, getCoverPlateThickness());
    }

    @Override
    public double getCoverPlateThickness() {
        return 0;
    }

    @Override
    public Direction getFrontFacing() {
        return machine.getFrontFacing();
    }

    @Override
    public boolean shouldRenderBackSide() {
        return !machine.getBlockState().canOcclude();
    }

    @Override
    public @Nullable CoverBehavior getCoverAtSide(Direction side) {
        return switch (side) {
            case UP -> up;
            case DOWN -> down;
            case NORTH -> north;
            case SOUTH -> south;
            case WEST -> west;
            case EAST -> east;
        };
    }

    @Override
    public void setCoverAtSide(@Nullable CoverBehavior coverBehavior, Direction side) {
        setCoverAtSideRaw(coverBehavior, side);
        refreshStoredCovers();
    }

    private void setCoverAtSideRaw(@Nullable CoverBehavior coverBehavior, Direction side) {
        switch (side) {
            case UP -> up = coverBehavior;
            case DOWN -> down = coverBehavior;
            case NORTH -> north = coverBehavior;
            case SOUTH -> south = coverBehavior;
            case WEST -> west = coverBehavior;
            case EAST -> east = coverBehavior;
        }
    }

    @Override
    public @Nullable IItemHandlerModifiable getItemHandlerCap(@Nullable Direction side, boolean useCoverCapability) {
        return machine.getItemHandlerCap(side, useCoverCapability);
    }

    @Override
    public @Nullable IFluidHandlerModifiable getFluidHandlerCap(@Nullable Direction side, boolean useCoverCapability) {
        return machine.getFluidHandlerCap(side, useCoverCapability);
    }

    @Override
    public void scheduleRenderUpdate() {
        machine.scheduleRenderUpdate();
    }

    @Override
    public void markAsChanged() {
        refreshStoredCovers();
        machine.markAsChanged();
    }

    private record CoverSnapshot(ResourceLocation id, ItemStack item, SyncTagMap data) {}
}
