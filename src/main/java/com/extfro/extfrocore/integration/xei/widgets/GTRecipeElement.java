package com.extfro.extfrocore.integration.xei.widgets;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.EFValues;
import com.extfro.extfrocore.api.capability.recipe.CWURecipeCapability;
import com.extfro.extfrocore.api.capability.recipe.FluidRecipeCapability;
import com.extfro.extfrocore.api.capability.recipe.IO;
import com.extfro.extfrocore.api.capability.recipe.ItemRecipeCapability;
import com.extfro.extfrocore.api.capability.recipe.RecipeCapability;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.gui.ProgressTextures;
import com.extfro.extfrocore.api.recipe.GTRecipe;
import com.extfro.extfrocore.api.recipe.OverclockingLogic;
import com.extfro.extfrocore.api.recipe.RecipeCondition;
import com.extfro.extfrocore.api.recipe.RecipeHelper;
import com.extfro.extfrocore.api.recipe.chance.boost.ChanceBoostFunction;
import com.extfro.extfrocore.api.recipe.chance.logic.ChanceLogic;
import com.extfro.extfrocore.api.recipe.content.Content;
import com.extfro.extfrocore.api.recipe.ingredient.EnergyStack;
import com.extfro.extfrocore.api.recipe.ingredient.IntProviderFluidIngredient;
import com.extfro.extfrocore.api.recipe.ingredient.IntProviderIngredient;
import com.extfro.extfrocore.api.recipe.ingredient.SizedIngredientExtensions;
import com.extfro.extfrocore.api.registry.GTRegistries;
import com.extfro.extfrocore.client.TooltipsHandler;
import com.extfro.extfrocore.common.data.GTRecipeTypes;
import com.extfro.extfrocore.common.machine.multiblock.electric.FusionReactorMachine;
import com.extfro.extfrocore.common.recipe.condition.DimensionCondition;
import com.extfro.extfrocore.data.lang.LangHandler;
import com.extfro.extfrocore.integration.xei.entry.fluid.FluidEntryList;
import com.extfro.extfrocore.integration.xei.entry.item.ItemEntryList;
import com.extfro.extfrocore.integration.xei.handlers.fluid.CycleFluidEntryHandler;
import com.extfro.extfrocore.integration.xei.handlers.item.CycleItemEntryHandler;
import com.extfro.extfrocore.utils.FormattingUtil;
import com.extfro.extfrocore.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.lowdragmc.lowdraglib2.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.FillDirection;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.FluidSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ProgressBar;
import com.lowdragmc.lowdraglib2.gui.ui.event.HoverTooltips;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.integration.xei.IngredientIO;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import org.apache.commons.lang3.mutable.MutableInt;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.extfro.extfrocore.api.EFValues.MAX;
import static com.extfro.extfrocore.api.EFValues.OpV;
import static com.extfro.extfrocore.api.EFValues.UEV;
import static com.extfro.extfrocore.api.EFValues.UHV;
import static com.extfro.extfrocore.api.EFValues.UIV;
import static com.extfro.extfrocore.api.EFValues.ULV;
import static com.extfro.extfrocore.api.EFValues.UXV;
import static com.extfro.extfrocore.api.EFValues.V;
import static com.extfro.extfrocore.api.EFValues.ZPM;

public class GTRecipeElement extends UIElement {

    public static final int LINE_HEIGHT = 10;

    private final GTRecipe recipe;
    private final int xOffset;
    private final int minTier;
    private int tier;
    private int yOffset;
    private final List<Label> recipeParaTexts = new ArrayList<>();
    private Label recipeVoltageText;
    private Label voltageText;
    private static final Map<FluidSlot, List<FluidStack>> FLUID_SLOT_STACKS = new Object2ObjectLinkedOpenHashMap<>();

    public GTRecipeElement(GTRecipe recipe) {
        this.recipe = recipe;
        this.xOffset = getXOffset(recipe);
        this.minTier = RecipeHelper.getRecipeEUtTier(recipe);
        this.tier = minTier;
        var size = recipe.recipeType.getRecipeUI().getJEISize();
        layout(layout -> layout.width(size.width).height(size.height));
        build();
    }

