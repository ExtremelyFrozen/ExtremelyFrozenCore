package com.extfro.extfrocore.core.mixins.client;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.data.pack.event.RegisterDynamicResourcesEvent;
import com.extfro.extfrocore.integration.modernfix.GTModernFixIntegration;

import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.fml.ModLoader;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(value = ModelManager.class)
public abstract class ModelManagerMixin {

    @Inject(method = "reload", at = @At(value = "HEAD"))
    private void gtceu$loadDynamicModels(PreparableReloadListener.PreparationBarrier preparationBarrier,
                                         ResourceManager resourceManager, ProfilerFiller preparationsProfiler,
                                         ProfilerFiller reloadProfiler, Executor backgroundExecutor,
                                         Executor gameExecutor, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        if (ModLoader.hasErrors()) {
            ExtForCore.LOGGER.warn("GregTech Model loading CANCELLED because loading errors have been encountered");
            return;
        }

        long startTime = System.currentTimeMillis();
        // turns out these do have to be init in here after all, as they check for asset existence. whoops.
        ModLoader.postEventWrapContainerInModOrder(new RegisterDynamicResourcesEvent());

        if (ExtForCore.Mods.isModernFixLoaded()) {
            GTModernFixIntegration.setAsLast();
        }
        ExtForCore.LOGGER.info("GregTech Model loading took {}ms", System.currentTimeMillis() - startTime);
    }
}
