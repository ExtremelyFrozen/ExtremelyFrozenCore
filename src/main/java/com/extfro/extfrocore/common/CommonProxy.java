package com.extfro.extfrocore.common;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.EFAPI;
import com.extfro.extfrocore.api.EFValues;
import com.extfro.extfrocore.api.addon.AddonFinder;
import com.extfro.extfrocore.api.addon.IGTAddon;
import com.extfro.extfrocore.api.block.MetaMachineBlock;
import com.extfro.extfrocore.api.capability.GTCapability;
import com.extfro.extfrocore.api.capability.compat.EUToFEProvider;
import com.extfro.extfrocore.api.capability.recipe.FluidRecipeCapability;
import com.extfro.extfrocore.api.capability.recipe.ItemRecipeCapability;
import com.extfro.extfrocore.api.data.chemical.material.Material;
import com.extfro.extfrocore.api.data.chemical.material.event.PostMaterialEvent;
import com.extfro.extfrocore.api.data.chemical.material.info.MaterialIconSet;
import com.extfro.extfrocore.api.data.chemical.material.info.MaterialIconType;
import com.extfro.extfrocore.api.data.chemical.material.registry.MaterialRegistry;
import com.extfro.extfrocore.api.data.tag.TagPrefix;
import com.extfro.extfrocore.api.data.worldgen.GTOreDefinition;
import com.extfro.extfrocore.api.data.worldgen.WorldGenLayers;
import com.extfro.extfrocore.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.extfro.extfrocore.api.data.worldgen.bedrockore.BedrockOreDefinition;
import com.extfro.extfrocore.api.data.worldgen.generator.IndicatorGenerators;
import com.extfro.extfrocore.api.data.worldgen.generator.VeinGenerators;
import com.extfro.extfrocore.api.item.IComponentItem;
import com.extfro.extfrocore.api.item.IGTTool;
import com.extfro.extfrocore.api.item.MetaMachineItem;
import com.extfro.extfrocore.api.machine.MachineDefinition;
import com.extfro.extfrocore.api.misc.forge.QuantumFluidHandlerItemStack;
import com.extfro.extfrocore.api.recipe.chance.logic.ChanceLogic;
import com.extfro.extfrocore.api.recipe.ingredient.IntCircuitIngredient;
import com.extfro.extfrocore.api.recipe.ingredient.IntProviderFluidIngredient;
import com.extfro.extfrocore.api.recipe.ingredient.IntProviderIngredient;
import com.extfro.extfrocore.api.recipe.lookup.ingredient.AbstractMapIngredient;
import com.extfro.extfrocore.api.recipe.lookup.ingredient.IntersectionMapIngredient;
import com.extfro.extfrocore.api.recipe.lookup.ingredient.MapIngredientTypeManager;
import com.extfro.extfrocore.api.recipe.lookup.ingredient.fluid.CustomFluidMapIngredient;
import com.extfro.extfrocore.api.recipe.lookup.ingredient.fluid.FluidDataComponentMapIngredient;
import com.extfro.extfrocore.api.recipe.lookup.ingredient.fluid.FluidStackMapIngredient;
import com.extfro.extfrocore.api.recipe.lookup.ingredient.fluid.FluidTagMapIngredient;
import com.extfro.extfrocore.api.recipe.lookup.ingredient.item.*;
import com.extfro.extfrocore.api.registry.GTRegistries;
import com.extfro.extfrocore.api.registry.registrate.GTRegistrate;
import com.extfro.extfrocore.common.block.*;
import com.extfro.extfrocore.common.data.*;
import com.extfro.extfrocore.common.data.GTBlocks;
import com.extfro.extfrocore.common.data.item.*;
import com.extfro.extfrocore.common.data.machines.GTMachineUtils;
import com.extfro.extfrocore.common.data.materials.GTFoods;
import com.extfro.extfrocore.common.fluid.potion.BottleItemFluidHandler;
import com.extfro.extfrocore.common.fluid.potion.PotionItemFluidHandler;
import com.extfro.extfrocore.common.item.DrumMachineItem;
import com.extfro.extfrocore.common.item.GTBucketItem;
import com.extfro.extfrocore.common.item.armor.GTArmorMaterials;
import com.extfro.extfrocore.common.item.tool.rotation.CustomBlockRotations;
import com.extfro.extfrocore.common.machine.multiblock.electric.FusionReactorMachine;
import com.extfro.extfrocore.common.machine.owner.MachineOwner;
import com.extfro.extfrocore.common.machine.storage.QuantumTankMachine;
import com.extfro.extfrocore.config.ConfigHolder;
import com.extfro.extfrocore.core.mixins.registrate.AbstractRegistrateAccessor;
import com.extfro.extfrocore.data.GregTechDatagen;
import com.extfro.extfrocore.data.lang.MaterialLangGenerator;
import com.extfro.extfrocore.data.loot.ChestGenHooks;
import com.extfro.extfrocore.data.pack.GTDynamicDataPack;
import com.extfro.extfrocore.data.pack.GTDynamicResourcePack;
import com.extfro.extfrocore.data.pack.GTPackSource;
import com.extfro.extfrocore.data.recipe.*;
import com.extfro.extfrocore.integration.cctweaked.CCTweakedPlugin;
import com.extfro.extfrocore.integration.kjs.GTCEuStartupEvents;
import com.extfro.extfrocore.integration.kjs.GregTechKubeJSPlugin;
import com.extfro.extfrocore.integration.kjs.events.MaterialModificationEventJS;
import com.extfro.extfrocore.integration.map.WaypointManager;
import com.extfro.extfrocore.utils.input.SyncedKeyMappings;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.Capabilities.FluidHandler;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.common.crafting.IntersectionIngredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.wrappers.FluidBucketWrapper;
import net.neoforged.neoforge.fluids.crafting.*;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.ModifyRegistriesEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.callback.BakeCallback;

