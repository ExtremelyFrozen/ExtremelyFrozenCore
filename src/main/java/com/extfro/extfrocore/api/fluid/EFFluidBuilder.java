package com.extfro.extfrocore.api.fluid;

import com.extfro.extfrocore.api.material.EFMaterial;
import com.extfro.extfrocore.api.material.info.EFMaterialFlags;
import com.extfro.extfrocore.api.material.property.EFBlastProperty;
import com.extfro.extfrocore.api.material.property.EFMaterialPropertyKey;
import com.extfro.extfrocore.api.registry.registrate.EFRegistrate;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidType;

import com.google.common.base.Preconditions;
import com.tterrag.registrate.builders.FluidBuilder;
import com.tterrag.registrate.util.entry.FluidEntry;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.Tolerate;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

@Accessors(fluent = true, chain = true)
public class EFFluidBuilder {

    public static final int ROOM_TEMPERATURE = 293;
    public static final int SOLID_LIQUID_TEMPERATURE = 1200;
    public static final int LIQUID_TEMPERATURE_OFFSET = 200;
    public static final int GAS_TEMPERATURE_OFFSET = 300;
    public static final int BASE_PLASMA_TEMPERATURE = 10000;
    public static final int DEFAULT_LIQUID_DENSITY = 1000;
    public static final int DEFAULT_GAS_DENSITY = -100;
    public static final int DEFAULT_PLASMA_DENSITY = 100;
    public static final int DEFAULT_LIQUID_VISCOSITY = 1000;
    public static final int DEFAULT_GAS_VISCOSITY = 100;
    public static final int DEFAULT_PLASMA_VISCOSITY = 10;
    public static final int STICKY_LIQUID_VISCOSITY = 6000;

    private static final int INFER_TEMPERATURE = -1;
    private static final int INFER_COLOR = 0xFFFFFFFF;
    private static final int INFER_DENSITY = -1;
    private static final int INFER_LUMINOSITY = -1;
    private static final int INFER_VISCOSITY = -1;

    @Setter
    private String name = null;
    @Setter
    private String translation = null;
    private final Collection<EFFluidAttribute> attributes = new ArrayList<>();
    @Setter
    private EFFluidState state = EFFluidState.LIQUID;
    @Getter
    private int temperature = INFER_TEMPERATURE;
    private int color = INFER_COLOR;
    private boolean isColorEnabled = true;
    @Setter
    private int density = INFER_DENSITY;
    private int luminosity = INFER_LUMINOSITY;
    private int viscosity = INFER_VISCOSITY;
    @Setter
    private int burnTime = -1;
    @Getter
    @Setter
    private ResourceLocation still = null;
    @Getter
    @Setter
    private ResourceLocation flowing = null;
    private boolean hasCustomStill = false;
    private boolean hasCustomFlowing = false;
    @Getter
    private boolean hasFluidBlock = false;
    private boolean hasBucket = true;

    public @NotNull EFFluidBuilder temperature(int temperature) {
        Preconditions.checkArgument(temperature >= 0, "temperature must be >= 0");
        this.temperature = temperature;
        return this;
    }

    public @NotNull EFFluidBuilder color(int color) {
        this.color = color | 0xFF000000;
        if (this.color == INFER_COLOR) {
            return disableColor();
        }
        return this;
    }

    public @NotNull EFFluidBuilder disableColor() {
        this.isColorEnabled = false;
        return this;
    }

    @Tolerate
    public @NotNull EFFluidBuilder density(double density) {
        return density(convertToMCDensity(density));
    }

    private static int convertToMCDensity(double density) {
        if (density > 0.001225) {
            return (int) (1000 * density);
        } else if (density < 0.001225) {
            return (int) (-0.1 / density);
        }
        return 0;
    }

    public @NotNull EFFluidBuilder luminosity(int luminosity) {
        Preconditions.checkArgument(luminosity >= 0 && luminosity < 16, "luminosity must be >= 0 and < 16");
        this.luminosity = luminosity;
        return this;
    }

