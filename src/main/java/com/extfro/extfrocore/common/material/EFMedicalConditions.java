package com.extfro.extfrocore.common.material;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.medical.EFMedicalCondition;

public final class EFMedicalConditions {

    public static final EFMedicalCondition NONE = new EFMedicalCondition(ExtForCore.id("none"));
    public static final EFMedicalCondition CARCINOGEN = new EFMedicalCondition(ExtForCore.id("carcinogen"));

    private EFMedicalConditions() {}
}
