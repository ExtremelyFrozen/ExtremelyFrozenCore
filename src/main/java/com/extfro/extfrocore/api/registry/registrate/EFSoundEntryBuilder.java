package com.extfro.extfrocore.api.registry.registrate;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.sound.EFConfiguredSoundEvent;
import com.extfro.extfrocore.api.sound.EFCustomSoundEntry;
import com.extfro.extfrocore.api.sound.EFSoundEntry;
import com.extfro.extfrocore.api.sound.EFWrappedSoundEntry;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

import com.google.gson.JsonObject;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class EFSoundEntryBuilder {

    public static class SoundEntryProvider implements DataProvider {

        private final PackOutput output;
        private final String modId;
        private final Collection<EFSoundEntry> sounds;

        public SoundEntryProvider(PackOutput output, String modId, Collection<EFSoundEntry> sounds) {
            this.output = output;
            this.modId = modId;
            this.sounds = sounds;
        }

        @Override
        public CompletableFuture<?> run(CachedOutput cache) {
            return generate(output.getOutputFolder(PackOutput.Target.RESOURCE_PACK).resolve(modId), cache);
        }

        @Override
        public String getName() {
            return modId + " custom sounds";
        }

        public CompletableFuture<?> generate(Path path, CachedOutput cache) {
            JsonObject json = new JsonObject();
            for (EFSoundEntry sound : sounds) {
                if (sound.getId().getNamespace().equals(modId)) {
                    sound.write(json);
                }
            }
            return DataProvider.saveStable(cache, json, path.resolve("sounds.json"));
        }
    }

    protected final EFRegistrate owner;
    protected final ResourceLocation id;
    protected String subtitle = "unregistered";
    protected SoundSource category = SoundSource.BLOCKS;
    protected final List<EFConfiguredSoundEvent> wrappedEvents;
    protected final List<ResourceLocation> variants;
    protected int attenuationDistance;

    public EFSoundEntryBuilder(EFRegistrate owner, ResourceLocation id) {
        this.owner = owner;
        this.id = id;
        wrappedEvents = new ArrayList<>();
        variants = new ArrayList<>();
    }

    public EFSoundEntryBuilder subtitle(String subtitle) {
        this.subtitle = subtitle;
        return this;
    }

    public EFSoundEntryBuilder attenuationDistance(int distance) {
        this.attenuationDistance = distance;
        return this;
    }

    public EFSoundEntryBuilder noSubtitle() {
        this.subtitle = null;
        return this;
    }

    public EFSoundEntryBuilder category(SoundSource category) {
        this.category = category;
        return this;
    }

    public EFSoundEntryBuilder addVariant(String name) {
        return addVariant(ExtForCore.id(name));
    }

    public EFSoundEntryBuilder addVariant(ResourceLocation id) {
        variants.add(id);
        return this;
    }

    public EFSoundEntryBuilder playExisting(Supplier<SoundEvent> event, float volume, float pitch) {
        wrappedEvents.add(new EFConfiguredSoundEvent(event, volume, pitch));
        return this;
    }

    public EFSoundEntryBuilder playExisting(SoundEvent event, float volume, float pitch) {
        return playExisting(() -> event, volume, pitch);
    }

    public EFSoundEntryBuilder playExisting(SoundEvent event) {
        return playExisting(event, 1, 1);
    }

    public EFSoundEntry build() {
        EFSoundEntry entry = wrappedEvents.isEmpty() ?
                new EFCustomSoundEntry(id, variants, subtitle, category, attenuationDistance) :
                new EFWrappedSoundEntry(id, subtitle, wrappedEvents, category, attenuationDistance);
        entry.prepare();
        return owner.registerSoundEntry(entry);
    }
}
