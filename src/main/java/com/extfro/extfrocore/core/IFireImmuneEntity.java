package com.extfro.extfrocore.core;

public interface IFireImmuneEntity {

    default void gtceu$setFireImmune(boolean isImmune) {
        throw new AssertionError("Mixin didn't apply");
    }
}
