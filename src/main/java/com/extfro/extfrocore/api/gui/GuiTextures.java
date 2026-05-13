package com.extfro.extfrocore.api.gui;

import com.extfro.extfrocore.api.gui.texture.CroppedTexture;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;

@SuppressWarnings("unused")
public class GuiTextures {

    private static SpriteTexture texture(String path) {
        return SpriteTexture.of(path);
    }

    private static SpriteTexture border(String path, int left, int top) {
        return SpriteTexture.of(path).setBorder(left, top, left, top);
    }

    // GREGTECH
    public static final SpriteTexture GREGTECH_LOGO = texture("extfrocore:textures/gui/icon/gregtech_logo.png");
    public static final SpriteTexture GREGTECH_LOGO_XMAS = texture("extfrocore:textures/gui/icon/gregtech_logo_xmas.png");

    // HUD
    public static final SpriteTexture TOOL_FRONT_FACING_ROTATION = texture("extfrocore:textures/gui/overlay/tool_front_facing_rotation.png");
    public static final SpriteTexture TOOL_IO_FACING_ROTATION = texture("extfrocore:textures/gui/overlay/tool_io_facing_rotation.png");
    public static final SpriteTexture TOOL_PAUSE = texture("extfrocore:textures/gui/overlay/tool_pause.png");
    public static final SpriteTexture TOOL_START = texture("extfrocore:textures/gui/overlay/tool_start.png");
    public static final SpriteTexture TOOL_COVER_SETTINGS = texture("extfrocore:textures/gui/overlay/tool_cover_settings.png");
    public static final SpriteTexture TOOL_MUTE = texture("extfrocore:textures/gui/overlay/tool_mute.png");
    public static final SpriteTexture TOOL_SOUND = texture("extfrocore:textures/gui/overlay/tool_sound.png");
    public static final SpriteTexture TOOL_ALLOW_INPUT = texture("extfrocore:textures/gui/overlay/tool_allow_input.png");
    public static final SpriteTexture TOOL_ATTACH_COVER = texture("extfrocore:textures/gui/overlay/tool_attach_cover.png");
    public static final SpriteTexture TOOL_REMOVE_COVER = texture("extfrocore:textures/gui/overlay/tool_remove_cover.png");
    public static final SpriteTexture TOOL_PIPE_BLOCK = texture("extfrocore:textures/gui/overlay/tool_pipe_block.png");
    public static final SpriteTexture TOOL_PIPE_CONNECT = texture("extfrocore:textures/gui/overlay/tool_pipe_connect.png");
    public static final SpriteTexture TOOL_WIRE_BLOCK = texture("extfrocore:textures/gui/overlay/tool_wire_block.png");
    public static final SpriteTexture TOOL_WIRE_CONNECT = texture("extfrocore:textures/gui/overlay/tool_wire_connect.png");
    public static final SpriteTexture TOOL_AUTO_OUTPUT = texture("extfrocore:textures/gui/overlay/tool_auto_output.png");
    public static final SpriteTexture TOOL_DISABLE_AUTO_OUTPUT = texture("extfrocore:textures/gui/overlay/tool_disable_auto_output.png");
    // todo switch to tool_switch_converter_native once that gets made
    public static final SpriteTexture TOOL_SWITCH_CONVERTER_NATIVE = texture("extfrocore:textures/gui/overlay/tool_wire_block.png");
    // todo switch to tool_switch_converter_eu once that gets made
    public static final SpriteTexture TOOL_SWITCH_CONVERTER_EU = texture("extfrocore:textures/gui/overlay/tool_wire_connect.png");

    // BASE TEXTURES
    public static final SpriteTexture BACKGROUND = border("extfrocore:textures/gui/base/background.png", 4, 4);
    public static final SpriteTexture BACKGROUND_INVERSE = border("extfrocore:textures/gui/base/background_inverse.png", 4, 4);
    public static final SteamTexture BACKGROUND_STEAM = SteamTexture
            .fullImage("extfrocore:textures/gui/base/background_%s.png");
    public static final SpriteTexture CLIPBOARD_BACKGROUND = texture("extfrocore:textures/gui/base/clipboard_background.png");
    public static final SpriteTexture CLIPBOARD_PAPER_BACKGROUND = texture("extfrocore:textures/gui/base/clipboard_paper_background.png");
    public static final SpriteTexture TITLE_BAR_BACKGROUND = border("extfrocore:textures/gui/base/title_bar_background.png", 4, 4);

    public static final SpriteTexture DISPLAY = texture("extfrocore:textures/gui/base/display.png");
    public static final SteamTexture DISPLAY_STEAM = SteamTexture.fullImage("extfrocore:textures/gui/base/display_%s.png");
    public static final SpriteTexture BLANK = border("extfrocore:textures/gui/base/blank.png", 0, 0);
    public static final SpriteTexture BLANK_TRANSPARENT = border("extfrocore:textures/gui/base/blank_transparent.png", 0, 0);
    public static final SpriteTexture FLUID_SLOT = border("extfrocore:textures/gui/base/fluid_slot.png", 1, 1);
    public static final SpriteTexture FLUID_TANK_BACKGROUND = texture("extfrocore:textures/gui/base/fluid_tank_background.png");
    public static final SpriteTexture FLUID_TANK_OVERLAY = texture("extfrocore:textures/gui/base/fluid_tank_overlay.png");
    public static final SpriteTexture SLOT = border("extfrocore:textures/gui/base/slot.png", 1, 1);
    public static final SpriteTexture SLOT_DARK = border("extfrocore:textures/gui/base/slot_dark.png", 1, 1);

