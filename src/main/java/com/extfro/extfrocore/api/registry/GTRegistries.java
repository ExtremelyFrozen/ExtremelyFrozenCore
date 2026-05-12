package com.extfro.extfrocore.api.registry;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.capability.recipe.RecipeCapability;
import com.extfro.extfrocore.api.cover.CoverDefinition;
import com.extfro.extfrocore.api.data.DimensionMarker;
import com.extfro.extfrocore.api.data.chemical.Element;
import com.extfro.extfrocore.api.data.chemical.material.Material;
import com.extfro.extfrocore.api.data.chemical.material.registry.MaterialRegistry;
import com.extfro.extfrocore.api.data.tag.TagPrefix;
import com.extfro.extfrocore.api.data.worldgen.GTOreDefinition;
import com.extfro.extfrocore.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.extfro.extfrocore.api.data.worldgen.bedrockore.BedrockOreDefinition;
import com.extfro.extfrocore.api.item.tool.behavior.ToolBehaviorType;
import com.extfro.extfrocore.api.machine.MachineDefinition;
import com.extfro.extfrocore.api.recipe.GTRecipeType;
import com.extfro.extfrocore.api.recipe.category.GTRecipeCategory;
import com.extfro.extfrocore.api.recipe.chance.logic.ChanceLogic;
import com.extfro.extfrocore.api.recipe.condition.RecipeConditionType;
import com.extfro.extfrocore.api.sound.SoundEntry;

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
import net.neoforged.neoforge.registries.IdMappingEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

public final class GTRegistries {

    // spotless:off
    private static final LinkedHashMap<ResourceLocation, Registry<?>> LOAD_ORDER = new LinkedHashMap<>();

    // server (datapack) registries' keys
    public static final ResourceKey<Registry<GTOreDefinition>> ORE_VEIN_REGISTRY = makeRegistryKey(ExtForCore.id("ore_vein"));
    public static final ResourceKey<Registry<BedrockFluidDefinition>> BEDROCK_FLUID_REGISTRY = makeRegistryKey(ExtForCore.id("bedrock_fluid"));
    public static final ResourceKey<Registry<BedrockOreDefinition>> BEDROCK_ORE_REGISTRY = makeRegistryKey(ExtForCore.id("bedrock_ore"));

    // static registries' keys
    public static final ResourceKey<Registry<Element>> ELEMENT_REGISTRY = makeRegistryKey(ExtForCore.id("element"));
    public static final ResourceKey<Registry<TagPrefix>> TAG_PREFIX_REGISTRY = makeRegistryKey(ExtForCore.id("tag_prefix"));
    public static final ResourceKey<Registry<Material>> MATERIAL_REGISTRY = makeRegistryKey(ExtForCore.id("material"));
    public static final ResourceKey<Registry<MachineDefinition>> MACHINE_REGISTRY = makeRegistryKey(ExtForCore.id("machine"));
    public static final ResourceKey<Registry<CoverDefinition>> COVER_REGISTRY = makeRegistryKey(ExtForCore.id("cover"));

    public static final ResourceKey<Registry<GTRecipeType>> RECIPE_TYPE_REGISTRY = makeRegistryKey(ExtForCore.id("recipe_type"));
    public static final ResourceKey<Registry<GTRecipeCategory>> RECIPE_CATEGORY_REGISTRY = makeRegistryKey(ExtForCore.id("recipe_category"));
    public static final ResourceKey<Registry<RecipeCapability<?>>> RECIPE_CAPABILITY_REGISTRY = makeRegistryKey(ExtForCore.id("recipe_capability"));
    public static final ResourceKey<Registry<RecipeConditionType<?>>> RECIPE_CONDITION_REGISTRY = makeRegistryKey(ExtForCore.id("recipe_condition"));
    public static final ResourceKey<Registry<ChanceLogic>> CHANCE_LOGIC_REGISTRY = makeRegistryKey(ExtForCore.id("chance_logic"));

    public static final ResourceKey<Registry<ToolBehaviorType<?>>> TOOL_BEHAVIOR_REGISTRY = makeRegistryKey(ExtForCore.id("tool_behavior"));
    public static final ResourceKey<Registry<SoundEntry>> SOUND_REGISTRY = makeRegistryKey(ExtForCore.id("sound"));
    public static final ResourceKey<Registry<DimensionMarker>> DIMENSION_MARKER_REGISTRY = makeRegistryKey(ExtForCore.id("dimension_marker"));

    // GT Registries
    public static final Registry<Element> ELEMENTS = makeRegistry(ELEMENT_REGISTRY);
    public static final Registry<Material> MATERIALS = makeMaterialRegistry();
    public static final Registry<TagPrefix> TAG_PREFIXES = makeRegistry(TAG_PREFIX_REGISTRY);