import com.google.common.collect.Multimaps;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateLangProvider;
import com.tterrag.registrate.providers.RegistrateProvider;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.ApiStatus;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static com.extfro.extfrocore.common.registry.GTRegistration.REGISTRATE;

public class CommonProxy {

    private static IEventBus modBus;

    public static void init(final IEventBus modBus) {
        CommonProxy.modBus = modBus;
        if (ExtForCore.Mods.isKubeJSLoaded()) {
            // initialize this before the class's static listeners
            // so KubeJS materials are registered before the material registry is closed.
            modBus.addListener(EventPriority.LOW, GregTechKubeJSPlugin::registerWrappers);
        }
        modBus.register(CommonProxy.class);

        // Initialize the model generator before any content is loaded so machine models can use the generated data
        GregTechDatagen.initPre();

        GTRegistries.init(modBus);
        REGISTRATE.registerEventListeners(modBus);
        GTCreativeModeTabs.init();
        GTAttachmentTypes.ATTACHMENT_TYPES.register(modBus);
        GTMenuTypes.init(modBus);

        FusionReactorMachine.registerFusionTier(EFValues.LuV, "MKI");
        FusionReactorMachine.registerFusionTier(EFValues.ZPM, "MKII");
        FusionReactorMachine.registerFusionTier(EFValues.UV, "MKIII");

        AddonFinder.getAddonList().forEach(IGTAddon::gtInitComplete);
    }

    // Only register everything once.
    private static boolean didRunRegistration = false;

    @SubscribeEvent
    public static void onRegister(RegisterEvent event) {
        if (didRunRegistration) {
            return;
        }
        didRunRegistration = true;

        GTElements.init();
        MaterialIconSet.init();
        MaterialIconType.init();
        initMaterials();
        GTMedicalConditions.init();
        TagPrefix.init();

        GTSoundEntries.init();
        GTDamageTypes.init();
        GTPlaceholders.initPlaceholders();

        GTBlocks.init();
        GTFluids.init();

        GTDimensionMarkers.init();
        GTRecipeCapabilities.init();
        GTRecipeConditions.init();
        ChanceLogic.init();
        GTRecipeTypes.init();
        GTRecipeCategories.init();

        GTFoods.init();
        GTToolTiers.init();
        GTToolBehaviors.init();
        GTDataComponents.DATA_COMPONENTS.register(modBus);
        GTArmorMaterials.ARMOR_MATERIALS.register(modBus);
        GTItems.init();

        GTMachineUtils.init();
        GTCovers.init();
        GTMachines.init();

        GTEntityTypes.init();
        GTIngredientTypes.ITEM_INGREDIENT_TYPES.register(modBus);
        GTIngredientTypes.FLUID_INGREDIENT_TYPES.register(modBus);
        GTRecipeSerializers.RECIPE_SERIALIZERS.register(modBus);

        GTCommandArguments.COMMAND_ARGUMENT_TYPES.register(modBus);
        GTMobEffects.MOB_EFFECTS.register(modBus);
        GTParticleTypes.PARTICLE_TYPES.register(modBus);

        GregTechDatagen.initPost();
        GTValueProviderTypes.init(modBus);
        GTFeatures.register(modBus);
        WorldGenLayers.registerAll();
        VeinGenerators.registerAddonGenerators();
        IndicatorGenerators.registerAddonGenerators();
        WaypointManager.init();

        CustomBlockRotations.init();
        SyncedKeyMappings.init();
        MachineOwner.init();
        ChestGenHooks.init();
    }