    public static final SpriteTexture SLOT_DARKENED = texture("extfrocore:textures/gui/base/darkened_slot.png");
    public static final SteamTexture SLOT_STEAM = SteamTexture.fullImage("extfrocore:textures/gui/base/slot_%s.png");
    public static final SpriteTexture TOGGLE_BUTTON_BACK = texture("extfrocore:textures/gui/widget/toggle_button_background.png");

    public static final SpriteTexture CLOSE_ICON = texture("extfrocore:textures/gui/icon/close.png");

    // FLUID & ITEM OUTPUT BUTTONS
    public static final SpriteTexture BLOCKS_INPUT = texture("extfrocore:textures/gui/widget/button_blocks_input.png");
    public static final SpriteTexture BUTTON = border("extfrocore:textures/gui/widget/button.png", 2, 2);
    public static final SpriteTexture BUTTON_ALLOW_IMPORT_EXPORT = texture("extfrocore:textures/gui/widget/button_allow_import_export.png");
    public static final SpriteTexture BUTTON_BLACKLIST = texture("extfrocore:textures/gui/widget/button_blacklist.png");
    public static final SpriteTexture BUTTON_CHUNK_MODE = texture("extfrocore:textures/gui/widget/button_chunk_mode.png");
    public static final SpriteTexture BUTTON_CLEAR_GRID = texture("extfrocore:textures/gui/widget/button_clear_grid.png");
    public static final SpriteTexture BUTTON_FILTER_DAMAGE = texture("extfrocore:textures/gui/widget/button_filter_damage.png");
    public static final SpriteTexture BUTTON_DISTINCT_BUSES = texture("extfrocore:textures/gui/widget/button_distinct_buses.png");
    public static final SpriteTexture BUTTON_POWER = texture("extfrocore:textures/gui/widget/button_power.png");
    public static final SpriteTexture BUTTON_BATCH = texture("extfrocore:textures/gui/widget/button_batch.png");
    public static final SpriteTexture BUTTON_FILTER_NBT = texture("extfrocore:textures/gui/widget/button_filter_nbt.png");
    public static final SpriteTexture BUTTON_FLUID_OUTPUT = texture("extfrocore:textures/gui/widget/button_fluid_output_overlay.png");
    public static final SpriteTexture BUTTON_ITEM_OUTPUT = texture("extfrocore:textures/gui/widget/button_item_output_overlay.png");
    public static final SpriteTexture BUTTON_LOCK = texture("extfrocore:textures/gui/widget/button_lock.png");
    public static final SpriteTexture BUTTON_VOID = texture("extfrocore:textures/gui/widget/button_void.png");
    public static final SpriteTexture BUTTON_VOID_PARTIAL = texture("extfrocore:textures/gui/widget/button_void_partial.png");
    public static final SpriteTexture BUTTON_VOID_MULTIBLOCK = texture("extfrocore:textures/gui/widget/button_void_multiblock.png");
    public static final SpriteTexture BUTTON_LEFT = texture("extfrocore:textures/gui/widget/left.png");
    public static final SpriteTexture BUTTON_PUBLIC_PRIVATE = texture("extfrocore:textures/gui/widget/button_public_private.png");
    public static final SpriteTexture BUTTON_CHECK = texture("extfrocore:textures/gui/widget/button_check.png");
    public static final SpriteTexture BUTTON_LIST = texture("extfrocore:textures/gui/widget/button_list.png");
    public static final SpriteTexture BUTTON_RIGHT = texture("extfrocore:textures/gui/widget/right.png");
    public static final SpriteTexture BUTTON_SILK_TOUCH_MODE = texture("extfrocore:textures/gui/widget/button_silk_touch_mode.png");
    public static final SpriteTexture BUTTON_SWITCH_VIEW = texture("extfrocore:textures/gui/widget/button_switch_view.png");
    public static final SpriteTexture BUTTON_WORKING_ENABLE = texture("extfrocore:textures/gui/widget/button_working_enable.png");
    public static final SpriteTexture BUTTON_INT_CIRCUIT_PLUS = texture("extfrocore:textures/gui/widget/button_circuit_plus.png");
    public static final SpriteTexture BUTTON_INT_CIRCUIT_MINUS = texture("extfrocore:textures/gui/widget/button_circuit_minus.png");
    public static final SpriteTexture CLIPBOARD_BUTTON = texture("extfrocore:textures/gui/widget/clipboard_button.png");
    public static final SpriteTexture CLIPBOARD_TEXT_BOX = border("extfrocore:textures/gui/widget/clipboard_text_box.png", 1, 1);
    public static final SpriteTexture DISTRIBUTION_MODE = texture("extfrocore:textures/gui/widget/button_distribution_mode.png");
    public static final SpriteTexture BUTTON_AUTO_PULL = texture("extfrocore:textures/gui/widget/button_me_auto_pull.png");
    public static final SpriteTexture LOCK = texture("extfrocore:textures/gui/widget/lock.png");
    public static final SpriteTexture LOCK_WHITE = texture("extfrocore:textures/gui/widget/lock_white.png");
    public static final SpriteTexture SWITCH = texture("extfrocore:textures/gui/widget/switch.png");
    public static final SpriteTexture SWITCH_HORIZONTAL = texture("extfrocore:textures/gui/widget/switch_horizontal.png");
    public static final SpriteTexture VANILLA_BUTTON = border("ldlib2:textures/gui/button_common.png", 1, 1);

