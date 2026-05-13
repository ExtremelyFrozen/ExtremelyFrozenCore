package com.extfro.extfrocore.common.data;

import com.extfro.extfrocore.ExtForCore;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.JukeboxSong;

public class GTJukeboxSongs {

    public static final ResourceKey<JukeboxSong> SUS = ResourceKey.create(Registries.JUKEBOX_SONG, ExtForCore.id("sus"));

    public static void bootstrap(BootstrapContext<JukeboxSong> ctx) {
        ctx.register(SUS, new JukeboxSong(
                BuiltInRegistries.SOUND_EVENT.wrapAsHolder(GTSoundEntries.SUS_RECORD.getMainEvent()),
                Component.translatable("item.gtceu.sus_record.desc"),
                820, 1));
    }
}
