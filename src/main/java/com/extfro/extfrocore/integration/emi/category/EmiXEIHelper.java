package com.extfro.extfrocore.integration.emi.category;

import com.extfro.extfrocore.integration.xei.entry.fluid.FluidEntryList;
import com.extfro.extfrocore.integration.xei.entry.item.ItemEntryList;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import dev.emi.emi.api.neoforge.NeoForgeEmiStack;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;

final class EmiXEIHelper {

    private EmiXEIHelper() {}

    static ResourceLocation syntheticId(String type, ResourceLocation id) {
        return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "/xei/" + type + "/" + id.getPath());
    }

    static ResourceLocation syntheticId(String type, ResourceLocation id, int index) {
        return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "/xei/" + type + "/" + id.getPath() +
                "/" + index);
    }

    static Component label(String key, Object value) {
        return Component.literal(key + ": " + value);
    }

    static EmiStack item(ItemStack stack) {
        return stack.isEmpty() ? EmiStack.EMPTY : EmiStack.of(stack.copy());
    }

    static EmiStack item(String namespace, String path) {
        var item = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(namespace, path));
        return item == net.minecraft.world.item.Items.AIR ? EmiStack.EMPTY : EmiStack.of(item);
    }

    static EmiStack fluid(Fluid fluid, long amount) {
        return fluid == net.minecraft.world.level.material.Fluids.EMPTY ? EmiStack.EMPTY : EmiStack.of(fluid, amount);
    }

    static EmiStack fluid(FluidStack stack) {
        return stack.isEmpty() ? EmiStack.EMPTY : NeoForgeEmiStack.of(stack);
    }

    static EmiIngredient itemIngredient(ItemEntryList entries) {
        return EmiIngredient.of(entries.getStacks().stream()
                .filter(stack -> !stack.isEmpty())
                .map(EmiStack::of)
                .toList());
    }

    static EmiIngredient fluidIngredient(FluidEntryList entries) {
        return EmiIngredient.of(entries.getStacks().stream()
                .filter(stack -> !stack.isEmpty())
                .map(NeoForgeEmiStack::of)
                .toList());
    }
}