    public @NotNull EFFluidBuilder viscosity(int viscosity) {
        Preconditions.checkArgument(viscosity >= 0, "viscosity must be >= 0");
        this.viscosity = viscosity;
        return this;
    }

    public @NotNull EFFluidBuilder viscosity(double viscosity) {
        return viscosity((int) (viscosity * 10000));
    }

    public @NotNull EFFluidBuilder attribute(@NotNull EFFluidAttribute attribute) {
        this.attributes.add(attribute);
        return this;
    }

    public @NotNull EFFluidBuilder attributes(@NotNull EFFluidAttribute @NotNull... attributes) {
        Collections.addAll(this.attributes, attributes);
        return this;
    }

    public @NotNull EFFluidBuilder customStill() {
        return textures(true);
    }

    public @NotNull EFFluidBuilder textures(boolean hasCustomStill) {
        this.hasCustomStill = hasCustomStill;
        this.isColorEnabled = false;
        return this;
    }

    public @NotNull EFFluidBuilder textures(boolean hasCustomStill, boolean hasCustomFlowing) {
        this.hasCustomStill = hasCustomStill;
        this.hasCustomFlowing = hasCustomFlowing;
        this.isColorEnabled = false;
        return this;
    }

    public @NotNull EFFluidBuilder block() {
        this.hasFluidBlock = true;
        return this;
    }

    public @NotNull EFFluidBuilder disableBucket() {
        this.hasBucket = false;
        return this;
    }

    public @NotNull Supplier<? extends Fluid> build(EFMaterial material, @NotNull EFFluidStorageKey key,
                                                    @NotNull EFRegistrate registrate) {
        determineName(material, key);
        determineTextures(material, key, material.getModId());
        determineState(key);
        determineTemperature(material);
        determineColor(material);
        determineDensity();
        determineLuminosity(material);
        determineViscosity(material);

        String translationKey = translation != null ? translation : key.getTranslationKeyFor(material);
        FluidBuilder<EFFluid.Flowing, EFRegistrate> builder = registrate.fluid(
                name, still, flowing,
                (properties, stillTexture, flowingTexture) -> createFluidType(
                        properties, translationKey, material),
                properties -> new EFFluid.Flowing(properties, state, burnTime))
                .lang(material.getDefaultTranslation())
                .clientExtension(() -> () -> new EFClientFluidTypeExtensions(still, flowing,
                        isColorEnabled ? color : 0xFFFFFFFF))
                .properties(properties -> properties
                        .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                        .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                        .temperature(temperature)
                        .density(density)
                        .lightLevel(luminosity)
                        .viscosity(viscosity))
                .fluidProperties(properties -> properties
                        .explosionResistance(10)
                        .tickRate(5)
                        .slopeFindDistance(4)
                        .levelDecreasePerBlock(1))
                .source(properties -> new EFFluid.Source(properties, state, burnTime))
                .tag(state.getTagKey());

        if (key.getExtraTag() != null) {
            builder.tag(key.getExtraTag());
        }
        if (!hasFluidBlock) {
            builder.noBlock();
        }
        if (!hasBucket) {
            builder.noBucket();
        }
        builder.onRegister(fluid -> {
            if (fluid.getSource() instanceof EFFluid source) {
                attributes.forEach(source::addAttribute);
            }
            if (fluid.getFlowing() instanceof EFFluid flowing) {
                attributes.forEach(flowing::addAttribute);
            }
        });

        FluidEntry<EFFluid.Flowing> entry = builder.register();
        return entry::getSource;
    }

    private @NotNull FluidType createFluidType(FluidType.Properties properties, String translationKey,
                                               EFMaterial material) {
        return new EFFluidType(properties, translationKey, material);
    }

    private void determineName(@NotNull EFMaterial material, EFFluidStorageKey key) {
        if (name == null) {
            if (material.isEmpty() || key == null) {
                throw new IllegalArgumentException("Fluid must have a name");
            }
            name = key.getRegistryNameFor(material);
        }
    }

