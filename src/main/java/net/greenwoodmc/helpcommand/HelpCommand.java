package net.greenwoodmc.helpcommand;

import net.greenwoodmc.helpcommand.commands.hcCommand;
import net.greenwoodmc.helpcommand.listeners.helpAliases;
import net.greenwoodmc.helpcommand.tabcomplete.hc;
import net.greenwoodmc.helpcommand.util.FormatMode;
import net.greenwoodmc.helpcommand.util.TextUtil;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import net.greenwoodmc.helpcommand.commands.help;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.List;

public class HelpCommand extends JavaPlugin {

    public void onEnable() {

        getLogger().info("Help Command Enabled");
        getLogger().info("Author: VoidemLIVE");
        getLogger().info("Version: " + getDescription().getVersion());
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            getLogger().info("PlaceholderAPI: Enabled");
        } else {
            getLogger().info("PlaceholderAPI: Disabled");
        }
        int pluginId = 15592;
        new Metrics(this, pluginId);
        getConfig().options().copyDefaults(false);
        boolean freshInstall = !new File(getDataFolder(), "config.yml").exists();
        saveDefaultConfig();
        resolveFormat(freshInstall);
        FileConfiguration config = getConfig();
        getCommand("help").setExecutor(new help());
        getCommand("hc").setExecutor(new hcCommand());
        getCommand("hc").setTabCompleter(new hc());
        if (!config.getStringList("aliases").isEmpty()) {
            getLogger().info("Aliases: Enabled");
            getServer().getPluginManager().registerEvents(new helpAliases(), this);
            registerAliases();
        } else {
            getLogger().info("Aliases: Disabled");
        }

        try {
            Class.forName("org.spigotmc.SpigotConfig");
        } catch (ClassNotFoundException ex) {
            getLogger().severe("To run Help Command, you need to install Spigot or a fork of Spigot");
            getLogger().severe("Download here: https://www.spigotmc.org/wiki/spigot-installation/.");
            getPluginLoader().disablePlugin(this);
            return;
        }
    }

    public void onDisable() {
        getLogger().info("Help Plugin Disabled");
        getLogger().info("Author: VoidemLIVE");
        getLogger().info("Version: " + getDescription().getVersion());
    }

    // determine if the config is legacy or MM
    public void resolveFormat(boolean freshInstall) {
        File file = new File(getDataFolder(), "config.yml");

        String configured = YamlConfiguration.loadConfiguration(file).getString("format");

        if (configured == null) {
            TextUtil.setFormat(FormatMode.LEGACY);
            if (freshInstall) {
                getLogger().warning("Format: LEGACY (could not read 'format' from config.yml)");
            } else {
                getLogger().info("Format: LEGACY (this config predates MiniMessage support)");
                getLogger().info("Format: set 'format: MINIMESSAGE' in config.yml to opt in");
                appendFormatKey(file);
            }
            return;
        }

        FormatMode parsed = FormatMode.parse(configured);
        if (parsed == null) {
            getLogger().warning("Format: '" + configured + "' is not a format, expected LEGACY or MINIMESSAGE");
            getLogger().warning("Format: falling back to LEGACY");
            parsed = FormatMode.LEGACY;
        }

        if (parsed == FormatMode.MINIMESSAGE && !TextUtil.isMiniMessageAvailable()) {
            getLogger().warning("Format: MINIMESSAGE needs MiniMessage, which this server does not provide");
            getLogger().warning("Format: MiniMessage ships with Paper 1.18.2+ and its forks; falling back to LEGACY");
            parsed = FormatMode.LEGACY;
        }

        TextUtil.setFormat(parsed);
        getLogger().info("Format: " + parsed);
    }

    private void appendFormatKey(File file) {
        if (!file.isFile()) {
            return;
        }

        String eol = System.lineSeparator();

        String block = eol + eol
                + "######################################" + eol
                + "# Text format" + eol
                + "######################################" + eol
                + "# LEGACY      - the '&' colour codes, plus previous custom formatting" + eol
                + "# MINIMESSAGE - MiniMessage tags" + eol
                + "#" + eol
                + "# This key was added automatically, and is set to LEGACY because this config was" + eol
                + "# written before MiniMessage support existed." + eol
                + "# Switching to MINIMESSAGE means rewriting the messages in this" + eol
                + "# file in MiniMessage syntax, as the two cannot be mixed. Needs Paper or a fork." + eol
                + "format: LEGACY" + eol;

        Writer writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(file, true), Charset.forName("UTF-8"));
            writer.write(block);
        } catch (IOException e) {
            // Costs discoverability only: an absent key already resolves to LEGACY above.
            getLogger().warning("Format: could not add 'format' to config.yml: " + e.getMessage());
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public static boolean isPapiInstalled(JavaPlugin plugin) {
        return plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    private void registerAliases() {
        CommandMap commandMap = getCommandMap();
        List<String> aliases = getConfig().getStringList("aliases");
        for (String alias : aliases) {
            BukkitCommand command = new BukkitCommand(alias) {
                @Override
                public boolean execute(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String[] strings) {
                    return false;
                }
            };
            commandMap.register(getDescription().getName(), command);
        }
    }

    private CommandMap getCommandMap() {
        try {
            Field commandMapField = getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            return (CommandMap) commandMapField.get(getServer());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }
}