    public static final SpriteTexture ENERGY_DETECTOR_COVER_MODE_BUTTON = texture("extfrocore:textures/gui/widget/button_detector_cover_energy_mode.png");
    public static final SpriteTexture INVERT_REDSTONE_BUTTON = texture("extfrocore:textures/gui/widget/button_detector_cover_inverted.png");

    public static final SpriteTexture IO_CONFIG_FLUID_MODES_BUTTON = texture("extfrocore:textures/gui/icon/io_config/output_config_fluid_modes.png");
    public static final SpriteTexture IO_CONFIG_ITEM_MODES_BUTTON = texture("extfrocore:textures/gui/icon/io_config/output_config_item_modes.png");
    public static final SpriteTexture IO_CONFIG_COVER_SLOT_OVERLAY = texture("extfrocore:textures/gui/icon/io_config/cover_slot_overlay.png");
    public static final SpriteTexture IO_CONFIG_COVER_SETTINGS = texture("extfrocore:textures/gui/icon/io_config/cover_settings.png");

    public static final SpriteTexture PATTERN_OVERLAY = texture("extfrocore:textures/gui/widget/pattern_overlay.png");
    public static final SpriteTexture REFUND_OVERLAY = texture("extfrocore:textures/gui/widget/refund_overlay.png");
    // INDICATORS & ICONS
    public static final SpriteTexture INDICATOR_NO_ENERGY = texture("extfrocore:textures/gui/base/indicator_no_energy.png");
    public static final SteamTexture INDICATOR_NO_STEAM = SteamTexture
            .fullImage("extfrocore:textures/gui/base/indicator_no_steam_%s.png");
    public static final SpriteTexture TANK_ICON = texture("extfrocore:textures/gui/base/tank_icon.png");

    // WIDGET UI RELATED
    public static final SpriteTexture SLIDER_BACKGROUND = texture("extfrocore:textures/gui/widget/slider_background.png");
    public static final SpriteTexture SLIDER_BACKGROUND_VERTICAL = texture("extfrocore:textures/gui/widget/slider_background_vertical.png");
    public static final SpriteTexture SLIDER_ICON = texture("extfrocore:textures/gui/widget/slider.png");
    public static final SpriteTexture MAINTENANCE_BUTTON = texture("extfrocore:textures/gui/widget/button_maintenance.png");
    public static final SpriteTexture MAINTENANCE_ICON = texture("extfrocore:textures/block/overlay/machine/overlay_maintenance.png");
    public static final SpriteTexture STORAGE_ICON = texture("extfrocore:textures/item/storage.png");
    public static final SpriteTexture BUTTON_MINER_MODES = texture("extfrocore:textures/gui/widget/button_miner_modes.png");

    // ORE PROCESSING
    public static final SpriteTexture OREBY_BASE = texture("extfrocore:textures/gui/arrows/oreby-base.png");
    public static final SpriteTexture OREBY_CHEM = texture("extfrocore:textures/gui/arrows/oreby-chem.png");
    public static final SpriteTexture OREBY_SEP = texture("extfrocore:textures/gui/arrows/oreby-sep.png");
    public static final SpriteTexture OREBY_SIFT = texture("extfrocore:textures/gui/arrows/oreby-sift.png");
    public static final SpriteTexture OREBY_SMELT = texture("extfrocore:textures/gui/arrows/oreby-smelt.png");

    // PRIMITIVE
    public static final SpriteTexture PRIMITIVE_BACKGROUND = border("extfrocore:textures/gui/primitive/primitive_background.png", 3, 3);
    public static final SpriteTexture PRIMITIVE_SLOT = border("extfrocore:textures/gui/primitive/primitive_slot.png", 1, 1);
    public static final SpriteTexture PRIMITIVE_FURNACE_OVERLAY = texture("extfrocore:textures/gui/primitive/overlay_primitive_furnace.png");
    public static final SpriteTexture PRIMITIVE_DUST_OVERLAY = texture("extfrocore:textures/gui/primitive/overlay_primitive_dust.png");
    public static final SpriteTexture PRIMITIVE_INGOT_OVERLAY = texture("extfrocore:textures/gui/primitive/overlay_primitive_ingot.png");
    public static final SpriteTexture PRIMITIVE_LARGE_FLUID_TANK = texture("extfrocore:textures/gui/primitive/primitive_large_fluid_tank.png");
    public static final SpriteTexture PRIMITIVE_LARGE_FLUID_TANK_OVERLAY = texture("extfrocore:textures/gui/primitive/primitive_large_fluid_tank_overlay.png");
    public static final SpriteTexture PRIMITIVE_BLAST_FURNACE_PROGRESS_BAR = texture("extfrocore:textures/gui/primitive/progress_bar_primitive_blast_furnace.png");

