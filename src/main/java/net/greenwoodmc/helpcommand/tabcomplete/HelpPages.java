package net.greenwoodmc.helpcommand.tabcomplete;

import net.greenwoodmc.helpcommand.HelpCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class HelpPages implements TabCompleter {
    private final HelpCommand plugin;

    public HelpPages(HelpCommand plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        FileConfiguration config = plugin.getConfig();
        // An explicit empty list prevents Bukkit's default player-name fallback.
        if (!config.getBoolean("helpcmd") || args.length != 1) {
            return Collections.emptyList();
        }
        String prefix = args[0];
        return config.getIntegerList("pagesEnabled").stream()
                .filter(page -> page > 0 && config.isList("help." + page))
                .distinct()
                .sorted()
                .map(String::valueOf)
                .filter(page -> page.startsWith(prefix))
                .collect(Collectors.toList());
    }
}
