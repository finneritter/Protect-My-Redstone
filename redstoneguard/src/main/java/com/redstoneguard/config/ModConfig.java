package com.redstoneguard.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redstoneguard.RedstoneguardMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("redstoneguard.json");

    private static ModConfig INSTANCE = new ModConfig();

    // --- Config fields (public for Cloth Config access) ---

    /** Whether the mod warning is active. */
    public boolean enabled = true;

    /**
     * Minimum number of active redstone indicators that must be detected before
     * the logout warning is shown.
     */
    public int threshold = 1;

    /**
     * Seconds the player must wait on the warning screen before the "Leave" button unlocks.
     */
    public int countdownSeconds = 5;

    // --- Component toggles ---

    /** Count mid-motion pistons (moving piston block entities). */
    public boolean countPistons = true;

    /** Count powered comparators (output signal > 0). */
    public boolean countComparators = true;

    /** Count powered repeaters. */
    public boolean countRepeaters = true;

    /** Count powered observers. */
    public boolean countObservers = true;

    /** Count powered rails (active booster rails). */
    public boolean countPoweredRails = true;

    /** Count detector rails with a minecart on them. */
    public boolean countDetectorRails = true;

    /** Count sculk sensors and calibrated sculk sensors that are active or cooling down. */
    public boolean countSculkSensors = true;

    // --- Singleton access ---

    public static ModConfig getInstance() {
        return INSTANCE;
    }

    // --- Persistence ---

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                INSTANCE = GSON.fromJson(reader, ModConfig.class);
                // Clamp values to sane ranges after loading
                INSTANCE.threshold = Math.max(1, INSTANCE.threshold);
                INSTANCE.countdownSeconds = Math.max(0, Math.min(60, INSTANCE.countdownSeconds));
            } catch (IOException e) {
                RedstoneguardMod.LOGGER.error("Failed to load config, using defaults", e);
                INSTANCE = new ModConfig();
            }
        } else {
            save();
        }
    }

    public static void save() {
        try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(INSTANCE, writer);
        } catch (IOException e) {
            RedstoneguardMod.LOGGER.error("Failed to save config", e);
        }
    }
}
