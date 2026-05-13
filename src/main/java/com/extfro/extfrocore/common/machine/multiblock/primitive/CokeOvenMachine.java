package com.extfro.extfrocore.common.machine.multiblock.primitive;

import com.extfro.extfrocore.api.EFValues;
import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.gui.ModularUIBuilder;
import com.extfro.extfrocore.api.gui.UITemplate;
import com.extfro.extfrocore.api.gui.widget.SlotWidget;
import com.extfro.extfrocore.api.gui.widget.TankWidget;
import com.extfro.extfrocore.api.machine.feature.IUIMachine;
import com.extfro.extfrocore.common.machine.gui.MachineUIHelper;
import com.extfro.extfrocore.config.ConfigHolder;
import com.extfro.extfrocore.utils.ExtendedUseOnContext;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.fluids.FluidUtil;

import com.lowdragmc.lowdraglib2.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.data.FillDirection;

public class CokeOvenMachine extends PrimitiveWorkableMachine implements IUIMachine {

    public CokeOvenMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUIBuilder(176, 166, this, entityPlayer)
                .background(GuiTextures.PRIMITIVE_BACKGROUND)
                .widget(MachineUIHelper.label(5, 5, getBlockState().getBlock().getDescriptionId()))
                .widget(new SlotWidget(importItems.storage, 0, 52, 30, true, true)
                        .setBackgroundTexture(
                                new GuiTextureGroup(GuiTextures.PRIMITIVE_SLOT, GuiTextures.PRIMITIVE_FURNACE_OVERLAY)))
                .widget(MachineUIHelper.progress(76, 32, 20, 15, recipeLogic::getProgressPercent,
                        GuiTextures.PRIMITIVE_BLAST_FURNACE_PROGRESS_BAR))
                .widget(new SlotWidget(exportItems.storage, 0, 103, 30, true, false)
                        .setBackgroundTexture(
                                new GuiTextureGroup(GuiTextures.PRIMITIVE_SLOT, GuiTextures.PRIMITIVE_FURNACE_OVERLAY)))
                .widget(new TankWidget(exportFluids.getStorages()[0], 134, 13, 20, 58, true, false)
                        .setBackground(GuiTextures.PRIMITIVE_LARGE_FLUID_TANK)
                        .setFillDirection(FillDirection.DOWN_TO_UP)
                        .setShowAmountOverlay(false)
                        .setOverlay(GuiTextures.PRIMITIVE_LARGE_FLUID_TANK_OVERLAY))
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.PRIMITIVE_SLOT, 7, 84,
                        true));
    }

    @Override
    public void animateTick(RandomSource random) {
        if (this.isActive()) {
            final BlockPos pos = getBlockPos();
            float x = pos.getX() + 0.5F;
            float z = pos.getZ() + 0.5F;

            final var facing = getFrontFacing();
            final float horizontalOffset = EFValues.RNG.nextFloat() * 0.6F - 0.3F;
            final float y = pos.getY() + EFValues.RNG.nextFloat() * 0.375F + 0.3F;

            if (facing.getAxis() == Direction.Axis.X) {
                if (facing.getAxisDirection() == Direction.AxisDirection.POSITIVE) x += 0.52F;
                else x -= 0.52F;
                z += horizontalOffset;
            } else if (facing.getAxis() == Direction.Axis.Z) {
                if (facing.getAxisDirection() == Direction.AxisDirection.POSITIVE) z += 0.52F;
                else z -= 0.52F;
                x += horizontalOffset;
            }
            if (ConfigHolder.INSTANCE.machines.machineSounds && EFValues.RNG.nextDouble() < 0.1) {
                getLevel().playLocalSound(x, y, z, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F,
                        false);
            }
            getLevel().addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, 0, 0, 0);
            getLevel().addParticle(ParticleTypes.FLAME, x, y, z, 0, 0, 0);
        }
    }

    @Override
    public InteractionResult onUseWithItem(ExtendedUseOnContext context) {
        if (!isRemote()) {
            if (super.onUseWithItem(context) == InteractionResult.SUCCESS) {
                return InteractionResult.SUCCESS;
            }
            if (FluidUtil.interactWithFluidHandler(context.getPlayer(), context.getHand(), exportFluids)) {
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }
        return super.onUseWithItem(context);
    }
}