    // SLOT OVERLAYS
    public static final SpriteTexture ATOMIC_OVERLAY_1 = texture("extfrocore:textures/gui/overlay/atomic_overlay_1.png");
    public static final SpriteTexture ATOMIC_OVERLAY_2 = texture("extfrocore:textures/gui/overlay/atomic_overlay_2.png");
    public static final SpriteTexture ARROW_INPUT_OVERLAY = texture("extfrocore:textures/gui/overlay/arrow_input_overlay.png");
    public static final SpriteTexture ARROW_OUTPUT_OVERLAY = texture("extfrocore:textures/gui/overlay/arrow_output_overlay.png");
    public static final SpriteTexture BATTERY_OVERLAY = texture("extfrocore:textures/gui/overlay/battery_overlay.png");
    public static final SpriteTexture BEAKER_OVERLAY_1 = texture("extfrocore:textures/gui/overlay/beaker_overlay_1.png");
    public static final SpriteTexture BEAKER_OVERLAY_2 = texture("extfrocore:textures/gui/overlay/beaker_overlay_2.png");
    public static final SpriteTexture BEAKER_OVERLAY_3 = texture("extfrocore:textures/gui/overlay/beaker_overlay_3.png");
    public static final SpriteTexture BEAKER_OVERLAY_4 = texture("extfrocore:textures/gui/overlay/beaker_overlay_4.png");
    public static final SpriteTexture BENDER_OVERLAY = texture("extfrocore:textures/gui/overlay/bender_overlay.png");
    public static final SpriteTexture BOX_OVERLAY = texture("extfrocore:textures/gui/overlay/box_overlay.png");
    public static final SpriteTexture BOXED_OVERLAY = texture("extfrocore:textures/gui/overlay/boxed_overlay.png");
    public static final SpriteTexture BREWER_OVERLAY = texture("extfrocore:textures/gui/overlay/brewer_overlay.png");
    public static final SpriteTexture CANNER_OVERLAY = texture("extfrocore:textures/gui/overlay/canner_overlay.png");
    public static final SpriteTexture CHARGER_OVERLAY = texture("extfrocore:textures/gui/overlay/charger_slot_overlay.png");
    public static final SpriteTexture CANISTER_OVERLAY = texture("extfrocore:textures/gui/overlay/canister_overlay.png");
    public static final SteamTexture CANISTER_OVERLAY_STEAM = SteamTexture
            .fullImage("extfrocore:textures/gui/overlay/canister_overlay_%s.png");
    public static final SpriteTexture CENTRIFUGE_OVERLAY = texture("extfrocore:textures/gui/overlay/centrifuge_overlay.png");
    public static final SpriteTexture CIRCUIT_OVERLAY = texture("extfrocore:textures/gui/overlay/circuit_overlay.png");
    public static final SteamTexture COAL_OVERLAY_STEAM = SteamTexture
            .fullImage("extfrocore:textures/gui/overlay/coal_overlay_%s.png");
    public static final SpriteTexture COMPRESSOR_OVERLAY = texture("extfrocore:textures/gui/overlay/compressor_overlay.png");
    public static final SteamTexture COMPRESSOR_OVERLAY_STEAM = SteamTexture
            .fullImage("extfrocore:textures/gui/overlay/compressor_overlay_%s.png");
    public static final SpriteTexture CRACKING_OVERLAY_1 = texture("extfrocore:textures/gui/overlay/cracking_overlay_1.png");
    public static final SpriteTexture CRACKING_OVERLAY_2 = texture("extfrocore:textures/gui/overlay/cracking_overlay_2.png");
    public static final SpriteTexture CRUSHED_ORE_OVERLAY = texture("extfrocore:textures/gui/overlay/crushed_ore_overlay.png");
    public static final SteamTexture CRUSHED_ORE_OVERLAY_STEAM = SteamTexture
            .fullImage("extfrocore:textures/gui/overlay/crushed_ore_overlay_%s.png");
    public static final SpriteTexture CRYSTAL_OVERLAY = texture("extfrocore:textures/gui/overlay/crystal_overlay.png");
    public static final SpriteTexture CUTTER_OVERLAY = texture("extfrocore:textures/gui/overlay/cutter_overlay.png");
    public static final SpriteTexture DARK_CANISTER_OVERLAY = texture("extfrocore:textures/gui/overlay/dark_canister_overlay.png");
    public static final SpriteTexture DUST_OVERLAY = texture("extfrocore:textures/gui/overlay/dust_overlay.png");
    public static final SteamTexture DUST_OVERLAY_STEAM = SteamTexture
            .fullImage("extfrocore:textures/gui/overlay/dust_overlay_%s.png");
    public static final SpriteTexture EXTRACTOR_OVERLAY = texture("extfrocore:textures/gui/overlay/extractor_overlay.png");
    public static final SteamTexture EXTRACTOR_OVERLAY_STEAM = SteamTexture
            .fullImage("extfrocore:textures/gui/overlay/extractor_overlay_%s.png");
    public static final SpriteTexture FILTER_SLOT_OVERLAY = texture("extfrocore:textures/gui/overlay/filter_slot_overlay.png");
    public static final SpriteTexture FURNACE_OVERLAY_1 = texture("extfrocore:textures/gui/overlay/furnace_overlay_1.png");
    public static final SpriteTexture FURNACE_OVERLAY_2 = texture("extfrocore:textures/gui/overlay/furnace_overlay_2.png");
    public static final SteamTexture FURNACE_OVERLAY_STEAM = SteamTexture
            .fullImage("extfrocore:textures/gui/overlay/furnace_overlay_%s.png");
    public static final SpriteTexture HAMMER_OVERLAY = texture("extfrocore:textures/gui/overlay/hammer_overlay.png");
    public static final SteamTexture HAMMER_OVERLAY_STEAM = SteamTexture
            .fullImage("extfrocore:textures/gui/overlay/hammer_overlay_%s.png");
    public static final SpriteTexture HEATING_OVERLAY_1 = texture("extfrocore:textures/gui/overlay/heating_overlay_1.png");
    public static final SpriteTexture HEATING_OVERLAY_2 = texture("extfrocore:textures/gui/overlay/heating_overlay_2.png");
    public static final SpriteTexture IMPLOSION_OVERLAY_1 = texture("extfrocore:textures/gui/overlay/implosion_overlay_1.png");
    public static final SpriteTexture IMPLOSION_OVERLAY_2 = texture("extfrocore:textures/gui/overlay/implosion_overlay_2.png");
    public static final SpriteTexture IN_SLOT_OVERLAY = texture("extfrocore:textures/gui/overlay/in_slot_overlay.png");
    public static final SteamTexture IN_SLOT_OVERLAY_STEAM = SteamTexture
            .fullImage("extfrocore:textures/gui/overlay/in_slot_overlay_%s.png");
    public static final SpriteTexture INGOT_OVERLAY = texture("extfrocore:textures/gui/overlay/ingot_overlay.png");
    public static final SpriteTexture INT_CIRCUIT_OVERLAY = texture("extfrocore:textures/gui/overlay/int_circuit_overlay.png");
    public static final SpriteTexture LENS_OVERLAY = texture("extfrocore:textures/gui/overlay/lens_overlay.png");
    public static final SpriteTexture LIGHTNING_OVERLAY_1 = texture("extfrocore:textures/gui/overlay/lightning_overlay_1.png");
    public static final SpriteTexture LIGHTNING_OVERLAY_2 = texture("extfrocore:textures/gui/overlay/lightning_overlay_2.png");
    public static final SpriteTexture MOLD_OVERLAY = texture("extfrocore:textures/gui/overlay/mold_overlay.png");
    public static final SpriteTexture MOLECULAR_OVERLAY_1 = texture("extfrocore:textures/gui/overlay/molecular_overlay_1.png");
    public static final SpriteTexture MOLECULAR_OVERLAY_2 = texture("extfrocore:textures/gui/overlay/molecular_overlay_2.png");
    public static final SpriteTexture MOLECULAR_OVERLAY_3 = texture("extfrocore:textures/gui/overlay/molecular_overlay_3.png");
    public static final SpriteTexture MOLECULAR_OVERLAY_4 = texture("extfrocore:textures/gui/overlay/molecular_overlay_4.png");
    public static final SpriteTexture OUT_SLOT_OVERLAY = texture("extfrocore:textures/gui/overlay/out_slot_overlay.png");
    public static final SteamTexture OUT_SLOT_OVERLAY_STEAM = SteamTexture
            .fullImage("extfrocore:textures/gui/overlay/out_slot_overlay_%s.png");
    public static final SpriteTexture PAPER_OVERLAY = texture("extfrocore:textures/gui/overlay/paper_overlay.png");
    public static final SpriteTexture PRINTED_PAPER_OVERLAY = texture("extfrocore:textures/gui/overlay/printed_paper_overlay.png");
    public static final SpriteTexture PIPE_OVERLAY_2 = texture("extfrocore:textures/gui/overlay/pipe_overlay_2.png");
    public static final SpriteTexture PIPE_OVERLAY_1 = texture("extfrocore:textures/gui/overlay/pipe_overlay_1.png");
    public static final SpriteTexture PRESS_OVERLAY_1 = texture("extfrocore:textures/gui/overlay/press_overlay_1.png");
    public static final SpriteTexture PRESS_OVERLAY_2 = texture("extfrocore:textures/gui/overlay/press_overlay_2.png");
    public static final SpriteTexture PRESS_OVERLAY_3 = texture("extfrocore:textures/gui/overlay/press_overlay_3.png");
    public static final SpriteTexture PRESS_OVERLAY_4 = texture("extfrocore:textures/gui/overlay/press_overlay_4.png");
    public static final SpriteTexture SAWBLADE_OVERLAY = texture("extfrocore:textures/gui/overlay/sawblade_overlay.png");
    public static final SpriteTexture SOLIDIFIER_OVERLAY = texture("extfrocore:textures/gui/overlay/solidifier_overlay.png");
    public static final SpriteTexture STRING_SLOT_OVERLAY = texture("extfrocore:textures/gui/overlay/string_slot_overlay.png");
    public static final SpriteTexture TOOL_SLOT_OVERLAY = texture("extfrocore:textures/gui/overlay/tool_slot_overlay.png");
    public static final SpriteTexture TURBINE_OVERLAY = texture("extfrocore:textures/gui/overlay/turbine_overlay.png");
    public static final SpriteTexture VIAL_OVERLAY_1 = texture("extfrocore:textures/gui/overlay/vial_overlay_1.png");
    public static final SpriteTexture VIAL_OVERLAY_2 = texture("extfrocore:textures/gui/overlay/vial_overlay_2.png");
    public static final SpriteTexture WIREMILL_OVERLAY = texture("extfrocore:textures/gui/overlay/wiremill_overlay.png");
    public static final SpriteTexture POSITIVE_MATTER_OVERLAY = texture("extfrocore:textures/gui/overlay/positive_matter_overlay.png");
    public static final SpriteTexture NEUTRAL_MATTER_OVERLAY = texture("extfrocore:textures/gui/overlay/neutral_matter_overlay.png");
    public static final SpriteTexture DATA_ORB_OVERLAY = texture("extfrocore:textures/gui/overlay/data_orb_overlay.png");
    public static final SpriteTexture SCANNER_OVERLAY = texture("extfrocore:textures/gui/overlay/scanner_overlay.png");
    public static final SpriteTexture DUCT_TAPE_OVERLAY = texture("extfrocore:textures/gui/overlay/duct_tape_overlay.png");
    public static final SpriteTexture RESEARCH_STATION_OVERLAY = texture("extfrocore:textures/gui/overlay/research_station_overlay.png");

