package com.extfro.extfrocore.core.mixins.ldlib;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.core.MixinHelpers;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelDataManager;
import net.neoforged.neoforge.common.extensions.IBlockGetterExtension;
import net.neoforged.neoforge.common.extensions.ILevelExtension;

import com.lowdragmc.lowdraglib2.utils.virtuallevel.DummyWorld;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.lang.ref.WeakReference;

@Mixin(value = DummyWorld.class, remap = false)
public abstract class DummyWorldMixin implements ILevelExtension, IBlockGetterExtension {

    @Shadow
    public WeakReference<Level> level;

    @Unique
    private final ModelDataManager gtceu$modelDataManager = new ModelDataManager((Level) (Object) this);

    /**
     * @author screret
     * @reason ensure client-server separation
     */
    @Overwrite
    public @NotNull Level getLevel() {
        Level level = this.level.get();
        if (level == null && ExtForCore.isClientThread()) {
            level = MixinHelpers.ClientCallWrapper.getClientLevel();
            this.level = new WeakReference<>(level);
        }
        assert level != null;
        return level;
    }

    @Override
    public @Nullable ModelDataManager getModelDataManager() {
        return gtceu$modelDataManager;
    }

    @Override
    public @NotNull ModelData getModelData(@NotNull BlockPos pos) {
        return gtceu$modelDataManager.getAt(pos);
    }
}
