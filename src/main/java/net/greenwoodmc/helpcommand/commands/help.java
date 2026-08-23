package net.greenwoodmc.helpcommand.commands;

import me.clip.placeholderapi.PlaceholderAPI;
import net.greenwoodmc.helpcommand.HelpCommand;
import net.greenwoodmc.helpcommand.util.FormatMode;
import net.greenwoodmc.helpcommand.util.TextUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.List;
import java.util.stream.Collectors;

public class help implements CommandExecutor {

    private static final Pattern TOKEN = Pattern.compile("\\$\\[(.*?)\\]");

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        FileConfiguration config = JavaPlugin.getPlugin(HelpCommand.class).getConfig();

        if (!(sender instanceof Player)) {
            sender.sendMessage(config.getString("playersOnly"));
            return true;
        }

        Player player = (Player) sender;
        String ver;
        String ver2;
        String pageText = config.getString("pagePrompt.page");
        String backArrow = config.getString("pagePrompt.arrowBack");
        String forwardArrow = config.getString("pagePrompt.arrowForward");
        int currPageNumber;
        List<Integer> enabledPages = config.getIntegerList("pagesEnabled");
        int lastPage = enabledPages.get(enabledPages.size() - 1);
        JavaPlugin plug = JavaPlugin.getPlugin(HelpCommand.class);
        boolean papiInstalled = HelpCommand.isPapiInstalled(plug);
        boolean pagePromptEnabled = config.getBoolean("pagePrompt.enabled");

        if (cmd.getName().equalsIgnoreCase("help")) {
            if (config.getBoolean("helpcmd")) {
                if (args.length > 0) {
                    try {
                        currPageNumber = Integer.parseInt(args[0]);
                    } catch (NumberFormatException e) {
                        TextUtil.send(player, config.getString("pageNA"));
                        return true;
                    }
                } else {
                    currPageNumber = 1; // Default to page 1 if no arguments provided
                }

                if (currPageNumber >= 1 && currPageNumber <= lastPage) {
                    if (enabledPages.contains(currPageNumber)) {
                        ver = config.getStringList("help." + currPageNumber).stream().collect(Collectors.joining("\n"));
                        ver2 = papiInstalled ? PlaceholderAPI.setPlaceholders(player, ver) : ver;
                        TextUtil.send(player, ver2);

                        if (pagePromptEnabled && enabledPages.size() > 1) { // Only show page prompt if more than one page
                            sendPagePrompt(player, currPageNumber, lastPage, pageText, backArrow, forwardArrow);
                        }

                    } else {
                        TextUtil.send(player, config.getString("pageNA"));
                    }
                } else {
                    TextUtil.send(player, config.getString("pageNA"));
                }
            } else {
                TextUtil.send(player, config.getString("disabled"));
            }
        }
        return true;
    }

    private void sendPagePrompt(Player player, int currPageNumber, int lastPage,
                                String pageText, String backArrow, String forwardArrow) {
        int previousPageNumber = currPageNumber - 1;
        int nextPageNumber = currPageNumber + 1;

        String back = currPageNumber > 1
                ? resolveToken(backArrow + " ", "prevNum", String.valueOf(previousPageNumber))
                : null;
        String page = resolveToken(pageText, "pageNum", String.valueOf(currPageNumber));
        String forward = currPageNumber < lastPage
                ? resolveToken(" " + forwardArrow, "nextNum", String.valueOf(nextPageNumber))
                : null;

        String backCommand = "/help " + previousPageNumber;
        String forwardCommand = "/help " + nextPageNumber;

        if (TextUtil.getFormat() == FormatMode.MINIMESSAGE) {
            TextUtil.sendPagePrompt(player, back, backCommand, page, forward, forwardCommand);
            return;
        }

        ComponentBuilder pagePromptBuilder = new ComponentBuilder();
        if (back != null) {
            pagePromptBuilder.append(TextUtil.color(back))
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, backCommand));
        }
        if (page != null) {
            pagePromptBuilder.append(new TextComponent(TextUtil.color(page)), ComponentBuilder.FormatRetention.NONE);
        }
        if (forward != null) {
            pagePromptBuilder.append(TextUtil.color(forward))
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, forwardCommand));
        }

        player.spigot().sendMessage(pagePromptBuilder.create());
    }


    private static String resolveToken(String template, String token, String value) {
        if (template == null) {
            return null;
        }

        Matcher matcher = TOKEN.matcher(template);
        if (!matcher.find()) {
            return template;
        }

        if (token.equalsIgnoreCase(matcher.group(1))) {
            return template.replace("$[" + token + "]", value);
        }

        return null;
    }
}