    // PROGRESS BARS
    public static final SpriteTexture PROGRESS_BAR_ARC_FURNACE = texture("extfrocore:textures/gui/progress_bar/progress_bar_arc_furnace.png");
    public static final SpriteTexture PROGRESS_BAR_ARROW = texture("extfrocore:textures/gui/progress_bar/progress_bar_arrow.png");
    public static final SteamTexture PROGRESS_BAR_ARROW_STEAM = SteamTexture
            .fullImage("extfrocore:textures/gui/progress_bar/progress_bar_arrow_%s.png");
    public static final SpriteTexture PROGRESS_BAR_ARROW_MULTIPLE = texture("extfrocore:textures/gui/progress_bar/progress_bar_arrow_multiple.png");
    public static final SpriteTexture PROGRESS_BAR_ASSEMBLER = texture("extfrocore:textures/gui/progress_bar/progress_bar_assembler.png");

    public static final SpriteTexture PROGRESS_BAR_ASSEMBLY_LINE = texture("extfrocore:textures/gui/progress_bar/progress_bar_assembly_line.png");
    public static final SpriteTexture PROGRESS_BAR_ASSEMBLY_LINE_ARROW = texture("extfrocore:textures/gui/progress_bar/progress_bar_assembly_line_arrow.png");
    public static final SpriteTexture PROGRESS_BAR_BATH = texture("extfrocore:textures/gui/progress_bar/progress_bar_bath.png");
    public static final SpriteTexture PROGRESS_BAR_BENDING = texture("extfrocore:textures/gui/progress_bar/progress_bar_bending.png");
    public static final SteamTexture PROGRESS_BAR_BOILER_EMPTY = SteamTexture
            .fullImage("extfrocore:textures/gui/progress_bar/progress_bar_boiler_empty_%s.png");
    public static final SteamTexture PROGRESS_BAR_BOILER_FUEL = SteamTexture
            .fullImage("extfrocore:textures/gui/progress_bar/progress_bar_boiler_fuel_%s.png");
    public static final SpriteTexture PROGRESS_BAR_BOILER_HEAT = texture("extfrocore:textures/gui/progress_bar/progress_bar_boiler_heat.png");
    public static final SpriteTexture PROGRESS_BAR_CANNER = texture("extfrocore:textures/gui/progress_bar/progress_bar_canner.png");
    public static final SpriteTexture PROGRESS_BAR_CIRCUIT = texture("extfrocore:textures/gui/progress_bar/progress_bar_circuit_assembler.png");
    public static final SpriteTexture PROGRESS_BAR_CIRCUIT_ASSEMBLER = texture("extfrocore:textures/gui/progress_bar/progress_bar_circuit_assembler.png");
    public static final SpriteTexture PROGRESS_BAR_COKE_OVEN = texture("extfrocore:textures/gui/progress_bar/progress_bar_coke_oven.png");
    public static final SpriteTexture PROGRESS_BAR_COMPRESS = texture("extfrocore:textures/gui/progress_bar/progress_bar_compress.png");
    public static final SteamTexture PROGRESS_BAR_COMPRESS_STEAM = SteamTexture
            .fullImage("extfrocore:textures/gui/progress_bar/progress_bar_compress_%s.png");
    public static final SpriteTexture PROGRESS_BAR_CRACKING = texture("extfrocore:textures/gui/progress_bar/progress_bar_cracking.png");
    public static final SpriteTexture PROGRESS_BAR_CRACKING_INPUT = texture("extfrocore:textures/gui/progress_bar/progress_bar_cracking_2.png");
    public static final SpriteTexture PROGRESS_BAR_CRYSTALLIZATION = texture("extfrocore:textures/gui/progress_bar/progress_bar_crystallization.png");
    public static final SpriteTexture PROGRESS_BAR_DISTILLATION_TOWER = texture("extfrocore:textures/gui/progress_bar/progress_bar_distillation_tower.png");
    public static final SpriteTexture PROGRESS_BAR_EXTRACT = texture("extfrocore:textures/gui/progress_bar/progress_bar_extract.png");
    public static final SteamTexture PROGRESS_BAR_EXTRACT_STEAM = SteamTexture
            .fullImage("extfrocore:textures/gui/progress_bar/progress_bar_extract_%s.png");
    public static final SpriteTexture PROGRESS_BAR_EXTRUDER = texture("extfrocore:textures/gui/progress_bar/progress_bar_extruder.png");
    public static final SpriteTexture PROGRESS_BAR_FUSION = texture("extfrocore:textures/gui/progress_bar/progress_bar_fusion.png");
    public static final SpriteTexture PROGRESS_BAR_GAS_COLLECTOR = texture("extfrocore:textures/gui/progress_bar/progress_bar_gas_collector.png");
    public static final SpriteTexture PROGRESS_BAR_HAMMER = texture("extfrocore:textures/gui/progress_bar/progress_bar_hammer.png");
    public static final SteamTexture PROGRESS_BAR_HAMMER_STEAM = SteamTexture
            .fullImage("extfrocore:textures/gui/progress_bar/progress_bar_hammer_%s.png");
    public static final SpriteTexture PROGRESS_BAR_HAMMER_BASE = texture("extfrocore:textures/gui/progress_bar/progress_bar_hammer_base.png");
    public static final SteamTexture PROGRESS_BAR_HAMMER_BASE_STEAM = SteamTexture
            .fullImage("extfrocore:textures/gui/progress_bar/progress_bar_hammer_base_%s.png");
    public static final SpriteTexture PROGRESS_BAR_LATHE = texture("extfrocore:textures/gui/progress_bar/progress_bar_lathe.png");
    public static final SpriteTexture PROGRESS_BAR_LATHE_BASE = texture("extfrocore:textures/gui/progress_bar/progress_bar_lathe_base.png");
    public static final SpriteTexture PROGRESS_BAR_MACERATE = texture("extfrocore:textures/gui/progress_bar/progress_bar_macerate.png");
    public static final SteamTexture PROGRESS_BAR_MACERATE_STEAM = SteamTexture
            .fullImage("extfrocore:textures/gui/progress_bar/progress_bar_macerate_%s.png");
    public static final SpriteTexture PROGRESS_BAR_MAGNET = texture("extfrocore:textures/gui/progress_bar/progress_bar_magnet.png");
    public static final SpriteTexture PROGRESS_BAR_MASS_FAB = texture("extfrocore:textures/gui/progress_bar/progress_bar_mass_fab.png");
    public static final SpriteTexture PROGRESS_BAR_MIXER = texture("extfrocore:textures/gui/progress_bar/progress_bar_mixer.png");
    public static final SpriteTexture PROGRESS_BAR_PACKER = texture("extfrocore:textures/gui/progress_bar/progress_bar_packer.png");
    public static final SpriteTexture PROGRESS_BAR_RECYCLER = texture("extfrocore:textures/gui/progress_bar/progress_bar_recycler.png");
    public static final SpriteTexture PROGRESS_BAR_REPLICATOR = texture("extfrocore:textures/gui/progress_bar/progress_bar_replicator.png");
    public static final SpriteTexture PROGRESS_BAR_SIFT = texture("extfrocore:textures/gui/progress_bar/progress_bar_sift.png");
    public static final SpriteTexture PROGRESS_BAR_SLICE = texture("extfrocore:textures/gui/progress_bar/progress_bar_slice.png");
    public static final SteamTexture PROGRESS_BAR_SOLAR_STEAM = SteamTexture
            .fullImage("extfrocore:textures/gui/progress_bar/progress_bar_solar_%s.png");
    public static final SpriteTexture PROGRESS_BAR_UNLOCK = texture("extfrocore:textures/gui/progress_bar/progress_bar_unlock.png");
    public static final SpriteTexture PROGRESS_BAR_UNPACKER = texture("extfrocore:textures/gui/progress_bar/progress_bar_unpacker.png");
    public static final SpriteTexture PROGRESS_BAR_WIREMILL = texture("extfrocore:textures/gui/progress_bar/progress_bar_wiremill.png");
    public static final SpriteTexture PROGRESS_BAR_RESEARCH_STATION_1 = texture("extfrocore:textures/gui/progress_bar/progress_bar_research_station_1.png");
    public static final SpriteTexture PROGRESS_BAR_RESEARCH_STATION_2 = texture("extfrocore:textures/gui/progress_bar/progress_bar_research_station_2.png");
    public static final SpriteTexture PROGRESS_BAR_RESEARCH_STATION_BASE = texture("extfrocore:textures/gui/progress_bar/progress_bar_research_station_base.png");

