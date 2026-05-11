package com.extfro.extfrocore.api.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public abstract class EFFluid extends BaseFlowingFluid {

    @Getter
    private final Collection<EFFluidAttribute> attributes = new ObjectLinkedOpenHashSet<>();
    @Getter
    private final EFFluidState state;
    @Getter
    private final int burnTime;

    public EFFluid(Properties properties, EFFluidState state, int burnTime) {
        super(properties);
        this.state = state;
        this.burnTime = burnTime;
    }

    public void addAttribute(@NotNull EFFluidAttribute attribute) {
        attributes.add(attribute);
    }

    @Override
    protected boolean canBeReplacedWith(net.minecraft.world.level.material.FluidState state, BlockGetter level,
                                        BlockPos pos, Fluid fluid, Direction direction) {
        return direction == Direction.DOWN && !isSame(fluid);
    }

    @Override
    public int getTickDelay(LevelReader level) {
        return 5;
    }

    @Override
    protected float getExplosionResistance() {
        return 10;
    }

    @Override
    protected int getSlopeFindDistance(LevelReader level) {
        return 4;
    }

    @Override
    protected int getDropOff(LevelReader level) {
        return 1;
    }

    @Override
    protected boolean canConvertToSource(Level level) {
        return false;
    }

    public static class Source extends EFFluid {

        public Source(Properties properties, EFFluidState state, int burnTime) {
            super(properties, state, burnTime);
        }

        @Override
        public int getAmount(net.minecraft.world.level.material.FluidState state) {
            return 8;
        }

        @Override
        public boolean isSource(net.minecraft.world.level.material.FluidState state) {
            return true;
        }
    }

    public static class Flowing extends EFFluid {

        public Flowing(Properties properties, EFFluidState state, int burnTime) {
            super(properties, state, burnTime);
        }

        @Override
        protected void createFluidStateDefinition(StateDefinition.@NotNull Builder<Fluid, net.minecraft.world.level.material.FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(net.minecraft.world.level.material.FluidState state) {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(net.minecraft.world.level.material.FluidState state) {
            return false;
        }
    }
}
