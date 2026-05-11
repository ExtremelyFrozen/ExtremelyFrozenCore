package com.extfro.extfrocore.api.registry;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.capability.recipe.RecipeCapability;
import com.extfro.extfrocore.api.cover.CoverDefinition;
import com.extfro.extfrocore.api.data.worldgen.OreDefinition;
import com.extfro.extfrocore.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.extfro.extfrocore.api.data.worldgen.bedrockore.BedrockOreDefinition;
import com.extfro.extfrocore.api.data.worldgen.generator.IndicatorGenerators;
import com.extfro.extfrocore.api.data.worldgen.generator.VeinGenerators;
import com.extfro.extfrocore.api.machine.MachineDefinition;
import com.extfro.extfrocore.api.material.tag.EFTagPrefix;
import com.extfro.extfrocore.api.recipe.MachineRecipeType;
import com.extfro.extfrocore.api.recipe.category.RecipeCategory;
import com.extfro.extfrocore.api.recipe.chance.logic.ChanceLogic;
import com.extfro.extfrocore.api.recipe.condition.RecipeConditionType;

import net.minecraft.client.Minecraft;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.IdMappingEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.mojang.serialization.Codec;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

public final class EFRegistries {

    public static final ResourceKey<Registry<OreDefinition>> ORE_VEIN_REGISTRY = makeRegistryKey("ore_vein");
    public static final ResourceKey<Registry<BedrockFluidDefinition>> BEDROCK_FLUID_REGISTRY = makeRegistryKey("bedrock_fluid");
    public static final ResourceKey<Registry<BedrockOreDefinition>> BEDROCK_ORE_REGISTRY = makeRegistryKey("bedrock_ore");
    public static final ResourceKey<Registry<RecipeCapability<?>>> RECIPE_CAPABILITY_REGISTRY = makeRegistryKey("recipe_capability");
    public static final ResourceKey<Registry<MachineRecipeType>> RECIPE_TYPE_REGISTRY = makeRegistryKey("recipe_type");
    public static final ResourceKey<Registry<RecipeCategory>> RECIPE_CATEGORY_REGISTRY = makeRegistryKey("recipe_category");
    public static final ResourceKey<Registry<RecipeConditionType<?>>> RECIPE_CONDITION_REGISTRY = makeRegistryKey("recipe_condition");
    public static final ResourceKey<Registry<ChanceLogic>> CHANCE_LOGIC_REGISTRY = makeRegistryKey("chance_logic");
    public static final ResourceKey<Registry<EFTagPrefix>> TAG_PREFIX_REGISTRY = makeRegistryKey("tag_prefix");
    public static final ResourceKey<Registry<MachineDefinition>> MACHINE_REGISTRY = makeRegistryKey("machine");
    public static final ResourceKey<Registry<CoverDefinition>> COVER_REGISTRY = makeRegistryKey("cover");

    private static final LinkedHashMap<ResourceLocation, Registry<?>> LOAD_ORDER = new LinkedHashMap<>();
    private static final LinkedHashMap<ResourceKey<? extends Registry<?>>, DataPackRegistryEntry<?>> DATA_PACK_REGISTRIES = new LinkedHashMap<>();
    private static final Table<Registry<?>, ResourceLocation, Object> PENDING_REGISTRATIONS = HashBasedTable.create();

    private static boolean frozen = true;
    private static final RegistryAccess BLANK = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    private static RegistryAccess frozenRegistryAccess = BLANK;

    public static final Registry<EFTagPrefix> TAG_PREFIXES = makeRegistry(TAG_PREFIX_REGISTRY);
    public static final Registry<ChanceLogic> CHANCE_LOGICS = makeRegistry(CHANCE_LOGIC_REGISTRY);
    public static final Registry<RecipeCapability<?>> RECIPE_CAPABILITIES = makeRegistry(RECIPE_CAPABILITY_REGISTRY);
    public static final Registry<RecipeConditionType<?>> RECIPE_CONDITIONS = makeRegistry(RECIPE_CONDITION_REGISTRY);
    public static final Registry<RecipeCategory> RECIPE_CATEGORIES = makeRegistry(RECIPE_CATEGORY_REGISTRY);
    public static final Registry<MachineRecipeType> RECIPE_TYPES = makeRegistry(RECIPE_TYPE_REGISTRY);
    public static final Registry<MachineDefinition> MACHINES = makeRegistry(MACHINE_REGISTRY);
    public static final Registry<CoverDefinition> COVERS = makeRegistry(COVER_REGISTRY);