    // JEI
    public static final SpriteTexture INFO_ICON = texture("extfrocore:textures/gui/widget/information.png");
    public static final SpriteTexture MULTIBLOCK_CATEGORY = texture("extfrocore:textures/gui/icon/coke_oven.png");

    public static final SpriteTexture ARC_FURNACE_RECYCLING_CATEGORY = texture("extfrocore:textures/gui/icon/category/arc_furnace_recycling.png");
    public static final SpriteTexture MACERATOR_RECYCLING_CATEGORY = texture("extfrocore:textures/gui/icon/category/macerator_recycling.png");
    public static final SpriteTexture EXTRACTOR_RECYCLING_CATEGORY = texture("extfrocore:textures/gui/icon/category/extractor_recycling.png");

    // Covers
    public static final SpriteTexture COVER_MACHINE_CONTROLLER = texture("extfrocore:textures/items/machine_controller_cover.png");

    // Terminal
    public static final SpriteTexture ICON_REMOVE = texture("extfrocore:textures/gui/terminal/icon/remove_hover.png");
    public static final SpriteTexture ICON_UP = texture("extfrocore:textures/gui/terminal/icon/up_hover.png");
    public static final SpriteTexture ICON_DOWN = texture("extfrocore:textures/gui/terminal/icon/down_hover.png");
    public static final SpriteTexture ICON_RIGHT = texture("extfrocore:textures/gui/terminal/icon/right_hover.png");
    public static final SpriteTexture ICON_LEFT = texture("extfrocore:textures/gui/terminal/icon/left_hover.png");
    public static final SpriteTexture ICON_ADD = texture("extfrocore:textures/gui/terminal/icon/add_hover.png");

