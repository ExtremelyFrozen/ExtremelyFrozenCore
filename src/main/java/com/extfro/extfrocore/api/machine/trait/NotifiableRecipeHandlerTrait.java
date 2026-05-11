package com.extfro.extfrocore.api.machine.trait;

import com.extfro.extfrocore.api.sync_system.annotations.SaveField;
import com.extfro.extfrocore.api.sync_system.annotations.SyncToClient;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public abstract class NotifiableRecipeHandlerTrait<T> implements IRecipeHandlerTrait<T> {

    protected final List<Runnable> listeners = new ArrayList<>();

    @Getter
    @SaveField
    @SyncToClient
    protected boolean isDistinct;

    public void setDistinct(boolean distinct) {
        isDistinct = distinct;
    }

    @Override
    public ISubscription addChangedListener(Runnable listener) {
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }

    public void notifyListeners() {
        listeners.forEach(Runnable::run);
    }
}