    @ApiStatus.Internal
    public static void initMaterials() {
        ExtForCore.LOGGER.info("Registering ExtForCore Materials");
        GTMaterials.init();
        EFAPI.materialManager.setFallbackMaterial(ExtForCore.MOD_ID, GTMaterials.Aluminium);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onRegisterLate(RegisterEvent event) {
        // Material event *should* happen before any of the others here
        if (event.getRegistryKey() == GTRegistries.MATERIAL_REGISTRY) {
            // Fire Post-Material event, intended for when Materials need to be iterated over in-full before freezing
            // Block entirely new Materials from being added in the Post event
            ((MaterialRegistry) GTRegistries.MATERIALS).close();
            ModLoader.postEventWrapContainerInModOrder(new PostMaterialEvent());
            if (ExtForCore.Mods.isKubeJSLoaded()) {
                KJSEventWrapper.materialModification();
            }
            // --spacer--
        } else if (event.getRegistryKey() == Registries.FLUID) {
            // Material fluids
            GTFluids.generateMaterialFluids();
            // --spacer--
        } else if (event.getRegistryKey() == Registries.BLOCK) {
            // Material Blocks
            REGISTRATE.creativeModeTab(GTCreativeModeTabs.MATERIAL_BLOCK);
            GTMaterialBlocks.generateMaterialBlocks();   // Compressed Blocks
            GTMaterialBlocks.generateOreBlocks();        // Ore Blocks
            GTMaterialBlocks.generateOreIndicators();    // Ore Indicators
            GTMaterialBlocks.buildMaterialBlockTable();

            // Material Pipes/Wires
            REGISTRATE.creativeModeTab(GTCreativeModeTabs.MATERIAL_PIPE);
            GTMaterialBlocks.generateCableBlocks();        // Cable & Wire Blocks
            GTMaterialBlocks.generateFluidPipeBlocks();    // Fluid Pipe Blocks
            GTMaterialBlocks.generateItemPipeBlocks();     // Item Pipe Blocks
            // --spacer--
        } else if (event.getRegistryKey() == Registries.ITEM) {
            // Material Items & Tools
            GTMaterialItems.generateMaterialItems();
            GTMaterialItems.generateTools();
            GTMaterialItems.generateArmors();
            // --spacer--
        } else if (event.getRegistryKey() == Registries.BLOCK_ENTITY_TYPE) {
            GTBlockEntities.init();
        }
    }

    private static void postInitMaterials(Registry<Material> registry) {
        // Register all material manager registries, for materials with mod ids.
        EFAPI.materialManager.getUsedNamespaces().forEach(namespace -> {
            // Force the material lang generator to be at index 0, so that addons' lang generators can override it.
            GTRegistrate registrate = GTRegistrate.createIgnoringListenerErrors(namespace);
            AbstractRegistrateAccessor accessor = (AbstractRegistrateAccessor) registrate;
            if (accessor.getDoDatagen().get()) {
                List<NonNullConsumer<? extends RegistrateProvider>> providers = Multimaps.asMap(accessor.getDatagens())
                        .get(ProviderType.LANG);
                providers.addFirst(
                        (provider) -> MaterialLangGenerator.generate((RegistrateLangProvider) provider, namespace));
            }

            ModList.get().getModContainerById(namespace)
                    .map(ModContainer::getEventBus)
                    .ifPresent(registrate::registerEventListeners);
        });
    }

    @SubscribeEvent
    public static void registerRegistries(NewRegistryEvent event) {
        GTRegistries.getRegistries().forEach(event::register);
    }

    @SubscribeEvent
    public static void registerDataPackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(GTRegistries.ORE_VEIN_REGISTRY,
                GTOreDefinition.DIRECT_CODEC, GTOreDefinition.DIRECT_CODEC);
        event.dataPackRegistry(GTRegistries.BEDROCK_FLUID_REGISTRY,
                BedrockFluidDefinition.DIRECT_CODEC, BedrockFluidDefinition.DIRECT_CODEC);
        event.dataPackRegistry(GTRegistries.BEDROCK_ORE_REGISTRY,
                BedrockOreDefinition.DIRECT_CODEC, BedrockOreDefinition.DIRECT_CODEC);
    }

