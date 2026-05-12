package com.extfro.extfrocore.api.cover;

import com.extfro.extfrocore.api.capability.recipe.IO;
import com.extfro.extfrocore.common.cover.data.ManualIOMode;

public interface IIOCover {

    int getTransferRate();

    IO getIo();

    ManualIOMode getManualIOMode();
}
