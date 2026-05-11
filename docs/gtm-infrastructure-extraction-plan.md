# GTM Infrastructure Extraction Plan

This document records the extraction boundary for separating the GTM infrastructure into this library without carrying
over concrete GTM content such as the existing materials, machines, recipes, covers, or worldgen definitions.

Reference snapshots:

- `F:\1.20.1\GregTech-Modern`, branch `1.20.1`, commit `22cf6aaf3`, `2026-05-10 21:15:38 +0200`
- `F:\1.21.1\GregTech-Modern`, branch `1.21`, commit `84380f0c8`, `2026-05-10 11:32:58 +0000`

Use the 1.21 tree for NeoForge and Minecraft 1.21 API shape. Use the 1.20.1 tree as the feature reference where 1.21 is
behind.

## Scope Rule

Migrate infrastructure and extension points. Do not migrate GTM content.

Infrastructure means:

- registries, registry lifecycle, registration builders, and deferred registration glue
- material/type/property/tag-prefix systems, but not the stock material list
- fluid infrastructure, but not stock material fluids
- machine definitions, machine base classes, traits, capabilities, UI hooks, model states, and builders, but not
  concrete machine registrations or recipes
- multiblock pattern/checking/render-preview infrastructure, but not stock multiblock definitions
- recipe type/capability/condition/category/lookup infrastructure and datagen builders, but not GTM recipe files/loaders
  for concrete products
- dynamic data/resource pack systems
- datagen provider framework, model builders, tag/lang hooks, and runtime asset helpers
- sync/data-component/network infrastructure needed by the above
- client model/render infrastructure required by material, pipe, machine, and dynamic model systems
- config and compatibility hooks where they gate the infrastructure
- utility classes used by the extracted systems

Content means:

- concrete material declarations
- concrete machine declarations
- concrete covers, hatches, tools, armor, parts, circuits, ores, recipes, and worldgen definitions
- GTM language text for concrete content
- GTM textures/models for concrete content, except generic templates and material/icon-set templates
- optional integrations that only render or expose concrete GTM content

## Whole-Tree Inventory

Both versions have the same top-level Java roots:

- `api`
- `client`
- `common`
- `config`
- `core`
- `data`
- `integration`
- `utils`

The important package counts from the current snapshots:

| Area              | 1.20.1 | 1.21.1 | Extraction meaning                                                                    |
|-------------------|-------:|-------:|---------------------------------------------------------------------------------------|
| `api/data`        |     86 |     84 | materials, tag prefixes, worldgen descriptors, rotation state                         |
| `api/machine`     |     85 |     94 | machine definitions, traits, multiblocks, recipe logic                                |
| `api/recipe`      |     74 |     66 | recipe type, recipe content, lookup, ingredients, serializers                         |
| `api/gui`         |     62 |     62 | UI factories/widgets/fancy UI hooks                                                   |
| `api/item`        |     54 |     63 | component item system; 1.21 adds data components                                      |
| `api/registry`    |     13 |     10 | registries and Registrate builders                                                    |
| `api/sync_system` |     28 |     29 | managed sync, field metadata, network payloads                                        |
| `client/model`    |     35 |     35 | machine, pipe, material, item model loaders                                           |
| `client/renderer` |     47 |     45 | item/block/machine render support                                                     |
| `common/data`     |     55 |     63 | mostly content registrars; extract only infrastructure registrars                     |
| `common/machine`  |    128 |    114 | mostly concrete machines; extract only reusable abstract/base machinery               |
| `core/mixins`     |     74 |     77 | required accessors/hooks for registry, datagen, dynamic resources, rendering, recipes |
| `data/recipe`     |     70 |     71 | recipe builders and generated content; split builders from concrete recipe loaders    |
| `integration/kjs` |     45 |     60 | KubeJS builders/wrappers; optional, infrastructure only                               |

Resource roots:

- `assets/.../models`: generic templates and material/icon-set models are infrastructure; machine/content-specific
  models are content.
- `assets/.../textures`: material/icon-set templates and generic overlays can be infrastructure; named
  item/machine/material textures are content.
