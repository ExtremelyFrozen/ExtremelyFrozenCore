package com.extfro.extfrocore.api.data.chemical.material.properties;

@FunctionalInterface
public interface IMaterialProperty {

    void verifyProperty(MaterialProperties properties);
}
