package net.greenwoodmc.helpcommand.util;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;


final class AdventureText {

    private AdventureText() {
    }

    static Component parse(String message) {
        return MiniMessage.miniMessage().deserialize(message);
    }

    static Component fromLegacy(String message) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(message);
    }

    static void send(CommandSender target, Component message) {
        ((Audience) target).sendMessage(message);
    }

    static void sendPagePrompt(CommandSender target,
                               String back, String backCommand,
                               String page,
                               String forward, String forwardCommand) {
        Component prompt = Component.empty();

        if (back != null) {
            prompt = prompt.append(parse(back).clickEvent(ClickEvent.runCommand(backCommand)));
        }
        if (page != null) {
            prompt = prompt.append(parse(page));
        }
        if (forward != null) {
            prompt = prompt.append(parse(forward).clickEvent(ClickEvent.runCommand(forwardCommand)));
        }

        send(target, prompt);
    }
}