- `assets/.../sounds`: sound registry infrastructure can stay; actual named GTM sounds are content unless this library
  needs generic sound entries.
- `assets/.../ui`: recipe UI templates are infrastructure if recipe UI is migrated.
- `data/...`: most entries are concrete tags/loot/config; migrate only generic loader scaffolding and required metadata
  format examples.
- `META-INF/enum_extensions.json` exists only in 1.21 and must be considered when moving 1.21 enum proxy infrastructure.

## Version Delta That Matters

Prefer 1.21 API shape for these:

- NeoForge event classes and registration APIs
- `DataComponent`-based item state
- `GTRegistries` style built on `RegistryBuilder`, `RegisterEvent`, `IdMappingEvent`, `DataPackRegistryEvent`
- `GTCEu` constructor with `IEventBus` and `FMLModContainer`
- payload registration through the 1.21 network path
- `DataMapsHandler`
- `enum_extensions.json` and enum proxy registration
- `ResourceLocation` helper/mixin changes
- 1.21 KubeJS builder wrappers
- split recipe manager mixins: early/late hooks

Check 1.20.1 before finalizing these, because 1.20.1 may contain newer behavior not present in the 1.21 branch:

- material registry event flow and addon material registration
- material icon set/type fallback and generated models
- fluid builder behavior and material fluid generation
- machine builder feature surface
- multiblock builder feature surface
- dynamic resource pack behavior
- sync system changes from the external synced-data library
- config toggles and compatibility gates
- worldgen and map cache features

## Required Extraction Modules

### 1. Mod Entry And Lifecycle

Source anchors:

- `GTCEu.java`
- `common/CommonProxy.java`
- `client/ClientProxy.java`
- `api/GTCEuAPI.java`
- `api/GTValues.java`
- `config/ConfigHolder.java`

Extract:

- mod id helpers: `id`, `appendIdString`, path constants
- environment helpers: dev/prod/datagen/client-thread/server availability
- loaded-mod helper surface used by optional integrations
- common init order
- client init order
- high-tier/config gating if machine tiers are kept generic
- API singleton and public extension-state holders, renamed to this library style

Do not extract:

- GTM-specific high-tier business decisions unless a generic tier gate is needed
- concrete fusion tier setup
- concrete content calls such as stock blocks, items, fluids, machines, recipes

### 2. Registry Core

Source anchors:

- `api/registry/GTRegistries.java`
- 1.20.1 `api/registry/GTRegistry.java`
- `api/registry/registrate/GTRegistrate.java`
- `api/registry/registrate/MachineBuilder.java`
- `api/registry/registrate/MultiblockMachineBuilder.java`
- fluid builders under `api/registry/registrate`

Extract:

- central registry key definitions for extracted domains
- static registry creation and load order
- delayed registration table while registries are frozen
- registration event listeners and freeze/unfreeze handling
- `Registrate` wrapper caching and event-bus binding
- builders for machine, multiblock, sound, material item/block, fluid
- custom registry codec/buffer/tag helpers where required

Do not extract:

- GTM concrete registry entries
- registry keys for domains not migrated in the first pass

Current repository status:

- A minimal `EFRegistrate`, material registration, sound registration, fluid registration, sync system, and machine
  builder skeleton already exist.
- The next work should replace partial custom registry pieces with the full 1.21-style registry lifecycle while
  retaining this library naming.

### 3. Material And Tag Prefix System

Source anchors:

- `api/data/chemical/*`
- `api/data/tag/*`
- 1.20.1 material registry manager
- 1.21 `IMaterialRegistry`
- material item/block classes and renderers
- `data/lang/MaterialLangGenerator.java`
- material model/texture assets under `material_sets`

Extract:

- element model and registry
- material model, stack model, marker material model
- material properties and property keys
- material registry lifecycle
- material events: registry/open/post/freeze
- material icon sets/types and fallback lookup
- tag prefix definition logic
- material item/block creation helpers
- material language generation
- material icon model and texture template support
- generated material blocks/items/fluids/pipes infrastructure

Do not extract:

