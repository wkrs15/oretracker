package com.wkrs14.oretracker.client;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Properties;
import java.util.Set;

/**
 * Small client-only settings store. Keeping this independent from Forge config
 * makes it safe to change values from the custom screen while the game is running.
 */
public final class OreTrackerConfig {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Path FILE = FMLPaths.CONFIGDIR.get().resolve("oretracker-client.properties");

    private static final boolean DEFAULT_ENABLED = false;
    private static final int DEFAULT_SCAN_RADIUS = 32;
    private static final int DEFAULT_VERTICAL_RADIUS = 20;
    private static final int DEFAULT_LINE_RANGE = 48;
    private static final boolean DEFAULT_SHOW_LINES = true;

    private static boolean enabled = DEFAULT_ENABLED;
    private static int scanRadius = DEFAULT_SCAN_RADIUS;
    private static int verticalRadius = DEFAULT_VERTICAL_RADIUS;
    private static int lineRange = DEFAULT_LINE_RANGE;
    private static boolean showLines = DEFAULT_SHOW_LINES;
    private static final Set<OreType> ENABLED_ORES = EnumSet.allOf(OreType.class);

    private OreTrackerConfig() {
    }

    public static void load() {
        Properties properties = new Properties();
        if (Files.isRegularFile(FILE)) {
            try (InputStream input = Files.newInputStream(FILE)) {
                properties.load(input);
            } catch (IOException exception) {
                LOGGER.warn("Unable to read {}", FILE, exception);
            }
        }

        enabled = Boolean.parseBoolean(properties.getProperty("enabled", Boolean.toString(DEFAULT_ENABLED)));
        scanRadius = clamp(parseInt(properties, "scanRadius", DEFAULT_SCAN_RADIUS), 8, 64);
        verticalRadius = clamp(parseInt(properties, "verticalRadius", DEFAULT_VERTICAL_RADIUS), 4, 48);
        lineRange = clamp(parseInt(properties, "lineRange", parseInt(properties, "rayRange", DEFAULT_LINE_RANGE)), 8, 96);
        showLines = Boolean.parseBoolean(properties.getProperty("showLines", Boolean.toString(DEFAULT_SHOW_LINES)));

        ENABLED_ORES.clear();
        for (OreType type : OreType.values()) {
            String value = properties.getProperty("ore." + type.id(), "true");
            if (Boolean.parseBoolean(value)) {
                ENABLED_ORES.add(type);
            }
        }

        save();
    }

    public static void save() {
        Properties properties = new Properties();
        properties.setProperty("enabled", Boolean.toString(enabled));
        properties.setProperty("scanRadius", Integer.toString(scanRadius));
        properties.setProperty("verticalRadius", Integer.toString(verticalRadius));
        properties.setProperty("lineRange", Integer.toString(lineRange));
        properties.setProperty("showLines", Boolean.toString(showLines));
        for (OreType type : OreType.values()) {
            properties.setProperty("ore." + type.id(), Boolean.toString(ENABLED_ORES.contains(type)));
        }

        try {
            Files.createDirectories(FILE.getParent());
            try (OutputStream output = Files.newOutputStream(FILE)) {
                properties.store(output, "OreTracker client settings");
            }
        } catch (IOException exception) {
            LOGGER.warn("Unable to save {}", FILE, exception);
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    public static boolean toggleEnabled() {
        enabled = !enabled;
        save();
        return enabled;
    }

    public static int getScanRadius() {
        return scanRadius;
    }

    public static void setScanRadius(int value) {
        scanRadius = clamp(value, 8, 64);
    }

    public static int getVerticalRadius() {
        return verticalRadius;
    }

    public static void setVerticalRadius(int value) {
        verticalRadius = clamp(value, 4, 48);
    }

    public static int getLineRange() {
        return lineRange;
    }

    public static void setLineRange(int value) {
        lineRange = clamp(value, 8, 96);
    }

    public static boolean showLines() {
        return showLines;
    }

    public static void setShowLines(boolean value) {
        showLines = value;
    }

    public static boolean toggleShowLines() {
        showLines = !showLines;
        return showLines;
    }

    public static boolean isOreEnabled(OreType type) {
        return ENABLED_ORES.contains(type);
    }

    public static void setOreEnabled(OreType type, boolean value) {
        if (value) {
            ENABLED_ORES.add(type);
        } else {
            ENABLED_ORES.remove(type);
        }
    }

    public static void setAllOres(boolean value) {
        ENABLED_ORES.clear();
        if (value) {
            ENABLED_ORES.addAll(EnumSet.allOf(OreType.class));
        }
    }

    public static void resetDefaults() {
        enabled = DEFAULT_ENABLED;
        scanRadius = DEFAULT_SCAN_RADIUS;
        verticalRadius = DEFAULT_VERTICAL_RADIUS;
        lineRange = DEFAULT_LINE_RANGE;
        showLines = DEFAULT_SHOW_LINES;
        setAllOres(true);
    }

    private static int parseInt(Properties properties, String key, int fallback) {
        try {
            return Integer.parseInt(properties.getProperty(key, Integer.toString(fallback)).trim());
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