    private static int getXOffset(GTRecipe recipe) {
        if (recipe.recipeType.getRecipeUI().getOriginalWidth() != recipe.recipeType.getRecipeUI().getJEISize().width) {
            return (recipe.recipeType.getRecipeUI().getJEISize().width -
                    recipe.recipeType.getRecipeUI().getOriginalWidth()) / 2;
        }
        return 0;
    }

    private void build() {
        clearAllChildren();
        recipeParaTexts.clear();
        FLUID_SLOT_STACKS.clear();
        recipeVoltageText = null;
        voltageText = null;

        var storages = Tables.newCustomTable(new EnumMap<>(IO.class), LinkedHashMap<RecipeCapability<?>, Object>::new);
        var contents = Tables.newCustomTable(new EnumMap<>(IO.class),
                LinkedHashMap<RecipeCapability<?>, List<Content>>::new);
        collectStorage(storages, contents, recipe);

        UIElement group = recipe.recipeType.getRecipeUI().createXEIElement(storages, recipe.data.copy(),
                recipe.conditions);
        addSlots(contents, group, recipe);
        addChild(group);

        int groupHeight = recipe.recipeType.getRecipeUI().getOriginalHeight();
        EnergyStack EUt = RecipeHelper.getRealEUt(recipe);
        int yOffset = 5 + groupHeight;
        this.yOffset = yOffset;
        yOffset += !EUt.isEmpty() ? 21 : 0;
        if (recipe.data.getBoolean("duration_is_total_cwu")) {
            yOffset -= 10;
        }

        MutableInt yOff = new MutableInt(yOffset);
        for (var capability : recipe.inputs.entrySet()) {
            addXEIInfo(capability.getKey(), capability.getValue(), false, true, yOff);
        }
        for (var capability : recipe.tickInputs.entrySet()) {
            addXEIInfo(capability.getKey(), capability.getValue(), true, true, yOff);
        }
        for (var capability : recipe.outputs.entrySet()) {
            addXEIInfo(capability.getKey(), capability.getValue(), false, false, yOff);
        }
        for (var capability : recipe.tickOutputs.entrySet()) {
            addXEIInfo(capability.getKey(), capability.getValue(), true, false, yOff);
        }

        for (RecipeCondition<?> condition : recipe.conditions) {
            if (condition.getTooltips() == null) continue;
            if (condition instanceof DimensionCondition) {
                // Dimension marker slots are migrated with the ore/dimension XEI page batch.
            } else {
                addChild(label(3 - xOffset, yOffset += LINE_HEIGHT, Component.literal(condition.getTooltips().getString())));
            }
        }
        for (Function<CompoundTag, String> dataInfo : recipe.recipeType.getDataInfos()) {
            addChild(label(3 - xOffset, yOffset += LINE_HEIGHT, Component.literal(dataInfo.apply(recipe.data))));
        }

        initializeRecipeText();
        addButtons();
    }

    private void initializeRecipeText() {
        String tierText = EFValues.VNF[tier];
        int textsY = yOffset - 10;
        int duration = recipe.duration;
        var EUt = RecipeHelper.getRealEUtWithIO(recipe);
        var minVoltageTier = GTUtil.getTierByVoltage(EUt.voltage());
        float minAmperage = (float) EUt.getTotalEU() / EFValues.V[minVoltageTier];

        List<Component> texts = getRecipeParaText(recipe, duration, EUt);
        for (Component text : texts) {
            textsY += 10;
            Label label = label(3 - xOffset, textsY, text);
            addChild(label);
            recipeParaTexts.add(label);
        }

        if (EUt.voltage() > 0) {
            textsY += 10;
            Component text = Component.translatable(EUt.isInput() ? "gtceu.recipe.eu" : "gtceu.recipe.eu_inverted",
                    FormattingUtil.formatNumber2Places(minAmperage), EFValues.VN[minVoltageTier])
                    .withStyle(ChatFormatting.UNDERLINE);
            recipeVoltageText = label(3 - xOffset, textsY, text);
            recipeVoltageText.style(style -> style.tooltips(
                    Component.translatable("gtceu.recipe.eu.total", FormattingUtil.formatNumbers(EUt.getTotalEU()))
                            .withStyle(ChatFormatting.UNDERLINE)));
            addChild(recipeVoltageText);
        }

        if (EUt.isInput()) {
            int y = (int) getSizeHeight() - 10;
            if (recipe.recipeType.isOffsetVoltageText()) {
                y = (int) getSizeHeight() - recipe.recipeType.getVoltageTextOffset();
            }
            voltageText = label(getVoltageXOffset() - xOffset, y, Component.literal(tierText));
            Button button = button((int) voltageText.getLayoutX(), y, 18, 10, IGuiTexture.EMPTY);
            button.style(style -> style.tooltips(LangHandler.getMultiLang("gtceu.oc.tooltip", EFValues.VNF[minTier])
                    .toArray(Component[]::new)));
            button.setOnClick(event -> setRecipeOC(event.button, event.isShiftDown()));
            addChild(button);
            addChild(voltageText);
        }
    }