- concrete `GTMaterials` material list
- concrete `GTFoods`
- concrete ore vein definitions
- concrete item/part generation calls except as generic generators

Important 1.20 vs 1.21 split:

- 1.20.1 has `MaterialRegistryManager` with multi-registry flow and explicit material events.
- 1.21 collapses toward `IMaterialRegistry` backed by `GTRegistries.MATERIALS`.
- Keep 1.21 registry API shape, but preserve the 1.20.1 feature behavior for addon material registration and
  post-material modification.

### 4. Fluid Infrastructure

Source anchors:

- `api/fluids/*`
- `api/registry/registrate/GTClientFluidTypeExtensions.java`
- 1.20.1 `api/registry/registrate/forge/GTFluidBuilder.java`
- `common/fluid/*`
- `common/data/GTFluids.java`

Extract:

- fluid state/type/storage keys
- fluid attributes and attributed fluids
- client fluid type extensions
- fluid builder implementation
- material fluid generation mechanism
- potion/thermal/fluid-container helpers only if recipe/item infrastructure needs them

Do not extract:

- concrete GTM fluid registrations
- concrete named material fluids
- potion fluid registration unless generic fluid-container support depends on it

### 5. Machine Base System

Source anchors:

- `api/machine/MachineDefinition.java`
- `api/machine/MetaMachine.java`
- `api/machine/MultiblockMachineDefinition.java`
- `api/block/MetaMachineBlock.java`
- `api/item/MetaMachineItem.java`
- `api/data/RotationState.java`
- `api/machine/property/GTMachineModelProperties.java`
- `api/machine/trait/*`
- `api/machine/feature/*`
- `api/machine/TieredMachine.java`
- `api/machine/WorkableTieredMachine.java`
- `api/machine/SimpleTieredMachine.java`
- `api/machine/SimpleGeneratorMachine.java`
- steam and multiblock abstract/base classes

Extract:

- definition object and render state registry
- block/item/block-entity binding
- placement, rotation, shape, tooltip, default appearance, model-state hooks
- managed sync integration
- lifecycle hooks: load/unload/place/destroy/tick/render update
- machine trait holder and trait base classes
- recipe logic trait and notifiable item/fluid/energy handlers if recipes are migrated
- tiered, workable, simple generator, steam base classes as abstract infrastructure
- abstract multiblock controller and part bases

Do not extract:

- concrete simple machines
- concrete steam machines
- concrete multiblocks
- hatches and covers as concrete registrations
- concrete renderer registrations for named machines

Important note:

- `MetaMachine` in GTM is a central dependency hub. It touches sync, covers, tools, GUI, recipe logic, traits,
  ownership, rendering, components, and item save. It should be migrated after those base modules are available, not as
  an isolated copy.

### 6. Multiblock And Pattern System

Source anchors:

- `api/pattern/*`
- `api/machine/multiblock/*`
- `api/machine/feature/multiblock/*`
- multiblock shape info, pattern predicates, saved data
- XEI multiblock preview categories if recipe viewer integration is kept

Extract:

- block pattern and traceability predicates
- pattern match context and errors
- relative directions
- multiblock state and saved-data scheduling
- shape info builder
- controller/part interfaces and abstract base classes
- recovery item and part sorter hooks

Do not extract:

- concrete multiblock pattern declarations
- concrete controller machines
- concrete part machines unless they are abstract base types

### 7. Recipe Infrastructure

Source anchors:

- `api/recipe/*`
- `api/capability/recipe/*`
- `data/recipe/builder/*`
- `data/recipe/configurable/*`
- `common/recipe/*`
- recipe manager mixins

Extract:

- recipe type, category, condition, capability
- content model, chance logic, recipe modifier APIs
- ingredient/content codecs
- lookup/indexing and map ingredient infrastructure
- recipe builder base classes
- configurable recipe addition/removal framework
- serializers and ingredient type registration
- recipe manager hooks required for runtime/dynamic recipes

Do not extract:

- concrete generated recipe loaders
- chemistry chains
- machine recipe loaders
- material recipe generation outputs
- GTM crafting component table values, except generic table infrastructure

