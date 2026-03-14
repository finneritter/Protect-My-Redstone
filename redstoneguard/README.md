# Protect My Redstone

A client-side Fabric mod for Minecraft 1.21.11 that warns you before disconnecting while active redstone farms are running in any loaded chunk.

## The Problem

You're on an SMP server. Your iron farm, sugarcane farm, or sorting system is ticking away. You hit "Disconnect" out of habit — and now your farms sit broken until you log back in. Protect My Redstone intercepts the disconnect and shows a warning screen so you never make that mistake again.

## Features

- **Active redstone detection** — Scans all loaded chunks (including chunk-loader-kept distant chunks) for moving pistons, powered comparators, repeaters, observers, sculk sensors, powered rails, and more
- **Warning screen with countdown** — A locked warning screen prevents accidental disconnects. The "Leave World" button stays disabled for a configurable countdown period
- **Configurable threshold** — Set how many active redstone indicators are needed before the warning triggers (1–64)
- **Granular component toggles** — Choose exactly which redstone components count toward the threshold
- **In-game config screen** — Full ModMenu + Cloth Config integration for easy settings management

## Installation

### Requirements

- Minecraft **1.21.11**
- [Fabric Loader](https://fabricmc.net/) **0.16.0+**
- [Fabric API](https://modrinth.com/mod/fabric-api)
- [Cloth Config](https://modrinth.com/mod/cloth-config) (required)
- [Mod Menu](https://modrinth.com/mod/modmenu) (optional, for in-game config screen)

### Steps

1. Install Fabric Loader for Minecraft 1.21.11
2. Drop the mod JAR along with Fabric API and Cloth Config into your `mods/` folder
3. Launch the game

## Configuration

Settings are stored in `config/redstoneguard.json` and can be edited in-game via Mod Menu.

| Setting | Default | Range | Description |
|---------|---------|-------|-------------|
| Enabled | `true` | — | Master switch for the mod |
| Threshold | `1` | 1–64 | Minimum active indicators to trigger the warning |
| Countdown | `5s` | 0–60s | How long the Leave button stays locked |

### Tracked Components

Each component type can be individually toggled on or off:

| Component | What it detects |
|-----------|----------------|
| Pistons | Pistons mid-extension or retraction |
| Comparators | Comparators with output signal > 0 |
| Repeaters | Powered repeaters |
| Observers | Powered observers |
| Powered Rails | Active powered rails |
| Detector Rails | Detector rails with a minecart on them |
| Sculk Sensors | Sculk sensors in active or cooldown phase |

## How It Works

1. The mod tracks all loaded chunks in real-time via Fabric's chunk events
2. When you try to disconnect, a Mixin intercepts `MinecraftClient.disconnect()`
3. `RedstoneDetector` scans tracked chunks for active redstone indicators, using palette pre-filtering for performance
4. If the count meets your threshold, a warning screen appears showing which chunks have the most activity
5. After the countdown expires, you can choose to leave or stay

## Building from Source

```bash
git clone https://github.com/your-username/redstoneguard.git
cd redstoneguard
./gradlew build
```

The built JAR will be in `build/libs/`.

## License

[MIT](LICENSE)
