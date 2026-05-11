package com.extfro.extfrocore.data.pack;

import com.extfro.extfrocore.ExtForCore;

import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.models.blockstates.BlockStateGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.IoSupplier;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;

import com.google.gson.JsonElement;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.function.Supplier;

public class EFDynamicResourcePack implements PackResources {

    protected static final EFDynamicPackContents CONTENTS = new EFDynamicPackContents();

    public static final FileToIdConverter BLOCKSTATE_ID_CONVERTER = FileToIdConverter.json("blockstates");
    public static final FileToIdConverter MODEL_ID_CONVERTER = FileToIdConverter.json("models");

    private final PackLocationInfo info;

    public EFDynamicResourcePack(PackLocationInfo info) {
        this.info = info;
    }

    public static void clearClient() {
        CONTENTS.clearData();
    }

    public static void addResource(ResourceLocation location, JsonElement object) {
        addResource(location, object.toString().getBytes(StandardCharsets.UTF_8));
    }

    public static void addResource(ResourceLocation location, byte[] data) {
        CONTENTS.addToData(location, data);
    }

    public static void addBlockModel(ResourceLocation location, JsonElement object) {
        if (!location.getPath().startsWith("block/")) {
            location = location.withPrefix("block/");
        }
        addModel(location, object);
    }

    public static void addBlockModel(ResourceLocation location, Supplier<JsonElement> object) {
        addBlockModel(location, object.get());
    }

    public static void addBlockModel(BlockModelBuilder builder) {
        addBlockModel(builder.getLocation(), builder.toJson());
    }

    public static void addItemModel(ResourceLocation location, JsonElement object) {
        if (!location.getPath().startsWith("item/")) {
            location = location.withPrefix("item/");
        }
        addModel(location, object);
    }

    public static void addItemModel(ResourceLocation location, Supplier<JsonElement> object) {
        addItemModel(location, object.get());
    }

    public static void addItemModel(ItemModelBuilder builder) {
        addItemModel(builder.getLocation(), builder.toJson());
    }

    public static <T extends ModelBuilder<T>> void addModel(T builder) {
        addModel(builder.getLocation(), builder.toJson());
    }

    public static void addModel(ResourceLocation location, JsonElement object) {
        addResource(MODEL_ID_CONVERTER.idToFile(location), object);
    }

    public static void addModel(ResourceLocation location, Supplier<JsonElement> object) {
        addModel(location, object.get());
    }

    public static void addBlockState(ResourceLocation location, JsonElement stateJson) {
        addResource(BLOCKSTATE_ID_CONVERTER.idToFile(location), stateJson);
    }

    public static void addBlockState(ResourceLocation location, Supplier<JsonElement> stateJson) {
        addBlockState(location, stateJson.get());
    }

    public static void addBlockState(BlockStateGenerator generator) {
        addBlockState(BuiltInRegistries.BLOCK.getKey(generator.getBlock()), generator.get());
    }

    @Override
    @Nullable
    public IoSupplier<InputStream> getRootResource(String... elements) {
        return null;
    }

    @Override
    @Nullable
    public IoSupplier<InputStream> getResource(PackType type, ResourceLocation location) {
        return type == PackType.CLIENT_RESOURCES ? CONTENTS.getResource(location) : null;
    }

    @Override
    public void listResources(PackType packType, String namespace, String path, ResourceOutput resourceOutput) {
        if (packType == PackType.CLIENT_RESOURCES) {
            CONTENTS.listResources(namespace, path, resourceOutput);
        }
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        return type == PackType.CLIENT_RESOURCES ? Set.of(ExtForCore.MOD_ID, "minecraft", "neoforge") : Set.of();
    }

    @Override
    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T getMetadataSection(MetadataSectionSerializer<T> deserializer) {
        if (deserializer == PackMetadataSection.TYPE) {
            return (T) new PackMetadataSection(Component.literal(ExtForCore.MOD_NAME + " dynamic assets"),
                    SharedConstants.getCurrentVersion().getPackVersion(PackType.CLIENT_RESOURCES));
        }
        return null;
    }

    @Override
    public PackLocationInfo location() {
        return info;
    }

    @Override
    public void close() {}
}
