package com.extfro.extfrocore.api.capability.recipe;

public enum IO {

    IN,
    OUT,
    BOTH;

    public boolean supports(IO io) {
        return this == BOTH || io == BOTH || this == io;
    }
}