    private void determineTextures(@NotNull EFMaterial material, EFFluidStorageKey key, @NotNull String modId) {
        if (still == null) {
            if (!material.isEmpty() && key != null && !hasCustomStill) {
                still = key.getIconType().getBlockTexturePath(material.getMaterialIconSet(), true);
            } else {
                still = ResourceLocation.fromNamespaceAndPath(modId, "block/fluids/fluid." + name);
            }
        }
        if (flowing == null) {
            flowing = hasCustomFlowing ?
                    ResourceLocation.fromNamespaceAndPath(modId, "block/fluids/fluid." + name + "_flow") : still;
        }
    }

    private void determineState(@NotNull EFFluidStorageKey key) {
        if (state == null) {
            state = key.getDefaultFluidState() != null ? key.getDefaultFluidState() : EFFluidState.LIQUID;
        }
    }

    private void determineTemperature(@NotNull EFMaterial material) {
        if (temperature != INFER_TEMPERATURE) return;
        EFBlastProperty property = material.getProperty(EFMaterialPropertyKey.BLAST);
        if (property == null) {
            temperature = switch (state) {
                case LIQUID -> material.hasProperty(EFMaterialPropertyKey.DUST) ? SOLID_LIQUID_TEMPERATURE :
                        ROOM_TEMPERATURE;
                case GAS -> ROOM_TEMPERATURE;
                case PLASMA -> {
                    EFFluidBuilder primaryBuilder = null;
                    EFFluidBuilder plasmaBuilder = null;
                    if (material.hasFluid()) {
                        primaryBuilder = material.getFluidBuilder();
                        plasmaBuilder = material.getFluidBuilder(EFFluidStorageKeys.PLASMA);
                    }
                    if (primaryBuilder != null && primaryBuilder != plasmaBuilder) {
                        yield BASE_PLASMA_TEMPERATURE + primaryBuilder.temperature;
                    }
                    yield BASE_PLASMA_TEMPERATURE;
                }
            };
        } else {
            temperature = property.getBlastTemperature() + switch (state) {
                case LIQUID -> LIQUID_TEMPERATURE_OFFSET;
                case GAS -> GAS_TEMPERATURE_OFFSET;
                case PLASMA -> BASE_PLASMA_TEMPERATURE;
            };
        }
    }

    private void determineColor(@NotNull EFMaterial material) {
        if (color == INFER_COLOR && isColorEnabled && !material.isEmpty()) {
            color = material.getMaterialARGB();
        }
    }

    private void determineDensity() {
        if (density != INFER_DENSITY) return;
        density = switch (state) {
            case LIQUID -> DEFAULT_LIQUID_DENSITY;
            case GAS -> DEFAULT_GAS_DENSITY;
            case PLASMA -> DEFAULT_PLASMA_DENSITY;
        };
    }

    private void determineLuminosity(@NotNull EFMaterial material) {
        if (luminosity != INFER_LUMINOSITY) return;
        if (state == EFFluidState.PLASMA) {
            luminosity = 15;
        } else if (!material.isEmpty()) {
            if (material.hasFlag(EFMaterialFlags.PHOSPHORESCENT)) {
                luminosity = 15;
            } else if (state == EFFluidState.LIQUID && material.hasProperty(EFMaterialPropertyKey.DUST)) {
                luminosity = 10;
            } else {
                luminosity = 0;
            }
        } else {
            luminosity = 0;
        }
    }

    private void determineViscosity(@NotNull EFMaterial material) {
        if (viscosity != INFER_VISCOSITY) return;
        viscosity = switch (state) {
            case LIQUID -> material.hasFlag(EFMaterialFlags.STICKY) ? STICKY_LIQUID_VISCOSITY :
                    DEFAULT_LIQUID_VISCOSITY;
            case GAS -> DEFAULT_GAS_VISCOSITY;
            case PLASMA -> DEFAULT_PLASMA_VISCOSITY;
        };
    }
}
