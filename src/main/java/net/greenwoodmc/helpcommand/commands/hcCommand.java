package net.greenwoodmc.helpcommand.commands;

import net.greenwoodmc.helpcommand.HelpCommand;
import net.greenwoodmc.helpcommand.util.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class hcCommand implements CommandExecutor {
    HelpCommand plug = JavaPlugin.getPlugin(HelpCommand.class);
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        FileConfiguration config = plug.getConfig();
        String ver;
        String arg1;
        Boolean isPlayer;
        if (!(sender instanceof Player)) {
            isPlayer = false;
        } else {
            isPlayer = true;
        }

        if (cmd.getName().equalsIgnoreCase("hc")) {
            if (args.length >= 1) {
                arg1 = args[0];
                if (arg1.equalsIgnoreCase("reload") || arg1.equalsIgnoreCase("rl")) {
                    plug.reloadConfig();
                    plug.resolveFormat(false);
                    ver = plug.getConfig().getString("reload");
                    if (isPlayer) {
                        TextUtil.send(sender, ver);
                    } else {
                        plug.getLogger().info("Plugin reloaded");
                    }
                }

                if (arg1.equalsIgnoreCase("version") || arg1.equalsIgnoreCase("ver")) {
                    ver = plug.getDescription().getVersion();
                    if (isPlayer) {
                        sender.sendMessage("HelpCommand Version: " + ver);
                    } else {
                        plug.getLogger().info("HelpCommand Version: " + ver);
                    }
                }

                if (arg1.equalsIgnoreCase("help")) {
                    if (isPlayer) {
                        TextUtil.sendInternal(sender, "&6HelpCommand wiki: &ehttps://hcdocs.voidem.com");
                        TextUtil.sendInternal(sender, "&6Support Discord: &ehttps://support.voidem.com");
                    } else {
                        plug.getLogger().info("HelpCommand wiki: https://hcdocs.voidem.com");
                        plug.getLogger().info("Support Discord: https://support.voidem.com");
                    }
                }
            } else {
                if (isPlayer) {
                    arg1 = config.getString("noargument");
                    TextUtil.send(sender, arg1);
                } else {
                    plug.getLogger().info("No argument");
                }
            }
        }

        return false;
    }
}
