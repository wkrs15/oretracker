package com.wkrs14.oretracker.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class OreScanner {
    private static final int SCAN_INTERVAL = 6;
    private static final int MAX_OUTLINES = 384;
    private static final List<DetectedOre> RESULTS = new ArrayList<>();
    private static int tickCounter;

    private OreScanner() {
    }

    public static void tick(Minecraft minecraft) {
        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (!OreTrackerConfig.isEnabled() || player == null || level == null) {
            if (!RESULTS.isEmpty()) {
                RESULTS.clear();
            }
            return;
        }

        tickCounter++;
        if (tickCounter % SCAN_INTERVAL == 0) {
            scan(level, player);
        }
    }

    public static List<DetectedOre> results() {
        return RESULTS;
    }

    public static int resultCount() {
        return RESULTS.size();
    }

    private static void scan(ClientLevel level, LocalPlayer player) {
        Map<BlockPos, DetectedOre> found = new HashMap<>();
        scanNearby(level, player, found);

        List<DetectedOre> sorted = new ArrayList<>(found.values());
        sorted.sort(Comparator.comparingDouble(value -> value.distanceSquared(player)));
        if (sorted.size() > MAX_OUTLINES) {
            sorted = new ArrayList<>(sorted.subList(0, MAX_OUTLINES));
        }

        RESULTS.clear();
        RESULTS.addAll(sorted);
    }

    private static void scanNearby(ClientLevel level, LocalPlayer player, Map<BlockPos, DetectedOre> output) {
        int radius = OreTrackerConfig.showLines() ? Math.max(OreTrackerConfig.getScanRadius(), OreTrackerConfig.getLineRange()) : OreTrackerConfig.getScanRadius();
        int verticalRadius = OreTrackerConfig.getVerticalRadius();
        int playerX = player.getBlockX();
        int playerY = player.getBlockY();
        int playerZ = player.getBlockZ();

        int minChunkX = SectionPos.blockToSectionCoord(playerX - radius);
        int maxChunkX = SectionPos.blockToSectionCoord(playerX + radius);
        int minChunkZ = SectionPos.blockToSectionCoord(playerZ - radius);
        int maxChunkZ = SectionPos.blockToSectionCoord(playerZ + radius);
        int minSectionY = Math.max(level.getMinSection(), SectionPos.blockToSectionCoord(playerY - verticalRadius));
        int maxSectionY = Math.min(level.getMaxSection(), SectionPos.blockToSectionCoord(playerY + verticalRadius));
        double horizontalRadiusSq = (double) radius * radius;

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                if (!level.hasChunk(chunkX, chunkZ)) {
                    continue;
                }

                LevelChunk chunk = level.getChunk(chunkX, chunkZ);
                int baseX = chunkX << 4;
                int baseZ = chunkZ << 4;

                for (int sectionY = minSectionY; sectionY <= maxSectionY; sectionY++) {
                    LevelChunkSection section = chunk.getSection(level.getSectionIndexFromSectionY(sectionY));
                    if (section == null || section.hasOnlyAir() || !section.maybeHas(OreType::isTracked)) {
                        continue;
                    }

                    int baseY = sectionY << 4;
                    for (int localX = 0; localX < 16; localX++) {
                        int worldX = baseX + localX;
                        double dx = worldX + 0.5D - player.getX();
                        if (dx * dx > horizontalRadiusSq) {
                            continue;
                        }

                        for (int localZ = 0; localZ < 16; localZ++) {
                            int worldZ = baseZ + localZ;
                            double dz = worldZ + 0.5D - player.getZ();
                            if (dx * dx + dz * dz > horizontalRadiusSq) {
                                continue;
                            }

                            for (int localY = 0; localY < 16; localY++) {
                                int worldY = baseY + localY;
                                if (Math.abs(worldY + 0.5D - player.getY()) > verticalRadius) {
                                    continue;
                                }

                                OreType type = OreType.classify(section.getBlockState(localX, localY, localZ));
                                if (type != null) {
                                    BlockPos pos = new BlockPos(worldX, worldY, worldZ);
                                    output.put(pos, new DetectedOre(pos, type));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public record DetectedOre(BlockPos pos, OreType type) {
        public double distanceSquared(LocalPlayer player) {
            return pos.distToCenterSqr(player.getX(), player.getY(), player.getZ());
        }
    }
}