1.21 changes:

- use DataComponent ingredients instead of NBT predicate system where possible.
- 1.20.1 NBT predicate ingredients should not be copied directly if the target explicitly avoids `CompoundTag` in
  exposed code.

### 8. Dynamic Data And Resource Packs

Source anchors:

- `data/pack/*`
- `data/pack/event/RegisterDynamicResourcesEvent.java`
- `CommonProxy.registerPackFinders`
- `ClientProxy.registerDynamicAssets`
- `ReloadableServerResourcesMixin`
- `client/model/*`
- `utils/data/RuntimeBlockstateProvider.java`
- `utils/data/RuntimeExistingFileHelper.java`

Extract:

- dynamic resource pack contents
- dynamic client resource pack
- dynamic server data pack
- pack source
- dynamic resource registration event
- model manager reload hook
- runtime blockstate/model provider
- runtime existing-file helper

Do not extract:

- concrete recipe additions/removals
- concrete dungeon loot additions
- concrete pipe/material/machine dynamic model entries until their systems are migrated

Current repository status:

- A dynamic client resource pack exists. It should be expanded to include server data pack behavior if recipe/worldgen
  dynamic data is migrated.

### 9. Datagen Framework

Source anchors:

- `data/DataGenerators.java`
- `data/GregTechDatagen.java`
- `data/lang/*`
- `data/model/*`
- `data/tags/*`
- `data/datamap/DataMapsHandler.java` in 1.21
- `core/mixins/registrate/*`

Extract:

- Registrate provider replacement for blockstate/model generation
- blockstate model loader
- machine/pipe model builders
- lang handler framework
- material lang generator
- generic tag loaders
- data map handler
- sound provider hook
- server datapack built-in provider hooks for migrated datapack registries

Do not extract:

- concrete language lines
- concrete tags for GTM content
- concrete ore/worldgen bootstrap values

### 10. Client Model And Rendering

Source anchors:

- `client/model/*`
- `client/renderer/*`
- `client/util/*`
- `ClientProxy`
- client mixins

Extract:

- material item/block renderers
- machine model loader and machine model state system
- item model wrappers needed by dynamic item rendering
- pipe model loader if pipe infrastructure is migrated
- dynamic render manager and render type registry only as generic infra
- tooltip/render utility classes used by migrated systems

Do not extract:

- concrete machine dynamic render registrations
- concrete armor/tool renderers unless tool/armor infrastructure is migrated
- map/minimap visual content

### 11. Sync And Network

Source anchors:

- `api/sync_system/*`
- `common/network/*`
- payload registration in 1.21
- LDLib sync mixins

Extract:

- managed sync block entity
- sync field annotations
- sync metadata scanners
- save/item/client/server sync holders
- network payloads
- key mapping sync if UI/machines need it

Do not extract:

- concrete packet types for worldgen/map cache unless those systems are migrated

Important constraint:

- Public-facing new code should avoid exposing `CompoundTag` where possible. If internal Minecraft overrides require it,
  keep it contained inside sync implementation classes.

### 12. Config

Source anchors:

- `config/ConfigHolder.java`
- language config entries
- feature gates referenced from builders and proxies

Extract:

- config holder and categories needed by migrated infrastructure
- machine render default toggle
- recipe/config gates if recipe infra is migrated
- compatibility toggles for optional integrations

Do not extract:

- GTM balance values unless generic defaults require them
- concrete content enable/disable flags

### 13. Mixin And Accessor Layer

Source anchors:

- `core/mixins/registrate/AbstractRegistrateAccessor.java`
- `core/mixins/registrate/RegistrateDataProviderAccessor.java`
- `core/mixins/ResourceLocationMixin.java`
- `core/mixins/ReloadableServerResourcesMixin.java`
- `core/mixins/RecipeManagerEarlyMixin.java`
- `core/mixins/RecipeManagerLateMixin.java`
- `core/mixins/MappedRegistryMixin.java`
- `core/mixins/BlockEntityMixin.java`
- client model/render mixins
- `gtceu.mixins.json`

