package dev.ua.ikeepcalm.mdlootcatwalk.command;

import dev.ua.ikeepcalm.mdlootcatwalk.manager.LootManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MDLootCommand implements CommandExecutor, TabCompleter {

    private final LootManager lootManager;

    public MDLootCommand(LootManager lootManager) {
        this.lootManager = lootManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("mdloot.admin")) {
            sender.sendMessage(Component.text("You don't have permission to use this command.").color(NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(Component.text("Usage: /mdc <reload>").color(NamedTextColor.YELLOW));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                sender.sendMessage(Component.text("Reloading loot tables...").color(NamedTextColor.YELLOW));
                boolean success = lootManager.reloadLootTables();
                if (success) {
                    sender.sendMessage(Component.text("Loot tables reloaded successfully!").color(NamedTextColor.GREEN));
                } else {
                    sender.sendMessage(Component.text("Failed to reload loot tables. Check console for errors.").color(NamedTextColor.RED));
                }
                break;
            default:
                sender.sendMessage(Component.text("Unknown subcommand. Use: /mdloot reload").color(NamedTextColor.RED));
                break;
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("reload");
        }
        return List.of();
    }
}