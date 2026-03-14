package com.redstoneguard.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.text.Text;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ModConfig cfg = ModConfig.getInstance();

            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Text.translatable("redstoneguard.config.title"))
                    .setSavingRunnable(ModConfig::save);

            ConfigEntryBuilder entry = builder.entryBuilder();
            ConfigCategory general = builder.getOrCreateCategory(
                    Text.translatable("redstoneguard.config.category.general"));

            // Toggle: enable/disable the mod
            general.addEntry(entry
                    .startBooleanToggle(
                            Text.translatable("redstoneguard.config.enabled"),
                            cfg.enabled)
                    .setDefaultValue(true)
                    .setTooltip(Text.translatable("redstoneguard.config.enabled.tooltip"))
                    .setSaveConsumer(val -> cfg.enabled = val)
                    .build());

            // Integer slider: threshold (1–64)
            general.addEntry(entry
                    .startIntSlider(
                            Text.translatable("redstoneguard.config.threshold"),
                            cfg.threshold,
                            1, 64)
                    .setDefaultValue(1)
                    .setTooltip(Text.translatable("redstoneguard.config.threshold.tooltip"))
                    .setTextGetter(val -> Text.literal(val + " indicator" + (val == 1 ? "" : "s")))
                    .setSaveConsumer(val -> cfg.threshold = val)
                    .build());

            // Integer slider: countdown seconds (0–60)
            general.addEntry(entry
                    .startIntSlider(
                            Text.translatable("redstoneguard.config.countdown"),
                            cfg.countdownSeconds,
                            0, 60)
                    .setDefaultValue(5)
                    .setTooltip(Text.translatable("redstoneguard.config.countdown.tooltip"))
                    .setTextGetter(val -> Text.literal(val + " second" + (val == 1 ? "" : "s")))
                    .setSaveConsumer(val -> cfg.countdownSeconds = val)
                    .build());

            // Subcategory: which components count toward the threshold
            SubCategoryBuilder components = entry
                    .startSubCategory(Text.translatable("redstoneguard.config.components"));

            components.add(entry
                    .startBooleanToggle(
                            Text.translatable("redstoneguard.config.components.pistons"),
                            cfg.countPistons)
                    .setDefaultValue(true)
                    .setTooltip(Text.translatable("redstoneguard.config.components.pistons.tooltip"))
                    .setSaveConsumer(val -> cfg.countPistons = val)
                    .build());

            components.add(entry
                    .startBooleanToggle(
                            Text.translatable("redstoneguard.config.components.comparators"),
                            cfg.countComparators)
                    .setDefaultValue(true)
                    .setTooltip(Text.translatable("redstoneguard.config.components.comparators.tooltip"))
                    .setSaveConsumer(val -> cfg.countComparators = val)
                    .build());

            components.add(entry
                    .startBooleanToggle(
                            Text.translatable("redstoneguard.config.components.repeaters"),
                            cfg.countRepeaters)
                    .setDefaultValue(true)
                    .setTooltip(Text.translatable("redstoneguard.config.components.repeaters.tooltip"))
                    .setSaveConsumer(val -> cfg.countRepeaters = val)
                    .build());

            components.add(entry
                    .startBooleanToggle(
                            Text.translatable("redstoneguard.config.components.observers"),
                            cfg.countObservers)
                    .setDefaultValue(true)
                    .setTooltip(Text.translatable("redstoneguard.config.components.observers.tooltip"))
                    .setSaveConsumer(val -> cfg.countObservers = val)
                    .build());

            components.add(entry
                    .startBooleanToggle(
                            Text.translatable("redstoneguard.config.components.poweredRails"),
                            cfg.countPoweredRails)
                    .setDefaultValue(true)
                    .setTooltip(Text.translatable("redstoneguard.config.components.poweredRails.tooltip"))
                    .setSaveConsumer(val -> cfg.countPoweredRails = val)
                    .build());

            components.add(entry
                    .startBooleanToggle(
                            Text.translatable("redstoneguard.config.components.detectorRails"),
                            cfg.countDetectorRails)
                    .setDefaultValue(true)
                    .setTooltip(Text.translatable("redstoneguard.config.components.detectorRails.tooltip"))
                    .setSaveConsumer(val -> cfg.countDetectorRails = val)
                    .build());

            components.add(entry
                    .startBooleanToggle(
                            Text.translatable("redstoneguard.config.components.sculkSensors"),
                            cfg.countSculkSensors)
                    .setDefaultValue(true)
                    .setTooltip(Text.translatable("redstoneguard.config.components.sculkSensors.tooltip"))
                    .setSaveConsumer(val -> cfg.countSculkSensors = val)
                    .build());

            general.addEntry(components.build());

            return builder.build();
        };
    }
}
