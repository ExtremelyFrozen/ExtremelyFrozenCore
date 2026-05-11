package com.extfro.extfrocore.api.capability.recipe;

public enum IO {

    IN,
    OUT,
    BOTH,
    NONE;

    public boolean supports(IO io) {
        if (this == NONE || io == NONE) {
            return false;
        }
        return this == BOTH || io == BOTH || this == io;
    }
}