    @SubscribeEvent
    public static void modifyRegistries(ModifyRegistriesEvent event) {
        GTRegistries.MATERIALS.addCallback((BakeCallback<Material>) CommonProxy::postInitMaterials);
        GTRegistries.MACHINES.addCallback((BakeCallback<MachineDefinition>) GTMachines::bakeRenderStates);
    }

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // register the map ingredient converters for all of our ingredients
            // spotless:off
            MapIngredientTypeManager.registerMapIngredient(SizedFluidIngredient.class, (ingredient) -> {
                FluidIngredient inner = ingredient.ingredient();
                return MapIngredientTypeManager.getFrom(inner, FluidRecipeCapability.CAP);
            });
            MapIngredientTypeManager.registerMapIngredient(IntProviderFluidIngredient.class, (ingredient) -> {
                FluidIngredient inner = ingredient.getInner();
                return MapIngredientTypeManager.getFrom(inner, FluidRecipeCapability.CAP);
            });
            MapIngredientTypeManager.registerMapIngredient(CompoundFluidIngredient.class, (ingredient) -> {
                List<AbstractMapIngredient> list = new ObjectArrayList<>();
                for (FluidIngredient child : ingredient.children()) {
                    list.addAll(MapIngredientTypeManager.getFrom(child, FluidRecipeCapability.CAP));
                }
                return list;
            });

