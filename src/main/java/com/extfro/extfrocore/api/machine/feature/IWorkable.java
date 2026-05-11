package com.extfro.extfrocore.api.machine.feature;

public interface IWorkable {

    boolean isWorkingEnabled();

    void setWorkingEnabled(boolean isWorkingAllowed);

    void setSuspendAfterFinish(boolean suspendAfterFinish);

    boolean isSuspendAfterFinish();

    int getProgress();

    int getMaxProgress();

    boolean isActive();
}