    private static List<Component> getRecipeParaText(GTRecipe recipe, int duration, EnergyStack.WithIO eu) {
        List<Component> texts = new ArrayList<>();
        if (!recipe.data.getBoolean("hide_duration")) {
            texts.add(Component.translatable("gtceu.recipe.duration", FormattingUtil.formatNumbers(duration / 20f)));
        }
        if (eu.voltage() > 0) {
            long euTotal = eu.getTotalEU() * duration;
            if (recipe.data.getBoolean("duration_is_total_cwu") &&
                    recipe.tickInputs.containsKey(CWURecipeCapability.CAP)) {
                int minimumCWUt = Math.max(recipe.tickInputs.get(CWURecipeCapability.CAP).stream()
                        .map(Content::getContent).mapToInt(CWURecipeCapability.CAP::of).sum(), 1);
                texts.add(Component.translatable("gtceu.recipe.max_eu",
                        FormattingUtil.formatNumbers(euTotal / minimumCWUt)));
            } else {
                texts.add(Component.translatable("gtceu.recipe.total", FormattingUtil.formatNumbers(euTotal)));
            }
        }

        return texts;
    }

    private void addButtons() {
        if (!FMLLoader.isProduction()) {
            Button idButton = button((int) getSizeWidth() - xOffset - 18, (int) getSizeHeight() - 30, 15, 15,
                    new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture("ID")));
            idButton.style(style -> style.tooltips(Component.literal("click to copy: " + recipe.id)));
            idButton.setOnClick(event -> Minecraft.getInstance().keyboardHandler.setClipboard(recipe.id.toString()));
            addChild(idButton);
        }
    }

    private int getVoltageXOffset() {
        int x = (int) getSizeWidth() - switch (tier) {
            case ULV, EFValues.LuV, ZPM, UHV, UEV, UXV -> 20;
            case OpV, MAX -> 22;
            case UIV -> 18;
            case EFValues.IV -> 12;
            default -> 14;
        };
        if (!ExtForCore.Mods.isEMILoaded()) {
            x -= 3;
        }
        return x;
    }

    public void setRecipeOC(int button, boolean isShiftClick) {
        OverclockingLogic oc = OverclockingLogic.NON_PERFECT_OVERCLOCK;
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            setTier(tier + 1);
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            setTier(tier - 1);
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            setTier(minTier);
        }
        if (isShiftClick) {
            oc = OverclockingLogic.PERFECT_OVERCLOCK;
        }
        if (recipe.recipeType == GTRecipeTypes.FUSION_RECIPES) {
            oc = FusionReactorMachine.FUSION_OC;
        }
        updateOverclockText(oc);
        build();
    }

    private void updateOverclockText(OverclockingLogic logic) {
        EnergyStack inputEUt = recipe.getInputEUt();
        int duration = recipe.duration;
        String tierText = EFValues.VNF[tier];

        if (tier > minTier && !inputEUt.isEmpty()) {
            int ocs = tier - minTier;
            if (minTier == ULV) ocs--;
            var params = new OverclockingLogic.OCParams(inputEUt.voltage(), recipe.duration, ocs, 1);
            var result = logic.runOverclockingLogic(params, V[tier]);
            duration = (int) (duration * result.durationMultiplier());
            inputEUt = inputEUt.multiplyVoltage(result.eutMultiplier());
            tierText = tierText.formatted(ChatFormatting.ITALIC);
        }
        var minVoltageTier = GTUtil.getTierByVoltage(inputEUt.voltage());
        float minAmperage = (float) inputEUt.getTotalEU() / EFValues.V[minVoltageTier];
        List<Component> texts = getRecipeParaText(recipe, duration, new EnergyStack.WithIO(inputEUt, IO.IN));
        for (int i = 0; i < texts.size() && i < recipeParaTexts.size(); i++) {
            recipeParaTexts.get(i).setValue(texts.get(i));
        }
        if (voltageText != null) {
            voltageText.setText(tierText);
            voltageText.layout(layout -> layout.left(getVoltageXOffset() - xOffset));
        }
        if (recipeVoltageText != null) {
            recipeVoltageText.setValue(Component.translatable("gtceu.recipe.eu",
                    FormattingUtil.formatNumber2Places(minAmperage), EFValues.VN[minVoltageTier])
                    .withStyle(ChatFormatting.UNDERLINE));
            long totalEU = inputEUt.getTotalEU();
            recipeVoltageText.style(style -> style.tooltips(
                    Component.translatable("gtceu.recipe.eu.total", FormattingUtil.formatNumbers(totalEU))
                            .withStyle(ChatFormatting.UNDERLINE)));
        }
    }

    private void setTier(int tier) {
        this.tier = Mth.clamp(tier, minTier, EFValues.MAX);
    }

    private void collectStorage(Table<IO, RecipeCapability<?>, Object> extraTable,
                                Table<IO, RecipeCapability<?>, List<Content>> extraContents, GTRecipe recipe) {
        for (var entry : recipe.inputs.entrySet()) {
            extraContents.put(IO.IN, entry.getKey(), entry.getValue());
        }
        for (var entry : recipe.tickInputs.entrySet()) {
            mergeContents(extraContents, IO.IN, entry.getKey(), entry.getValue());
        }
        if (extraContents.containsRow(IO.IN)) {
            collectContainers(extraTable, extraContents.row(IO.IN), IO.IN);
        }

        for (var entry : recipe.outputs.entrySet()) {
            extraContents.put(IO.OUT, entry.getKey(), entry.getValue());
        }
        for (var entry : recipe.tickOutputs.entrySet()) {
            mergeContents(extraContents, IO.OUT, entry.getKey(), entry.getValue());
        }
        if (extraContents.containsRow(IO.OUT)) {
            collectContainers(extraTable, extraContents.row(IO.OUT), IO.OUT);
        }
    }

    private void mergeContents(Table<IO, RecipeCapability<?>, List<Content>> table, IO io, RecipeCapability<?> cap,
                               List<Content> contents) {
        if (table.get(io, cap) == null) {
            table.put(io, cap, contents);
        } else {
            ArrayList<Content> fullContents = new ArrayList<>(table.get(io, cap));
            fullContents.addAll(contents);
            table.put(io, cap, fullContents);
        }
    }

    private void collectContainers(Table<IO, RecipeCapability<?>, Object> table,
                                   Map<RecipeCapability<?>, List<Content>> contents, IO io) {
        Map<RecipeCapability<?>, List<Object>> capabilities = new Object2ObjectLinkedOpenHashMap<>();
        for (var entry : contents.entrySet()) {
            capabilities.put(entry.getKey(), entry.getKey().createXEIContainerContents(entry.getValue(), recipe, io));
        }
        for (var entry : capabilities.entrySet()) {
            int max = io == IO.IN ? recipe.recipeType.getMaxInputs(entry.getKey()) :
                    recipe.recipeType.getMaxOutputs(entry.getKey());
            while (entry.getValue().size() < max) {
                entry.getValue().add(null);
            }
            var container = entry.getKey().createXEIContainer(entry.getValue());
            if (container != null) {
                table.put(io, entry.getKey(), container);
            }
        }
    }

    private void addSlots(Table<IO, RecipeCapability<?>, List<Content>> contentTable, UIElement group, GTRecipe recipe) {
        for (var capabilityEntry : contentTable.rowMap().entrySet()) {
            IO io = capabilityEntry.getKey();
            for (var contentsEntry : capabilityEntry.getValue().entrySet()) {
                RecipeCapability<?> cap = contentsEntry.getKey();
                int nonTickCount = (io == IO.IN ? recipe.getInputContents(cap) :
                        recipe.getOutputContents(cap)).size();
                List<Content> contents = contentsEntry.getValue();
                group.selectRegex("^%s_[0-9]+$".formatted(cap.slotName(io))).forEach(element -> {
                    int index = slotIndex(element.getId());
                    if (index >= 0 && index < contents.size()) {
                        var content = contents.get(index);
                        applyContentInfo(element, cap, index, io, content, nonTickCount);
                    }
                });
            }
        }
    }

    private int slotIndex(String id) {
        int idx = id.lastIndexOf('_');
        if (idx < 0 || idx == id.length() - 1) return -1;
        try {
            return Integer.parseInt(id.substring(idx + 1));
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    private void applyContentInfo(UIElement element, RecipeCapability<?> cap, int index, IO io, Content content,
                                  int nonTickCount) {
        float chance = (float) recipe.getType().getChanceFunction()
                .getBoostedChance(content, minTier, tier) / content.maxChance;
        element.style(style -> style.overlay(content.createOverlay(index >= nonTickCount, minTier, tier,
                recipe.getType().getChanceFunction())));
        if (element instanceof ItemSlot itemSlot && cap == ItemRecipeCapability.CAP) {
            applyItemContent(itemSlot, index, io, content, chance);
        } else if (element instanceof FluidSlot fluidSlot && cap == FluidRecipeCapability.CAP) {
            applyFluidContent(fluidSlot, index, io, content, chance);
        }
    }

    private void applyItemContent(ItemSlot slot, int index, IO io, Content content, float chance) {
        IngredientIO ingredientIO = io == IO.IN ? IngredientIO.INPUT : IngredientIO.OUTPUT;
        if (io == IO.IN && (content.chance == 0 ||
                SizedIngredientExtensions.getContainedCustom(ItemRecipeCapability.CAP.of(content.content)) instanceof IntProviderIngredient)) {
            ingredientIO = IngredientIO.CATALYST;
        }
        slot.xeiRecipeSlot(ingredientIO, chance, 0, itemStacks(slot, index));
        slot.addEventListener(UIEvents.HOVER_TOOLTIPS, event -> {
            var tooltips = new ArrayList<>(slot.getFullTooltipTexts());
            addItemContentTooltips(tooltips, index, io, content);
            event.hoverTooltips = new HoverTooltips(tooltips, null, null, slot.getValue());
        });
    }

    private Stream<ItemStack> itemStacks(ItemSlot slot, int index) {
        if (slot.getSlot() != null && slot.getSlot().container instanceof CycleItemEntryHandler handler) {
            ItemEntryList entry = handler.getEntry(index);
            return entry == null ? Stream.empty() : entry.getStacks().stream();
        }
        ItemStack stack = slot.getValue();
        return stack.isEmpty() ? Stream.empty() : Stream.of(stack);
    }

    private void addItemContentTooltips(List<Component> tooltips, int index, IO io, Content content) {
        setConsumedChance(content,
                recipe.getChanceLogicForCapability(ItemRecipeCapability.CAP, io,
                        ItemRecipeCapability.CAP.isTickSlot(index, io, recipe)),
                tooltips, minTier, tier, recipe.getType().getChanceFunction());
        if (SizedIngredientExtensions.getContainedCustom(ItemRecipeCapability.CAP.of(content.content)) instanceof IntProviderIngredient ingredient) {
            IntProvider countProvider = ingredient.getCountProvider();
            tooltips.add(Component.translatable("gtceu.gui.content.count_range",
                    countProvider.getMinValue(), countProvider.getMaxValue()).withStyle(ChatFormatting.GOLD));
        }
        if (ItemRecipeCapability.CAP.isTickSlot(index, io, recipe)) {
            tooltips.add(Component.translatable("gtceu.gui.content.per_tick"));
        }
    }

    private void applyFluidContent(FluidSlot slot, int index, IO io, Content content, float chance) {
        IngredientIO ingredientIO = io == IO.IN && content.chance == 0 ? IngredientIO.CATALYST :
                (io == IO.IN ? IngredientIO.INPUT : IngredientIO.OUTPUT);
        slot.xeiRecipeSlot(ingredientIO, chance, 0, fluidStacks(slot, index));
        slot.addEventListener(UIEvents.HOVER_TOOLTIPS, event -> {
            var tooltips = new ArrayList<>(slot.getFullTooltipTexts());
            addFluidContentTooltips(tooltips, index, io, content);
            event.hoverTooltips = new HoverTooltips(tooltips, null, null, ItemStack.EMPTY);
        });
    }

    private Stream<FluidStack> fluidStacks(FluidSlot slot, int index) {
        List<FluidStack> stacks = FLUID_SLOT_STACKS.get(slot);
        if (stacks != null) return stacks.stream();
        FluidStack stack = slot.getFluid();
        return stack.isEmpty() ? Stream.empty() : Stream.of(stack);
    }

    public static void rememberFluidStacks(FluidSlot slot, IFluidHandler handler, int index) {
        if (handler instanceof CycleFluidEntryHandler cycle) {
            FluidEntryList entry = cycle.getEntry(index);
            FLUID_SLOT_STACKS.put(slot, entry == null ? List.of() : entry.getStacks());
        } else {
            FluidStack stack = handler.getFluidInTank(index);
            FLUID_SLOT_STACKS.put(slot, stack.isEmpty() ? List.of() : List.of(stack));
        }
    }

    private void addFluidContentTooltips(List<Component> tooltips, int index, IO io, Content content) {
        SizedFluidIngredient ingredient = FluidRecipeCapability.CAP.of(content.content);
        if (ingredient.getFluids().length > 0) {
            FluidStack stack = ingredient.getFluids()[0];
            TooltipsHandler.appendFluidTooltips(stack, tooltips::add,
                    TooltipFlag.NORMAL, Item.TooltipContext.of(GTRegistries.builtinRegistry()));
        }
        if (ingredient.ingredient() instanceof IntProviderFluidIngredient provider) {
            IntProvider countProvider = provider.getCountProvider();
            tooltips.add(Component.translatable("gtceu.gui.content.fluid_range",
                    countProvider.getMinValue(), countProvider.getMaxValue()).withStyle(ChatFormatting.GOLD));
        }
        setConsumedChance(content,
                recipe.getChanceLogicForCapability(FluidRecipeCapability.CAP, io,
                        FluidRecipeCapability.CAP.isTickSlot(index, io, recipe)),
                tooltips, minTier, tier, recipe.getType().getChanceFunction());
        if (FluidRecipeCapability.CAP.isTickSlot(index, io, recipe)) {
            tooltips.add(Component.translatable("gtceu.gui.content.per_tick"));
        }
    }

    private void addXEIInfo(RecipeCapability<?> capability, List<Content> contents, boolean perTick, boolean isInput,
                            MutableInt yOffset) {
        if (capability == CWURecipeCapability.CAP) {
            if (perTick) {
                int cwu = contents.stream().map(Content::getContent).mapToInt(CWURecipeCapability.CAP::of).sum();
                addChild(label(3 - xOffset, yOffset.addAndGet(10),
                        Component.translatable("gtceu.recipe.computation_per_tick", FormattingUtil.formatNumbers(cwu))));
            }
            if (recipe.data.getBoolean("duration_is_total_cwu")) {
                addChild(label(3 - xOffset, yOffset.addAndGet(10),
                        Component.translatable("gtceu.recipe.total_computation",
                                FormattingUtil.formatNumbers(recipe.duration))));
            }
        }
    }

    public static void setConsumedChance(Content content, ChanceLogic logic, List<Component> tooltips, int recipeTier,
                                         int chanceTier, ChanceBoostFunction function) {
        if (content.chance < ChanceLogic.getMaxChancedValue()) {
            int boostedChance = function.getBoostedChance(content, recipeTier, chanceTier);
            if (boostedChance == 0) {
                tooltips.add(Component.translatable("gtceu.gui.content.chance_nc"));
            } else {
                float baseChanceFloat = 100f * content.chance / content.maxChance;
                if (content.tierChanceBoost != 0) {
                    float boostedChanceFloat = 100f * boostedChance / content.maxChance;
                    if (logic != ChanceLogic.NONE && logic != ChanceLogic.OR) {
                        tooltips.add(Component.translatable("gtceu.gui.content.chance_base_logic",
                                FormattingUtil.formatNumber2Places(baseChanceFloat), logic.getTranslation())
                                .withStyle(ChatFormatting.YELLOW));
                    } else {
                        tooltips.add(FormattingUtil.formatPercentage2Places("gtceu.gui.content.chance_base",
                                baseChanceFloat));
                    }
                    String key = "gtceu.gui.content.chance_tier_boost_" +
                            ((content.tierChanceBoost > 0) ? "plus" : "minus");
                    tooltips.add(FormattingUtil.formatPercentage2Places(key,
                            Math.abs(100f * content.tierChanceBoost / content.maxChance)));
                    if (logic != ChanceLogic.NONE && logic != ChanceLogic.OR) {
                        tooltips.add(Component.translatable("gtceu.gui.content.chance_boosted_logic",
                                FormattingUtil.formatNumber2Places(boostedChanceFloat), logic.getTranslation())
                                .withStyle(ChatFormatting.YELLOW));
                    } else {
                        tooltips.add(FormattingUtil.formatPercentage2Places("gtceu.gui.content.chance_boosted",
                                boostedChanceFloat));
                    }
                } else if (logic != ChanceLogic.NONE && logic != ChanceLogic.OR) {
                    tooltips.add(Component.translatable("gtceu.gui.content.chance_no_boost_logic",
                            FormattingUtil.formatNumber2Places(baseChanceFloat), logic.getTranslation())
                            .withStyle(ChatFormatting.YELLOW));
                } else {
                    tooltips.add(FormattingUtil.formatPercentage2Places("gtceu.gui.content.chance_no_boost",
                            baseChanceFloat));
                }
            }
        }
    }

    public static UIElement fixed(int x, int y, int width, int height) {
        return new UIElement().layout(layout -> layout.left(x).top(y).width(width).height(height));
    }

    public static Label label(int x, int y, Component text) {
        Label label = new Label();
        label.setValue(text);
        label.layout(layout -> layout.left(x).top(y).width(140).height(10));
        label.textStyle(style -> style.textColor(-1).textShadow(true));
        return label;
    }

    public static Button button(int x, int y, int width, int height, IGuiTexture texture) {
        Button button = new Button().noText();
        button.layout(layout -> layout.left(x).top(y).width(width).height(height));
        button.buttonStyle(style -> style.baseTexture(texture).hoverTexture(texture).pressedTexture(texture));
        return button;
    }

    public static ProgressBar progressBar(int x, int y, int width, int height, ProgressTextures textures) {
        ProgressBar progressBar = new ProgressBar();
        progressBar.layout(layout -> layout.left(x).top(y).width(width).height(height));
        progressBar.setRange(0, 1).setProgress(1);
        progressBar.style(style -> style.background(textures.background()));
        progressBar.bar(bar -> bar.style(style -> style.background(textures.bar())));
        progressBar.progressBarStyle(style -> style.fillDirection(textures.fillDirection()));
        progressBar.label(Label::disabled);
        return progressBar;
    }

    public static ItemSlot itemSlot(int x, int y, IGuiTexture texture, String id) {
        ItemSlot slot = new ItemSlot();
        slot.layout(layout -> layout.left(x).top(y).width(18).height(18));
        slot.setId(id);
        slot.style(style -> style.background(texture));
        return slot;
    }

    public static ItemSlot itemSlot(int x, int y, IGuiTexture texture, String id,
                                    CycleItemEntryHandler handler, int index) {
        ItemSlot slot = itemSlot(x, y, texture, id);
        slot.bind(handler, index);
        return slot;
    }

    public static FluidSlot fluidSlot(int x, int y, IGuiTexture texture, String id) {
        FluidSlot slot = new FluidSlot();
        slot.layout(layout -> layout.left(x).top(y).width(18).height(18));
        slot.setId(id);
        slot.style(style -> style.background(texture));
        slot.slotStyle(style -> style.fillDirection(FillDirection.ALWAYS_FULL));
        slot.setAllowClickFilled(false).setAllowClickDrained(false);
        return slot;
    }

    public static FluidSlot fluidSlot(int x, int y, IGuiTexture texture, String id,
                                      CycleFluidEntryHandler handler, int index) {
        FluidSlot slot = fluidSlot(x, y, texture, id);
        slot.bind(handler, index);
        rememberFluidStacks(slot, handler, index);
        return slot;
    }
}