            MapIngredientTypeManager.registerMapIngredient(DataComponentFluidIngredient.class, FluidDataComponentMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(FluidIngredient.class, FluidTagMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(SingleFluidIngredient.class, FluidStackMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(IntersectionFluidIngredient.class, IntersectionMapIngredient::from);

            MapIngredientTypeManager.registerMapIngredient(FluidStack.class, FluidTagMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(FluidStack.class, FluidStackMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(FluidStack.class, FluidDataComponentMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(FluidStack.class, CustomFluidMapIngredient::from);

            MapIngredientTypeManager.registerMapIngredient(SizedIngredient.class, (ingredient) -> {
                Ingredient inner = ingredient.ingredient();
                if (inner.isCustom()) {
                    return MapIngredientTypeManager.getFrom(inner.getCustomIngredient(), ItemRecipeCapability.CAP);
                } else {
                    return MapIngredientTypeManager.getFrom(inner, ItemRecipeCapability.CAP);
                }
            });
            MapIngredientTypeManager.registerMapIngredient(IntProviderIngredient.class, (ingredient) -> {
                Ingredient inner = ingredient.getInner();
                if (inner.isCustom()) {
                    return MapIngredientTypeManager.getFrom(inner.getCustomIngredient(), ItemRecipeCapability.CAP);
                } else {
                    return MapIngredientTypeManager.getFrom(inner, ItemRecipeCapability.CAP);
                }
            });

            MapIngredientTypeManager.registerMapIngredient(DataComponentIngredient.class, ItemDataComponentMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(Ingredient.class, ItemTagMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(Ingredient.class, ItemStackMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(IntersectionIngredient.class, IntersectionMapIngredient::from);

            MapIngredientTypeManager.registerMapIngredient(ItemStack.class, ItemStackMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(ItemStack.class, ItemTagMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(ItemStack.class, ItemDataComponentMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(ItemStack.class, IntersectionMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(ItemStack.class, CustomItemMapIngredient::from);

            MapIngredientTypeManager.registerMapIngredient(IntCircuitIngredient.class, custom ->
                    List.of(new ItemStackMapIngredient(GTItems.PROGRAMMED_CIRCUIT.asStack(), custom.toVanilla())));
            // spotless:on

            if (ExtForCore.Mods.isCCTweakedLoaded()) {
                ExtForCore.LOGGER.info("CC: Tweaked found. Enabling integration...");
                CCTweakedPlugin.init();
            }
        });
    }

    @SubscribeEvent
    public static void loadComplete(FMLLoadCompleteEvent event) {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(FluidHandler.ITEM, BottleItemFluidHandler::new, Items.GLASS_BOTTLE);

        Stream<MachineDefinition> quantumTanks = Stream.of(GTMachines.SUPER_TANK, GTMachines.QUANTUM_TANK)
                .flatMap(Arrays::stream);
        quantumTanks = Stream.concat(quantumTanks, Stream.of(GTMachines.CREATIVE_FLUID));
        event.registerItem(FluidHandler.ITEM, (stack, ctx) -> {
            if (!(stack.getItem() instanceof MetaMachineItem machineItem)) {
                return null;
            }
            long capacity = QuantumTankMachine.TANK_CAPACITY.getLong(machineItem.getDefinition());
            if (capacity == -1) {
                return null;
            }
            return new QuantumFluidHandlerItemStack(stack, capacity);
        }, quantumTanks.filter(Objects::nonNull).map(MachineDefinition::getItem).toArray(Item[]::new));

        for (Block block : BuiltInRegistries.BLOCK) {
            if (ConfigHolder.INSTANCE.compat.energy.nativeEUToFE &&
                    event.isBlockRegistered(Capabilities.EnergyStorage.BLOCK, block)) {
                event.registerBlock(GTCapability.CAPABILITY_ENERGY_CONTAINER,
                        (level, pos, state, blockEntity, side) -> {
                            IEnergyStorage forgeEnergy = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos,
                                    state, blockEntity, side);
                            if (forgeEnergy != null) {
                                return new EUToFEProvider(forgeEnergy);
                            }
                            return null;
                        }, block);
            }

            if (block instanceof FluidPipeBlock fluidPipe) {
                fluidPipe.attachCapabilities(event);
            } else if (block instanceof CableBlock cable) {
                cable.attachCapabilities(event);
            } else if (block instanceof ItemPipeBlock itemPipe) {
                itemPipe.attachCapabilities(event);
            } else if (block instanceof LaserPipeBlock laserPipe) {
                laserPipe.attachCapabilities(event);
            } else if (block instanceof DuctPipeBlock duct) {
                duct.attachCapabilities(event);
            } else if (block instanceof MetaMachineBlock machine) {
                machine.attachCapabilities(event);
            } else if (block instanceof OpticalPipeBlock optical) {
                optical.attachCapabilities(event);
            }
        }

        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof IComponentItem componentItem) {
                componentItem.attachCapabilities(event);
            } else if (item instanceof IGTTool tool) {
                tool.attachCapabilities(event);
            } else if (item instanceof DrumMachineItem drum) {
                drum.attachCapabilities(event);
            } else if (item instanceof GTBucketItem) {
                event.registerItem(Capabilities.FluidHandler.ITEM,
                        (stack, ctx) -> new FluidBucketWrapper(stack), item);
            } else if (item instanceof PotionItem) {
                event.registerItem(Capabilities.FluidHandler.ITEM, PotionItemFluidHandler::new, item);
            }
        }
    }

    @SubscribeEvent
    public static void registerPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.CLIENT_RESOURCES) {
            // Clear old data
            GTDynamicResourcePack.clearClient();

            event.addRepositorySource(new GTPackSource("gtceu:dynamic_assets",
                    event.getPackType(),
                    Pack.Position.BOTTOM,
                    GTDynamicResourcePack::new));
        } else if (event.getPackType() == PackType.SERVER_DATA) {
            // Clear old data
            GTDynamicDataPack.clearServer();

            // LOADING MOVED TO ReloadableServerResourcesMixin

            event.addRepositorySource(new GTPackSource("gtceu:dynamic_data",
                    event.getPackType(),
                    Pack.Position.BOTTOM,
                    GTDynamicDataPack::new));
        }
    }

    @SubscribeEvent
    public static void addValidBlocksToBETypes(BlockEntityTypeAddBlocksEvent event) {
        event.modify(BlockEntityType.SIGN,
                GTBlocks.RUBBER_SIGN.get(),
                GTBlocks.RUBBER_WALL_SIGN.get(),
                GTBlocks.TREATED_WOOD_SIGN.get(),
                GTBlocks.TREATED_WOOD_WALL_SIGN.get());
        event.modify(BlockEntityType.HANGING_SIGN,
                GTBlocks.RUBBER_HANGING_SIGN.get(),
                GTBlocks.RUBBER_WALL_HANGING_SIGN.get(),
                GTBlocks.TREATED_WOOD_HANGING_SIGN.get(),
                GTBlocks.TREATED_WOOD_WALL_HANGING_SIGN.get());
    }

    public static final class KJSEventWrapper {

        public static void materialModification() {
            GTCEuStartupEvents.MATERIAL_MODIFICATION.post(new MaterialModificationEventJS());
        }
    }
}
