package com.extfro.extfrocore.core.mixins;

import com.extfro.extfrocore.core.MixinHelpers;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.lowdragmc.lowdraglib2.syncdata.holder.IPersistManagedHolder;
import com.lowdragmc.lowdraglib2.syncdata.holder.blockentity.ISyncBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BlockEntity.class, priority = 1500)
public class BlockEntityMixin {

    @Inject(method = "saveAdditional", at = @At(value = "HEAD"))
    private void gtceu$captureRegistriesSave(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        if (this instanceof IPersistManagedHolder) {
            MixinHelpers.CURRENT_BE_SAVE_LOAD_REGISTRIES.set(registries);
        }
    }

    @Inject(method = "saveAdditional", at = @At(value = "RETURN"))
    private void gtceu$clearRegistriesSave(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        if (this instanceof IPersistManagedHolder) {
            MixinHelpers.CURRENT_BE_SAVE_LOAD_REGISTRIES.remove();
        }
    }

    @Inject(method = "loadAdditional", at = @At(value = "HEAD"))
    private void gtceu$captureRegistriesLoad(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        if (this instanceof ISyncBlockEntity autoSyncBlockEntity &&
                tag.get(autoSyncBlockEntity.getSyncTag()) instanceof CompoundTag) {
            MixinHelpers.CURRENT_BE_SAVE_LOAD_REGISTRIES.set(registries);
        } else if (this instanceof IPersistManagedHolder) {
            MixinHelpers.CURRENT_BE_SAVE_LOAD_REGISTRIES.set(registries);
        }
    }

    @Inject(method = "loadAdditional", at = @At(value = "HEAD"))
    private void gtceu$clearRegistriesLoad(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        if (this instanceof ISyncBlockEntity autoSyncBlockEntity &&
                tag.get(autoSyncBlockEntity.getSyncTag()) instanceof CompoundTag) {
            MixinHelpers.CURRENT_BE_SAVE_LOAD_REGISTRIES.remove();
        } else if (this instanceof IPersistManagedHolder) {
            MixinHelpers.CURRENT_BE_SAVE_LOAD_REGISTRIES.remove();
        }
    }
}
