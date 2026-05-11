package com.extfro.extfrocore.utils;

import com.extfro.extfrocore.api.capability.ICoverable;
import com.extfro.extfrocore.api.tool.EFToolType;
import com.extfro.extfrocore.api.tool.ToolHelper;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;

import lombok.Getter;
import org.jetbrains.annotations.UnknownNullability;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Set;

public class ExtendedUseOnContext extends UseOnContext {

    @Getter
    private final Direction gridSide;
    @Getter
    @Unmodifiable
    private final Set<EFToolType> toolType;

    public ExtendedUseOnContext(Player player, InteractionHand hand, BlockHitResult hitResult) {
        super(player, hand, hitResult);
        gridSide = ICoverable.traceCoverSide(hitResult);
        toolType = ToolHelper.getToolTypes(getItemInHand());
    }

    @Override
    public @UnknownNullability Player getPlayer() {
        return super.getPlayer();
    }
}
