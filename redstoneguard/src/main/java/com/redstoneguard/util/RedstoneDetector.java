package com.redstoneguard.util;

import com.redstoneguard.RedstoneguardMod;
import com.redstoneguard.config.ModConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ComparatorBlockEntity;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.block.enums.SculkSensorPhase;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

import java.util.*;
import java.util.function.Predicate;

/**
 * Scans every chunk the client has loaded, anywhere in the world.
 *
 * Primary source: {@link RedstoneguardMod#LOADED_CHUNKS}, maintained via
 * ClientChunkEvents. This covers all loaded chunks regardless of distance —
 * including chunk-loader-kept chunks thousands of blocks away.
 *
 * Secondary source: a local sweep within {@link #SWEEP_RADIUS} chunks of the
 * player, used only as a safety net in case any CHUNK_LOAD events were missed.
 *
 * Active redstone indicators detected per chunk:
 *   Block-entity scan  — PistonBlockEntity (mid-motion piston, sticky or regular)
 *                        ComparatorBlockEntity with output signal > 0
 *   Block-state scan   — RepeaterBlock with POWERED = true
 *                        ObserverBlock  with POWERED = true
 *                        PoweredRailBlock with POWERED = true
 *                        DetectorRailBlock with POWERED = true
 *                        SculkSensorBlock / CalibratedSculkSensorBlock with phase ACTIVE or COOLDOWN
 *                        HopperBlock with ENABLED = false (redstone is blocking it)
 */
@Environment(EnvType.CLIENT)
public final class RedstoneDetector {

    /** Local sweep radius around the player (chunks). Safety net for missed CHUNK_LOAD events only — distant chunks are covered by LOADED_CHUNKS. */
    private static final int SWEEP_RADIUS = 32;

    /** Broad palette pre-filter: any redstone component, powered or not. */
    private static final Predicate<BlockState> HAS_REDSTONE =
            state -> isRedstoneComponent(state.getBlock());

    private RedstoneDetector() {}

    /**
     * Returns every chunk that has at least one active redstone indicator,
     * sorted descending by indicator count.
     */
    public static Map<ChunkPos, Integer> getActiveChunks() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return Collections.emptyMap();

        ClientWorld world = client.world;

        // Build the set of chunks to scan:
        //  1. Everything tracked by chunk-load events (covers chunk-loader-kept chunks)
        //  2. Direct sweep around the player to catch anything the events may have missed
        Set<ChunkPos> toScan = new HashSet<>(RedstoneguardMod.LOADED_CHUNKS);

        int px = client.player.getBlockPos().getX() >> 4;
        int pz = client.player.getBlockPos().getZ() >> 4;
        for (int dx = -SWEEP_RADIUS; dx <= SWEEP_RADIUS; dx++) {
            for (int dz = -SWEEP_RADIUS; dz <= SWEEP_RADIUS; dz++) {
                ChunkPos cp = new ChunkPos(px + dx, pz + dz);
                if (world.getChunkManager().getWorldChunk(cp.x, cp.z) != null) {
                    toScan.add(cp);
                }
            }
        }

        Map<ChunkPos, Integer> result = new LinkedHashMap<>();
        ModConfig cfg = ModConfig.getInstance();

        for (ChunkPos pos : toScan) {
            WorldChunk chunk = world.getChunkManager().getWorldChunk(pos.x, pos.z);
            if (chunk == null || chunk.isEmpty()) continue;

            int count = 0;

            // --- Block-entity scan (O(entities), fast) ---
            for (BlockEntity be : chunk.getBlockEntities().values()) {
                if (cfg.countPistons && be instanceof PistonBlockEntity) {
                    // Piston currently mid-motion (extend or retract, sticky or regular)
                    count++;
                } else if (cfg.countComparators && be instanceof ComparatorBlockEntity comparator) {
                    if (comparator.getOutputSignal() > 0) count++;
                }
            }

            // --- Block-state scan with palette pre-filter ---
            for (ChunkSection section : chunk.getSectionArray()) {
                if (section == null || section.isEmpty()) continue;
                if (!section.hasAny(HAS_REDSTONE)) continue;

                for (int lx = 0; lx < 16; lx++) {
                    for (int ly = 0; ly < 16; ly++) {
                        for (int lz = 0; lz < 16; lz++) {
                            if (isActiveRedstoneBlock(section.getBlockState(lx, ly, lz), cfg)) {
                                count++;
                            }
                        }
                    }
                }
            }

            if (count > 0) {
                result.put(pos, count);
            }
        }