    static {
        VeinGenerators.init();
        IndicatorGenerators.init();
        makeDataPackRegistry(ORE_VEIN_REGISTRY, OreDefinition.DIRECT_CODEC, OreDefinition.DIRECT_CODEC);
        makeDataPackRegistry(BEDROCK_FLUID_REGISTRY, BedrockFluidDefinition.DIRECT_CODEC);
        makeDataPackRegistry(BEDROCK_ORE_REGISTRY, BedrockOreDefinition.DIRECT_CODEC);
    }

    private EFRegistries() {}

    public static <T> ResourceKey<Registry<T>> makeRegistryKey(ResourceLocation registryId) {
        return ResourceKey.createRegistryKey(registryId);
    }

    public static <T> ResourceKey<Registry<T>> makeRegistryKey(String path) {
        return makeRegistryKey(ExtForCore.id(path));
    }

    public static <T> MappedRegistry<T> makeRegistry(ResourceKey<Registry<T>> key) {
        return makeRegistry(key, true);
    }

    @SuppressWarnings("unchecked")
    public static <T> MappedRegistry<T> makeRegistry(ResourceKey<Registry<T>> key, boolean sync) {
        MappedRegistry<T> registry = (MappedRegistry<T>) new RegistryBuilder<>(key).sync(sync).create();
        LOAD_ORDER.put(key.location(), registry);
        return registry;
    }

    public static <T> void makeDataPackRegistry(ResourceKey<Registry<T>> key, Codec<T> codec,
                                                @Nullable Codec<T> networkCodec) {
        DATA_PACK_REGISTRIES.put(key, new DataPackRegistryEntry<>(key, codec, networkCodec));
    }

    public static <T> void makeDataPackRegistry(ResourceKey<Registry<T>> key, Codec<T> codec) {
        makeDataPackRegistry(key, codec, null);
    }

    public static <V, T extends V> T register(Registry<V> registry, ResourceLocation name, T value) {
        if (!frozen) {
            Registry.register(registry, name, value);
        } else {
            PENDING_REGISTRATIONS.put(registry, name, value);
        }
        return value;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void registerPending(RegisterEvent event) {
        for (Registry registry : PENDING_REGISTRATIONS.rowKeySet()) {
            event.register(registry.key(), helper -> PENDING_REGISTRATIONS.row(registry).forEach(helper::register));
        }
        PENDING_REGISTRATIONS.clear();
    }

    private static void unfreeze(RegisterEvent event) {
        frozen = false;
    }

    private static void freeze(IdMappingEvent event) {
        frozen = event.isFrozen();
    }

    public static void init(IEventBus eventBus) {
        eventBus.addListener(EventPriority.HIGHEST, EFRegistries::unfreeze);
        eventBus.addListener(EventPriority.LOW, EFRegistries::registerPending);
        NeoForge.EVENT_BUS.addListener(EFRegistries::freeze);
        NeoForge.EVENT_BUS.addListener(EFRegistries::onAddReloadListener);
    }

    public static void registerRegistries(NewRegistryEvent event) {
        getRegistries().forEach(event::register);
    }

    public static void registerDataPackRegistries(DataPackRegistryEvent.NewRegistry event) {
        DATA_PACK_REGISTRIES.values().forEach(entry -> entry.register(event));
    }

    @UnmodifiableView
    public static List<ResourceLocation> getRegistrationOrder() {
        return List.copyOf(LOAD_ORDER.keySet());
    }

    @UnmodifiableView
    public static Collection<Registry<?>> getRegistries() {
        return LOAD_ORDER.values();
    }

    @UnmodifiableView
    public static Collection<ResourceKey<? extends Registry<?>>> getDataPackRegistryKeys() {
        return DATA_PACK_REGISTRIES.keySet();
    }

    @ApiStatus.Internal
    public static void onAddReloadListener(net.neoforged.neoforge.event.AddReloadListenerEvent event) {
        updateFrozenRegistry(event.getRegistryAccess());
    }

    @ApiStatus.Internal
    public static void updateFrozenRegistry(RegistryAccess registryAccess) {
        frozenRegistryAccess = registryAccess;
    }

    public static RegistryAccess builtinRegistry() {
        if (ExtForCore.isClientThread()) {
            return ClientHelpers.getClientRegistries();
        }
        return frozenRegistryAccess;
    }

    private record DataPackRegistryEntry<T>(ResourceKey<Registry<T>> key, Codec<T> codec,
                                            @Nullable Codec<T> networkCodec) {

        private void register(DataPackRegistryEvent.NewRegistry event) {
            event.dataPackRegistry(key, codec, networkCodec);
        }
    }

    private static final class ClientHelpers {

        private static RegistryAccess getClientRegistries() {
            if (Minecraft.getInstance().getConnection() != null) {
                return Minecraft.getInstance().getConnection().registryAccess();
            }
            return frozenRegistryAccess;
        }
    }
}
