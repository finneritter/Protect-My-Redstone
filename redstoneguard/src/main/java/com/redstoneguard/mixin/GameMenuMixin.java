package com.redstoneguard.mixin;

import com.redstoneguard.config.ModConfig;
import com.redstoneguard.screen.RedstoneWarningScreen;
import com.redstoneguard.util.RedstoneDetector;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Environment(EnvType.CLIENT)
@Mixin(MinecraftClient.class)
public class GameMenuMixin {

    @Unique
    private static boolean redstoneguard$bypassWarning = false;

    @Inject(method = "disconnect(Lnet/minecraft/text/Text;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void redstoneguard$onDisconnect(Text reason, CallbackInfo ci) {
        if (redstoneguard$bypassWarning) {
            redstoneguard$bypassWarning = false;
            return;
        }

        ModConfig cfg = ModConfig.getInstance();
        if (!cfg.enabled) return;

        MinecraftClient client = (MinecraftClient) (Object) this;
        if (client.currentScreen == null) return;

        // Scan all loaded chunks once and reuse the result for both the threshold
        // check and passing the chunk map to the warning screen.
        Map<ChunkPos, Integer> activeChunks = RedstoneDetector.getActiveChunks();
        int total = activeChunks.values().stream().mapToInt(Integer::intValue).sum();
        if (total < cfg.threshold) return;

        Screen returnScreen = client.currentScreen;

        Runnable disconnectAction = () -> {
            redstoneguard$bypassWarning = true;
            client.disconnect(reason);
        };

        client.setScreen(new RedstoneWarningScreen(returnScreen, disconnectAction, activeChunks));
        ci.cancel();
    }
}
