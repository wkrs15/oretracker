package com.wkrs14.oretracker.client;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.IdentityHashMap;
import java.util.Map;

public enum OreType {
    COAL("coal", 0.58F, 0.58F, 0.58F, BlockTags.COAL_ORES),
    IRON("iron", 0.92F, 0.67F, 0.48F, BlockTags.IRON_ORES),
    COPPER("copper", 1.00F, 0.47F, 0.22F, BlockTags.COPPER_ORES),
    GOLD("gold", 1.00F, 0.84F, 0.18F, BlockTags.GOLD_ORES),
    REDSTONE("redstone", 1.00F, 0.18F, 0.12F, BlockTags.REDSTONE_ORES),
    LAPIS("lapis", 0.25F, 0.44F, 1.00F, BlockTags.LAPIS_ORES),
    DIAMOND("diamond", 0.10F, 0.94F, 1.00F, BlockTags.DIAMOND_ORES),
    EMERALD("emerald", 0.12F, 1.00F, 0.38F, BlockTags.EMERALD_ORES),
    QUARTZ("quartz", 0.96F, 0.90F, 0.80F, null),
    ANCIENT_DEBRIS("ancient_debris", 0.68F, 0.31F, 0.17F, null);

    private static final Map<Block, OreType> VANILLA_BLOCKS = createVanillaMap();

    private final String id;
    private final float red;
    private final float green;
    private final float blue;
    private final TagKey<Block> tag;

    OreType(String id, float red, float green, float blue, TagKey<Block> tag) {
        this.id = id;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.tag = tag;
    }

    public String id() {
        return id;
    }

    public String translationKey() {
        return "ore.oretracker." + id;
    }

    public float red() {
        return red;
    }

    public float green() {
        return green;
    }

    public float blue() {
        return blue;
    }

    /**
     * Returns the display type only when the ore is enabled in the menu.
     */
    public static OreType classify(BlockState state) {
        OreType direct = VANILLA_BLOCKS.get(state.getBlock());
        if (direct != null) {
            return OreTrackerConfig.isOreEnabled(direct) ? direct : null;
        }

        // Modded ores can participate automatically when they use the vanilla ore tags.
        for (OreType type : values()) {
            if (type.tag != null && OreTrackerConfig.isOreEnabled(type) && state.is(type.tag)) {
                return type;
            }
        }
        return null;
    }

    public static boolean isTracked(BlockState state) {
        return classify(state) != null;
    }

    private static Map<Block, OreType> createVanillaMap() {
        Map<Block, OreType> map = new IdentityHashMap<>();
        put(map, COAL, Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE);
        put(map, IRON, Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE);
        put(map, COPPER, Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE);
        put(map, GOLD, Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE, Blocks.NETHER_GOLD_ORE);
        put(map, REDSTONE, Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE);
        put(map, LAPIS, Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE);
        put(map, DIAMOND, Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE);
        put(map, EMERALD, Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE);
        put(map, QUARTZ, Blocks.NETHER_QUARTZ_ORE);
        put(map, ANCIENT_DEBRIS, Blocks.ANCIENT_DEBRIS);
        return Map.copyOf(map);
    }

    private static void put(Map<Block, OreType> map, OreType type, Block... blocks) {
        for (Block block : blocks) {
            map.put(block, type);
        }
    }
}
