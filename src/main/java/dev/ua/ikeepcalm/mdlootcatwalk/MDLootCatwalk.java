package dev.ua.ikeepcalm.mdlootcatwalk;

import dev.ua.ikeepcalm.mdlootcatwalk.api.LootEndpoint;
import dev.ua.ikeepcalm.mdlootcatwalk.command.MDLootCommand;
import dev.ua.ikeepcalm.mdlootcatwalk.config.LootConfig;
import dev.ua.ikeepcalm.mdlootcatwalk.manager.LootManager;
import dev.ua.uaproject.catwalk.hub.webserver.services.CatWalkWebserverService;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class MDLootCatwalk extends JavaPlugin {

    @Getter
    @Setter
    private static MDLootCatwalk instance;

    private LootConfig lootConfig;
    private LootManager lootManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        lootConfig = new LootConfig(this);

        lootManager = new LootManager(this, lootConfig);

        getCommand("mdc").setExecutor(new MDLootCommand(lootManager));

        CatWalkWebserverService webserverService = Bukkit.getServicesManager().load(CatWalkWebserverService.class);

        if (webserverService == null) {
            getLogger().severe("Failed to load CatWalkWebserverService from Bukkit ServicesManager.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        webserverService.registerHandlers(new LootEndpoint(lootManager));

        log("MDLootCatwalk has been enabled!");
    }

    @Override
    public void onDisable() {
        if (lootManager != null) {
            lootManager.stop();
        }
        log("MDLootCatwalk has been disabled!");
    }

    public static void log(String message) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.BLUE + "[MDLootCatwalk] " + ChatColor.WHITE + message);
    }

    public static void error(String message) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[MDLootCatwalk] " + ChatColor.WHITE + message);
    }
}
