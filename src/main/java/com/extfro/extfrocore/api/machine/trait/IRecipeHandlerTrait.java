package com.extfro.extfrocore.api.machine.trait;

import com.extfro.extfrocore.api.capability.recipe.IO;
import com.extfro.extfrocore.api.capability.recipe.IRecipeHandler;

public interface IRecipeHandlerTrait<K> extends IRecipeHandler<K> {

    IO getHandlerIO();

    ISubscription addChangedListener(Runnable listener);
}