        // Sort descending by indicator count
        List<Map.Entry<ChunkPos, Integer>> entries = new ArrayList<>(result.entrySet());
        entries.sort((a, b) -> b.getValue() - a.getValue());
        Map<ChunkPos, Integer> sorted = new LinkedHashMap<>();
        for (Map.Entry<ChunkPos, Integer> e : entries) sorted.put(e.getKey(), e.getValue());
        return sorted;
    }

    /** Convenience total count used by the mixin threshold check. */
    public static int countActiveIndicators() {
        return getActiveChunks().values().stream().mapToInt(Integer::intValue).sum();
    }

    // -------------------------------------------------------------------------

    /**
     * Broad pre-filter — returns true for ANY redstone-related block regardless
     * of powered state. Used only by {@link ChunkSection#hasAny} to skip
     * sections with zero redstone components entirely.
     */
    private static boolean isRedstoneComponent(Block block) {
        return block instanceof RepeaterBlock
                || block instanceof ObserverBlock
                || block instanceof ComparatorBlock
                || block instanceof AbstractRedstoneGateBlock
                || block instanceof PistonBlock
                || block instanceof RedstoneWireBlock
                || block instanceof RedstoneTorchBlock
                || block instanceof PoweredRailBlock
                || block instanceof DetectorRailBlock
                || block instanceof SculkSensorBlock   // covers CalibratedSculkSensorBlock too
                || block instanceof NoteBlock
                || block instanceof DoorBlock
                || block instanceof TrapdoorBlock
                || block instanceof DropperBlock
                || block instanceof DispenserBlock;
    }

    /**
     * Narrow active check — returns true only when the block is currently in an
     * active/powered state.
     *
     * Detected as "active":
     *   - Repeater with POWERED = true
     *   - Observer with POWERED = true
     *   - PoweredRail with POWERED = true
     *   - DetectorRail with POWERED = true (minecart is on it)
     *   - SculkSensor / CalibratedSculkSensor with phase ACTIVE or COOLDOWN
     *   - Hopper with ENABLED = false (redstone is actively blocking it)
     *
     * Pistons are excluded here — idle extended pistons are not active.
     * Mid-motion pistons are caught by PistonBlockEntity in the block-entity scan.
     */
    private static boolean isActiveRedstoneBlock(BlockState state, ModConfig cfg) {
        Block block = state.getBlock();

        if (cfg.countRepeaters && block instanceof RepeaterBlock)
            return state.get(RepeaterBlock.POWERED);

        if (cfg.countObservers && block instanceof ObserverBlock)
            return state.get(ObserverBlock.POWERED);

        if (cfg.countPoweredRails && block instanceof PoweredRailBlock)
            return state.get(PoweredRailBlock.POWERED);

        if (cfg.countDetectorRails && block instanceof DetectorRailBlock)
            return state.get(DetectorRailBlock.POWERED);

        // SculkSensorBlock is parent of CalibratedSculkSensorBlock — one check covers both
        if (cfg.countSculkSensors && block instanceof SculkSensorBlock) {
            SculkSensorPhase phase = state.get(SculkSensorBlock.SCULK_SENSOR_PHASE);
            return phase == SculkSensorPhase.ACTIVE || phase == SculkSensorPhase.COOLDOWN;
        }

        return false;
    }
}
