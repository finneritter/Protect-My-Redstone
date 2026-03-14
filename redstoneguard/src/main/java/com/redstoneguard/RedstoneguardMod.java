package com.redstoneguard;

import com.redstoneguard.config.ModConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.util.math.ChunkPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Environment(EnvType.CLIENT)
public class RedstoneguardMod implements ClientModInitializer {

    public static final String MOD_ID = "redstoneguard";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /**
     * Every chunk position currently loaded on the client, maintained via
     * {@link ClientChunkEvents}. This covers chunk-loader-kept chunks that are
     * outside the player's render distance.
     */
    public static final Set<ChunkPos> LOADED_CHUNKS = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Override
    public void onInitializeClient() {
        ModConfig.load();

        // Track every chunk the client has loaded
        ClientChunkEvents.CHUNK_LOAD.register((world, chunk) ->
                LOADED_CHUNKS.add(chunk.getPos()));

        ClientChunkEvents.CHUNK_UNLOAD.register((world, chunk) ->
                LOADED_CHUNKS.remove(chunk.getPos()));

        // Clear the set when the player leaves a world so stale positions don't carry over
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
                LOADED_CHUNKS.clear());

        LOGGER.info("Protect My Redstone loaded — tracking all loaded chunks for farm detection.");
    }
}
