package com.redstoneguard.screen;

import com.redstoneguard.config.ModConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.ChunkPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class RedstoneWarningScreen extends Screen {

    private static final int MAX_CHUNKS_SHOWN = 5;

    private final Screen returnScreen;
    private final Runnable disconnectAction;
    private final List<ChunkPos> activeChunkList;

    private int ticksRemaining;
    private ButtonWidget leaveButton;

    private int centerX, centerY;

    public RedstoneWarningScreen(Screen returnScreen, Runnable disconnectAction,
                                  Map<ChunkPos, Integer> activeChunks) {
        super(Text.literal("Redstone Warning"));
        this.returnScreen     = returnScreen;
        this.disconnectAction = disconnectAction;
        this.ticksRemaining   = ModConfig.getInstance().countdownSeconds * 20;
        // activeChunks is already sorted descending by count — take the top ones
        this.activeChunkList  = new ArrayList<>(activeChunks.keySet());
    }

    @Override
    protected void init() {
        centerX = this.width  / 2;
        centerY = this.height / 2;

        leaveButton = ButtonWidget.builder(leaveButtonLabel(), button -> {
            client.setScreen(null);
            disconnectAction.run();
        }).dimensions(centerX - 155, centerY + 70, 150, 20).build();
        leaveButton.active = (ticksRemaining <= 0);

        ButtonWidget stayButton = ButtonWidget.builder(
                Text.literal("Stay"),
                button -> client.setScreen(returnScreen)
        ).dimensions(centerX + 5, centerY + 70, 150, 20).build();

        addDrawableChild(leaveButton);
        addDrawableChild(stayButton);
    }

    @Override
    public void tick() {
        if (ticksRemaining > 0) {
            ticksRemaining--;
            if (ticksRemaining == 0) {
                leaveButton.active = true;
                leaveButton.setMessage(Text.literal("Leave World"));
            } else {
                leaveButton.setMessage(leaveButtonLabel());
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0xC0101010);

        // Title
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("REDSTONE TICKING DETECTED"),
                centerX, centerY - 70, 0xFFFF3333);

        // Warning lines
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Your farms are still running!"),
                centerX, centerY - 52, 0xFFFFFFFF);

        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Make sure your farms are turned off before leaving."),
                centerX, centerY - 40, 0xFFFFAAAA);

        // Active chunk list
        int chunkCount = activeChunkList.size();
        int shown = Math.min(chunkCount, MAX_CHUNKS_SHOWN);

        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Active chunks (" + chunkCount + "):"),
                centerX, centerY - 22, 0xFFFFAA00);

        for (int i = 0; i < shown; i++) {
            ChunkPos cp = activeChunkList.get(i);
            String label = "Chunk [" + cp.x + ", " + cp.z + "]";
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal(label),
                    centerX, centerY - 10 + (i * 10), 0xFFFFCC55);
        }

        if (chunkCount > MAX_CHUNKS_SHOWN) {
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("... and " + (chunkCount - MAX_CHUNKS_SHOWN) + " more"),
                    centerX, centerY - 10 + (shown * 10), 0xFFAAAAAA);
        }

        // Countdown
        if (ticksRemaining > 0) {
            int s = (ticksRemaining + 19) / 20;
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("Please wait " + s + "s..."),
                    centerX, centerY + 52, 0xFFAAAAAA);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    private Text leaveButtonLabel() {
        if (ticksRemaining <= 0) return Text.literal("Leave World");
        int s = (ticksRemaining + 19) / 20;
        return Text.literal("Leave World (" + s + "s)");
    }
}
