package com.extfro.extfrocore.integration.emi.category;

import com.extfro.extfrocore.api.material.tag.EFMaterialTags;
import com.extfro.extfrocore.integration.xei.orevein.XEIOreVeinDisplay;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class OreVeinEmiRecipe implements EmiRecipe {

    private final EmiRecipeCategory category;
    private final XEIOreVeinDisplay display;
    private final ResourceLocation id;
    private final List<EmiIngredient> inputs;
    private final List<EmiStack> outputs;

    public OreVeinEmiRecipe(EmiRecipeCategory category, XEIOreVeinDisplay display) {
        this.category = category;
        this.display = display;
        this.id = EmiXEIHelper.syntheticId("ore_vein", display.id());
        this.inputs = List.of();
        this.outputs = outputs(display);
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return category;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return outputs;
    }

    @Override
    public int getDisplayWidth() {
        return 150;
    }

    @Override
    public int getDisplayHeight() {
        return Math.max(54, 28 + Math.ceilDiv(outputs.size(), 6) * 18);
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addText(Component.translatable(display.translationKey()), 0, 0, 0x404040, false);
        widgets.addText(Component.literal(display.type().name()).withStyle(ChatFormatting.DARK_GRAY), 0, 11, 0x707070, false);
        widgets.addText(EmiXEIHelper.label("weight", display.weight()), 90, 11, 0x707070, false);
        for (int i = 0; i < outputs.size(); i++) {
            widgets.addSlot(outputs.get(i), i % 6 * 18, 28 + i / 6 * 18).recipeContext(this);
        }
    }

    private static List<EmiStack> outputs(XEIOreVeinDisplay display) {
        List<EmiStack> outputs = new ArrayList<>();
        if (display instanceof XEIOreVeinDisplay.OreVein oreVein) {
            for (XEIOreVeinDisplay.WeightedItemStack output : oreVein.outputs()) {
                EmiStack stack = EmiXEIHelper.item(output.stack());
                if (!stack.isEmpty()) {
                    outputs.add(stack);
                }
            }
        } else if (display instanceof XEIOreVeinDisplay.BedrockOreVein bedrockOreVein) {
            bedrockOreVein.materials().stream()
                    .map(material -> EmiXEIHelper.item(material.material().getModId(),
                            EFMaterialTags.DUST.getRegisteredName(material.material())))
                    .filter(stack -> !stack.isEmpty())
                    .forEach(outputs::add);
        } else if (display instanceof XEIOreVeinDisplay.BedrockFluidVein bedrockFluidVein) {
            EmiStack stack = EmiXEIHelper.fluid(bedrockFluidVein.fluid(), bedrockFluidVein.maximumYield());
            if (!stack.isEmpty()) {
                outputs.add(stack);
            }
        }
        return List.copyOf(outputs);
    }
}
