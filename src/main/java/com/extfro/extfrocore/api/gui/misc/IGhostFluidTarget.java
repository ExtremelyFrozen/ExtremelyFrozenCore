package com.extfro.extfrocore.api.gui.misc;

import com.extfro.extfrocore.ExtForCore;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;

import com.google.common.collect.Lists;
import com.lowdragmc.lowdraglib2.gui.ingredient.IGhostIngredientTarget;
import com.lowdragmc.lowdraglib2.gui.ingredient.Target;
import dev.emi.emi.api.stack.EmiStack;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.neoforge.NeoForgeTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static com.extfro.extfrocore.api.gui.widget.PhantomFluidWidget.drainFrom;

public interface IGhostFluidTarget extends IGhostIngredientTarget {

    @OnlyIn(Dist.CLIENT)
    Rect2i getRectangleBox();

    @OnlyIn(Dist.CLIENT)
    void acceptFluid(FluidStack fluidStack);

    @OnlyIn(Dist.CLIENT)
    @Override
    default List<Target> getPhantomTargets(Object ingredient) {
        ingredient = convertIngredient(ingredient);
        if (!(ingredient instanceof FluidStack) && drainFrom(ingredient) == null) {
            return Collections.emptyList();
        } else {
            final Rect2i rectangle = getRectangleBox();
            return Lists.newArrayList(new Target[] { new Target() {

                @NotNull
                public Rect2i getArea() {
                    return rectangle;
                }

                public void accept(@NotNull Object ingredient) {
                    ingredient = convertIngredient(ingredient);

                    FluidStack ingredientStack;
                    if (ingredient instanceof FluidStack fluidStack) {
                        ingredientStack = fluidStack;
                    } else {
                        ingredientStack = drainFrom(ingredient);
                    }

                    if (ingredientStack != null) {
                        acceptFluid(ingredientStack);
                    }
                }
            } });
        }
    }

    default Object convertIngredient(Object ingredient) {
        if (ExtForCore.Mods.isEMILoaded() && ingredient instanceof EmiStack fluidEmiStack) {
            Fluid fluid = fluidEmiStack.getKeyOfType(Fluid.class);
            ingredient = fluid == null ? FluidStack.EMPTY :
                    new FluidStack(fluid.builtInRegistryHolder(),
                            (int) fluidEmiStack.getAmount(), fluidEmiStack.getComponentChanges());
        } else if (ExtForCore.Mods.isJEILoaded() && ingredient instanceof ITypedIngredient<?> fluidJeiStack) {
            return fluidJeiStack.getIngredient(NeoForgeTypes.FLUID_STACK).orElse(FluidStack.EMPTY);
        }
        return ingredient;
    }
}