    public static final Registry<SoundEntry> SOUNDS = makeRegistry(SOUND_REGISTRY, false);
    public static final Registry<ChanceLogic> CHANCE_LOGICS = makeRegistry(CHANCE_LOGIC_REGISTRY);
    public static final Registry<RecipeCapability<?>> RECIPE_CAPABILITIES = makeRegistry(RECIPE_CAPABILITY_REGISTRY);
    public static final Registry<RecipeConditionType<?>> RECIPE_CONDITIONS = makeRegistry(RECIPE_CONDITION_REGISTRY);
    public static final Registry<GTRecipeCategory> RECIPE_CATEGORIES = makeRegistry(RECIPE_CATEGORY_REGISTRY);

    public static final Registry<MachineDefinition> MACHINES = makeRegistry(MACHINE_REGISTRY);
    public static final Registry<CoverDefinition> COVERS = makeRegistry(COVER_REGISTRY);

    public static final Registry<ToolBehaviorType<?>> TOOL_BEHAVIORS = makeRegistry(TOOL_BEHAVIOR_REGISTRY);
    public static final Registry<DimensionMarker> DIMENSION_MARKERS = makeRegistry(DIMENSION_MARKER_REGISTRY, false);
    // spotless:on

    public static <T> ResourceKey<Registry<T>> makeRegistryKey(ResourceLocation registryId) {
        return ResourceKey.createRegistryKey(registryId);
    }

    public static <T> MappedRegistry<T> makeRegistry(ResourceKey<Registry<T>> key) {
        return makeRegistry(key, true);
    }

    public static <T> MappedRegistry<T> makeRegistry(ResourceKey<Registry<T>> key, boolean sync) {
        MappedRegistry<T> registry = (MappedRegistry<T>) new RegistryBuilder<>(key)
                .sync(sync)
                .create();
        LOAD_ORDER.put(key.location(), registry);
        return registry;
    }

    private static MaterialRegistry makeMaterialRegistry() {
        MaterialRegistry registry = new MaterialRegistry(MATERIAL_REGISTRY);
        LOAD_ORDER.put(MATERIAL_REGISTRY.location(), registry);
        return registry;
    }

    private static final Table<Registry<?>, ResourceLocation, Object> TO_REGISTER = HashBasedTable.create();
    private static boolean isFrozen = true;

    public static <V, T extends V> T register(Registry<V> registry, ResourceLocation name, T value) {
        if (!isFrozen) {
            Registry.register(registry, name, value);
        } else {
            TO_REGISTER.put(registry, name, value);
        }
        return value;
    }

    // ignore the generics and hope the registered objects are still correctly typed :3
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void actuallyRegister(RegisterEvent event) {
        for (Registry reg : TO_REGISTER.rowKeySet()) {
            event.register(reg.key(), helper -> {
                TO_REGISTER.row(reg).forEach(helper::register);
            });
        }
        TO_REGISTER.clear();
    }

    private static void onUnfreeze(RegisterEvent event) {
        isFrozen = false;
    }

    private static void onFreeze(IdMappingEvent event) {
        isFrozen = event.isFrozen();
    }

    public static void init(IEventBus eventBus) {
        eventBus.addListener(EventPriority.HIGHEST, GTRegistries::onUnfreeze);
        eventBus.addListener(EventPriority.LOW, GTRegistries::actuallyRegister);
        NeoForge.EVENT_BUS.addListener(GTRegistries::onFreeze);
    }

    @UnmodifiableView
    public static List<ResourceLocation> getRegistrationOrder() {
        return List.copyOf(LOAD_ORDER.keySet());
    }

    @UnmodifiableView
    public static Collection<Registry<?>> getRegistries() {
        return LOAD_ORDER.values();
    }

    private static final RegistryAccess BLANK = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    private static RegistryAccess FROZEN = BLANK;

    /**
     * You shouldn't call it, you should probably not even look at it just to be extra safe
     *
     * @param registryAccess the new value to set to the frozen registry access
     */
    @ApiStatus.Internal
    public static void updateFrozenRegistry(RegistryAccess registryAccess) {
        FROZEN = registryAccess;
    }

    public static RegistryAccess builtinRegistry() {
        if (ExtForCore.isClientThread()) {
            return ClientHelpers.getClientRegistries();
        }
        return FROZEN;
    }

    private static class ClientHelpers {

        private static RegistryAccess getClientRegistries() {
            if (Minecraft.getInstance().getConnection() != null) {
                return Minecraft.getInstance().getConnection().registryAccess();
            } else {
                return FROZEN;
            }
        }
    }
}
