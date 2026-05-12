package com.extfro.extfrocore.common.machine.multiblock.part.hpca;

import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.machine.multiblock.part.MultiblockPartMachine;
import com.extfro.extfrocore.common.data.GTBlocks;
import com.extfro.extfrocore.common.machine.trait.hpca.HPCAComponentTrait;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

import com.lowdragmc.lowdraglib2.gui.texture.ResourceTexture;
import lombok.Getter;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class HPCAComponentPartMachine extends MultiblockPartMachine {

    @Getter
    protected final HPCAComponentTrait hpcaComponentTrait;

    public HPCAComponentPartMachine(BlockEntityCreationInfo info,
                                    HPCAComponentTrait hpcaTrait) {
        super(info);
        this.hpcaComponentTrait = attachTrait(hpcaTrait);
    }

    public abstract boolean isAdvanced();

    public abstract ResourceTexture getComponentIcon();

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return false;
    }

    @Override
    public int getDefaultPaintingColor() {
        return 0xFFFFFF;
    }

    @Override
    public boolean canShared() {
        return false;
    }

    @Override
    public void modifyDrops(List<ItemStack> drops) {
        for (int i = 0; i < drops.size(); ++i) {
            ItemStack drop = drops.get(i);
            if (drop.getItem() == this.getDefinition().getItem()) {
                if (hpcaComponentTrait.isDamaged()) {
                    if (isAdvanced()) {
                        drops.set(i, GTBlocks.ADVANCED_COMPUTER_CASING.asStack());
                    } else {
                        drops.set(i, GTBlocks.COMPUTER_CASING.asStack());
                    }
                }
                break;
            }
        }
    }
}