Extract only mixins required by migrated infrastructure:

- Registrate datagen provider accessors
- dynamic resource/model reload hooks
- recipe manager hooks if dynamic recipes are migrated
- registry access hooks if using 1.21 static registries
- client model hooks for dynamic material/machine models
- LDLib compatibility hooks required by sync/UI scenes

Do not extract:

- minimap mixins unless map/worldgen overlay integration is migrated
- JEI/EMI/REI mixins unless recipe viewer integration is migrated
- armor/heart/render mixins unless those systems are migrated
- KubeJS mixins unless KubeJS integration is migrated

### 14. Integration Packages

Treat integrations as optional modules. They should not be required for the core infrastructure.

Possible infrastructure integrations:

- KubeJS builders for materials, tag prefixes, recipe types, machine builders, worldgen definitions
- JEI/EMI/REI generic recipe category display and multiblock preview
- Jade providers for generic machine traits
- AE2/CCTweaked/Create/map integrations only if their abstract API surface is needed

Current XEI status:

- `integration.xei.entry.item/*` and `integration.xei.entry.fluid/*` provide item/fluid entry lists for stacks, tags,
  and holder sets.
- `integration.xei.handlers.item.CycleItemEntryHandler` and `integration.xei.handlers.fluid.CycleFluidEntryHandler`
  provide viewer-facing cycling handlers.
- `integration.xei.recipe/*` provides neutral recipe display collection and registration hooks over `RecipeCategory` and
  `MachineRecipe`.
- `integration.jei.EFJEIPlugin` and `integration.emi.EFEMIPlugin` provide minimal entrypoints and registrar tables.
- `integration.xei.orevein/*`, `integration.xei.oreprocessing/*`, `integration.xei.circuit/*`, and
  `integration.xei.multipage/*` provide neutral data layers for ore vein, ore processing, circuit, and multiblock info
  pages.
- `integration.jei.category/*` registers JEI categories, recipes, and catalysts for those four page families.
- `integration.emi.category/*` registers EMI categories, recipes, workstations, and named category titles for those four
  page families.
- Remaining integration work is recipe transfer handlers, richer multiblock previews, and optional REI/KubeJS/Jade
  bridges. Do not add concrete GTM machines, items, materials, ore veins, or circuit content.

Do not extract:

- integrations that only expose concrete GTM machines/items/worldgen
- hard dependencies on optional mods in the core module

## Extraction Order

Recommended order:

1. Entry/lifecycle helpers and naming utilities.
2. Registry core and Registrate wrapper.
3. Dynamic pack and datagen provider replacement.
4. Material + icon set + tag prefix infrastructure.
5. Fluid builder and material fluid generation infrastructure.
6. Sync system, network payloads, and data components.
7. Machine definition/block/item/base block entity and render state.
8. Machine trait system and recipe logic base.
9. Multiblock pattern/controller/part infrastructure.
10. Recipe type/capability/condition/category and recipe lookup.
11. Client model/render infrastructure for material, machine, pipe.
12. Pipe/cable infrastructure if needed by machines.
13. Worldgen descriptor infrastructure if material ores are needed.
14. KubeJS and recipe viewer integration modules.

Each step should compile before starting the next one.

## Naming Rules For This Repository

- Do not keep `GT`, `GTCEu`, `GregTech`, `GTM`, or `gtceu` names in production code.
- Foundation class names that are generic in the GTM API may stay unprefixed when requested, for example
  `MachineDefinition`, `MultiblockMachineDefinition`, `MetaMachine`.
- Registry entry-point classes already following this repository's style should remain prefixed, for example
  `EFRegistrate` and `EFRegistration`.
- Resource paths must use this mod id and this library's naming. Do not keep `frame_gt`; use this repository's
  normalized material icon names.

## Current Repository Coverage Snapshot

Already present or started:

- material property/icon/tag basics
- material item/block rendering and model fallback
- fluid builder/storage basics
- sound entry builder wired through `EFRegistrate`
- dynamic client resource pack basics
- sync system from the external synced-data library
- `EFRegistrate`
- minimal machine definition/block/item/builder/multiblock skeleton
- `MachineRegistry`

