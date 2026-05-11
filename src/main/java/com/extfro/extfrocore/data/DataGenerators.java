package com.extfro.extfrocore.data;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.registry.registrate.EFSoundEntryBuilder;
import com.extfro.extfrocore.common.registry.EFRegistration;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Set;

@EventBusSubscriber(modid = ExtForCore.MOD_ID)
public final class DataGenerators {

    static {
        EFDatagen.init();
    }

    private DataGenerators() {}

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        EFDatagen.init();

        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();

        if (event.includeClient()) {
            event.addProvider(new EFSoundEntryBuilder.SoundEntryProvider(
                    packOutput, ExtForCore.MOD_ID, EFRegistration.REGISTRATE.getSoundEntries()));
        }
        if (event.includeServer() && !EFDataPackRegistries.isEmpty()) {
            event.addProvider(new DatapackBuiltinEntriesProvider(
                    packOutput,
                    event.getLookupProvider(),
                    EFDataPackRegistries.createBuilder(),
                    Set.of(ExtForCore.MOD_ID)));
        }
    }
}
