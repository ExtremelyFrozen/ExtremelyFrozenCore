package com.extfro.extfrocore.mixin;

import com.extfro.extfrocore.api.recipe.MachineRecipe;
import com.extfro.extfrocore.api.recipe.MachineRecipeType;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.stream.Stream;

@Mixin(value = RecipeManager.class, priority = 1500)
public abstract class RecipeManagerMixin {

    @Shadow
    private Multimap<RecipeType<?>, RecipeHolder<?>> byType;

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At("TAIL"))
    private void extfrocore$cacheMachineRecipes(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager,
                                                ProfilerFiller profiler, CallbackInfo ci) {
        for (RecipeType<?> recipeType : BuiltInRegistries.RECIPE_TYPE) {
            if (recipeType instanceof MachineRecipeType machineRecipeType) {
                machineRecipeType.beginStagingRecipes();
                var proxyRecipes = machineRecipeType.getProxyRecipes();
                if (this.byType.containsKey(machineRecipeType)) {
                    Stream.concat(this.byType.get(machineRecipeType).stream(),
                            proxyRecipes.entrySet().stream().flatMap(entry -> entry.getValue().stream()))
                            .filter(holder -> holder != null && holder.value() instanceof MachineRecipe)
                            .forEach(holder -> {
                                MachineRecipe recipe = (MachineRecipe) holder.value();
                                recipe.setId(holder.id());
                                machineRecipeType.addStagingRecipe(recipe);
                            });
                } else if (!proxyRecipes.isEmpty()) {
                    proxyRecipes.values().stream()
                            .flatMap(java.util.List::stream)
                            .map(RecipeHolder::value)
                            .forEach(machineRecipeType::addStagingRecipe);
                }
                machineRecipeType.completeStagingRecipes();
            }
        }
    }
}