Known gaps before calling the extraction complete:

- full 1.21-style static registry lifecycle
- full material registry lifecycle parity with 1.20.1 addon events
- tag prefix system parity
- full machine `MetaMachine` lifecycle, traits, recipe logic, cover/UI/tool hooks
- multiblock pattern checker and saved-data loop
- recipe type/capability/condition/category infrastructure
- runtime server data pack and dynamic recipe loading
- datagen provider replacement and generic tag/lang/model handlers
- 1.21 data components used by item/machine/tool infrastructure
- mixins required by full datagen/dynamic-resource/recipe/registry behavior
- optional integrations split into separate guarded modules

## Validation Checklist

After every extraction slice:

- `.\gradlew.bat compileJava --rerun-tasks`
- IDEA project build
- scan old names:
    - `rg -n -i "gtceu|gregtech|gtm|GTCEu|frame_gt" src/main/java src/main/resources`
- scan newly added public infrastructure for accidental `CompoundTag` exposure
- if datagen changed, run the repository's data generation task
- if resources changed, inspect generated model/resource paths for the correct namespace
- if registry lifecycle changed, run a client/server startup or a minimal game test

## Cover Infrastructure Gap Tracking

Reference anchors:

- `api/cover/CoverDefinition.java`
- `api/cover/CoverBehavior.java`
- `api/capability/ICoverable.java`
- `api/machine/MachineCoverContainer.java`
- `api/cover/filter/*`
- `client/renderer/cover/*`
- `api/gui/factory/CoverUIFactory.java`
- `api/gui/widget/CoverConfigurator.java`
- `api/gui/widget/directional/handlers/CoverableConfigHandler.java`
- `api/recipe/FacadeCoverRecipe.java`
- `api/placeholder/IPlaceholderInfoProviderCover.java`

Already extracted in this repository:

- cover definition and cover behavior base classes
- cover custom registry and `EFRegistrate.cover(...)`
- machine cover container and machine lifecycle hooks
- cover save/client sync using `SyncTagMap`
- basic cover capability registration
- tool interaction chain: side hit detection, remove/install cover actions, screwdriver/soft-mallet hooks, and tool
  highlight hooks
- cover UI factory and configurator hooks, adapted to this repository's UI dependencies
- full cover filter package: base filter, item/fluid filters, tag filters, simple filters, filter handlers
- cover client renderer implementations: coverable renderer, simple plate renderer, IO renderer, text renderer
- basic renderer interfaces and dynamic renderer hooks
- redstone output aggregation from covers into block signal methods
- item/fluid capability wrapping chain from machine handlers through attached covers
- item-to-cover-definition install mapping
- tick subscription and copyable config interfaces
- dynamic cover block entity renderer registration for registered machine block entity types

Remaining infrastructure gaps:

- static baked machine model integration so cover quads and cover render types are invoked
- real cover GUI opener registration for the final UI framework; current `CoverUIFactory` is the neutral extension point
- smart item filter only if generic recipe/machine lookup is available without concrete content coupling
- facade recipe infrastructure only if the generic facade path is needed; do not migrate concrete facade cover content
- placeholder/info-provider cover extension hook if placeholder integration is migrated

Do not extract as infrastructure:

- concrete cover implementations such as conveyor, pump, robot arm, facade, filters, detectors, ender links, storage,
  solar panel, voiding, wireless transmitter, and infinite water
- concrete cover registrations, textures, language lines, and recipes

Current implementation order:

1. Write this gap list into the extraction document.
2. Migrate `api.cover.filter` in parallel. Done.
3. Migrate `client.renderer.cover` generic pieces in parallel. Done.
4. Add main-thread tool/UI hook surface and `MetaMachineBlock` interaction dispatch. Done.
5. Integrate redstone/capability/render hooks that are required for compile-safe behavior. Partially done; dynamic cover
   block entity rendering is registered, while baked model integration still needs the machine model system.
6. Run `compileJava`, stale-name scan, and `CompoundTag` scan for new public cover code.