    public static final SpriteTexture ICON_NEW_PAGE = texture("extfrocore:textures/gui/terminal/icon/system/memory_card_hover.png");
    public static final SpriteTexture ICON_LOAD = texture("extfrocore:textures/gui/terminal/icon/folder_hover.png");
    public static final SpriteTexture ICON_SAVE = texture("extfrocore:textures/gui/terminal/icon/system/save_hover.png");
    public static final SpriteTexture ICON_LOCATION = texture("extfrocore:textures/gui/terminal/icon/guide_hover.png");
    public static final SpriteTexture ICON_VISIBLE = texture("extfrocore:textures/gui/terminal/icon/appearance_hover.png");
    public static final SpriteTexture ICON_CALCULATOR = texture("extfrocore:textures/gui/terminal/icon/calculator_hover.png");
    public static final SpriteTexture UI_FRAME_SIDE_UP = texture("extfrocore:textures/gui/terminal/frame_side_up.png");
    public static final SpriteTexture UI_FRAME_SIDE_DOWN = texture("extfrocore:textures/gui/terminal/frame_side_down.png");

    // Texture Areas
    public static final SpriteTexture BUTTON_FLUID = texture("extfrocore:textures/block/cover/cover_interface_fluid_button.png");
    public static final SpriteTexture BUTTON_ITEM = texture("extfrocore:textures/block/cover/cover_interface_item_button.png");
    public static final SpriteTexture BUTTON_ENERGY = texture("extfrocore:textures/block/cover/cover_interface_energy_button.png");
    public static final SpriteTexture BUTTON_MACHINE = texture("extfrocore:textures/block/cover/cover_interface_machine_button.png");
    public static final SpriteTexture BUTTON_INTERFACE = texture("extfrocore:textures/block/cover/cover_interface_computer_button.png");
    public static final SpriteTexture COVER_INTERFACE_MACHINE_ON_PROXY = texture("extfrocore:textures/block/cover/cover_interface_machine_on_proxy.png");
    public static final SpriteTexture COVER_INTERFACE_MACHINE_OFF_PROXY = texture("extfrocore:textures/blocks/cover/cover_interface_machine_off_proxy.png");
    public static final SpriteTexture SCENE = texture("extfrocore:textures/gui/widget/scene.png");
    public static final SpriteTexture DISPLAY_FRAME = border("extfrocore:textures/gui/base/display_frame.png", 4, 4);
    public static final SpriteTexture INSUFFICIENT_INPUT = texture("extfrocore:textures/gui/base/indicator_no_energy.png");
    public static final SpriteTexture ENERGY_BAR_BACKGROUND = border("extfrocore:textures/gui/progress_bar/progress_bar_boiler_empty_steel.png", 1, 1);
    public static final SpriteTexture ENERGY_BAR_BASE = border("extfrocore:textures/gui/progress_bar/progress_bar_boiler_heat.png", 1, 1);
    public static final SpriteTexture LIGHT_ON = texture("extfrocore:textures/gui/widget/light_on.png");
    public static final SpriteTexture LIGHT_OFF = texture("extfrocore:textures/gui/widget/light_off.png");
    public static final SpriteTexture UP = texture("extfrocore:textures/gui/base/up.png");
    public static final IGuiTexture[] TIER = new IGuiTexture[9];
    static {
        var offset = 1f / TIER.length;
        for (int i = 0; i < TIER.length; i++) {
            TIER[i] = CroppedTexture.of("extfrocore:textures/gui/overlay/tier.png", 0, i * offset, 1, offset);
        }
    }

