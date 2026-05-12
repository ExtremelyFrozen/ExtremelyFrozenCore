package com.extfro.extfrocore.api.machine.trait.feature;

import com.extfro.extfrocore.api.item.tool.GTToolType;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A machine trait that overrides some of the default machine rendering behaviour.
 */
@ParametersAreNonnullByDefault
public interface IRenderingTrait extends ITraitFeature {

    /**
     * Called when a player is looking at this machine, returns whether the grid overlay should be rendered.
     */
    default boolean shouldRenderGridOverlay(Player player, BlockPos pos, BlockState state, ItemStack held,
                                            Set<GTToolType> toolTypes) {
        return false;
    }

    /**
     * Called when the machine grid overlay is being rendered to determine the icon to be rendered within the grid
     * segment on a specifc side.
     */
    default @Nullable IGuiTexture getGridOverlayIcon(Player player, BlockPos pos, BlockState state,
                                                     Set<GTToolType> toolTypes,
                                                     Direction side) {
        return null;
    }

    default void updateModelData(ModelData.Builder builder) {}
}
