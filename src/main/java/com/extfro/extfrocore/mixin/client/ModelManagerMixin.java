package com.extfro.extfrocore.mixin.client;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.data.pack.event.EFRegisterDynamicResourcesEvent;

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

@Mixin(ModelManager.class)
public abstract class ModelManagerMixin {

    @Inject(method = "reload", at = @At("HEAD"))
    private void extfrocore$loadDynamicModels(PreparableReloadListener.PreparationBarrier preparationBarrier,
                                              ResourceManager resourceManager,
                                              ProfilerFiller preparationsProfiler,
                                              ProfilerFiller reloadProfiler,
                                              Executor backgroundExecutor,
                                              Executor gameExecutor,
                                              CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        if (ModLoader.hasErrors()) {
            ExtForCore.LOGGER.warn("Dynamic model loading skipped because loading errors have been encountered");
            return;
        }

        long startTime = System.currentTimeMillis();
        ModLoader.postEventWrapContainerInModOrder(new EFRegisterDynamicResourcesEvent());
        ExtForCore.LOGGER.debug("Dynamic model loading took {}ms", System.currentTimeMillis() - startTime);
    }
}
