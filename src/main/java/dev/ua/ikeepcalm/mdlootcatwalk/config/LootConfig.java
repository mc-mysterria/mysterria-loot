package dev.ua.ikeepcalm.mdlootcatwalk.config;

import dev.ua.ikeepcalm.mdlootcatwalk.MDLootCatwalk;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

@Getter
public class LootConfig {

    private final String lootTablesFile;
    private final boolean autoReload;
    private final int refreshIntervalMinutes;
    private final int maxSearchResults;
    private final boolean includeItemStackDetails;

    public LootConfig(MDLootCatwalk plugin) {
        FileConfiguration config = plugin.getConfig();

        this.lootTablesFile = config.getString("lootTablesFile", "plugins/MythicDungeons/loottables.yml");
        this.autoReload = config.getBoolean("cache.autoReload", false);
        this.refreshIntervalMinutes = config.getInt("cache.refreshIntervalMinutes", 0);
        this.maxSearchResults = config.getInt("api.maxSearchResults", 100);
        this.includeItemStackDetails = config.getBoolean("api.includeItemStackDetails", true);
    }
}