    // Lamp item overlay
    public static final SpriteTexture LAMP_NO_BLOOM = texture("extfrocore:textures/gui/item_overlay/lamp_no_bloom.png");
    public static final SpriteTexture LAMP_NO_LIGHT = texture("extfrocore:textures/gui/item_overlay/lamp_no_light.png");

    // ME hatch/bus
    public static final SpriteTexture NUMBER_BACKGROUND = texture("extfrocore:textures/gui/widget/number_background.png");
    public static final SpriteTexture CONFIG_ARROW = texture("extfrocore:textures/gui/widget/config_arrow.png");
    public static final SpriteTexture CONFIG_ARROW_DARK = texture("extfrocore:textures/gui/widget/config_arrow_dark.png");
    public static final SpriteTexture SELECT_BOX = texture("extfrocore:textures/gui/widget/select_box.png");

    // HPCA Component icons
    public static final SpriteTexture HPCA_COMPONENT_OUTLINE = texture("extfrocore:textures/gui/widget/hpca/component_outline.png");
    public static final SpriteTexture HPCA_ICON_EMPTY_COMPONENT = texture("extfrocore:textures/gui/widget/hpca/empty_component.png");
    public static final SpriteTexture HPCA_ICON_ADVANCED_COMPUTATION_COMPONENT = texture("extfrocore:textures/gui/widget/hpca/advanced_computation_component.png");
    public static final SpriteTexture HPCA_ICON_BRIDGE_COMPONENT = texture("extfrocore:textures/gui/widget/hpca/bridge_component.png");
    public static final SpriteTexture HPCA_ICON_COMPUTATION_COMPONENT = texture("extfrocore:textures/gui/widget/hpca/computation_component.png");
    public static final SpriteTexture HPCA_ICON_ACTIVE_COOLER_COMPONENT = texture("extfrocore:textures/gui/widget/hpca/active_cooler_component.png");
    public static final SpriteTexture HPCA_ICON_HEAT_SINK_COMPONENT = texture("extfrocore:textures/gui/widget/hpca/heat_sink_component.png");
    public static final SpriteTexture HPCA_ICON_DAMAGED_ADVANCED_COMPUTATION_COMPONENT = texture("extfrocore:textures/gui/widget/hpca/damaged_advanced_computation_component.png");
    public static final SpriteTexture HPCA_ICON_DAMAGED_COMPUTATION_COMPONENT = texture("extfrocore:textures/gui/widget/hpca/damaged_computation_component.png");
}
