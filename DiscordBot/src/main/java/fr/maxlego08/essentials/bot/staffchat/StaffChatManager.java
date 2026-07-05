package fr.maxlego08.essentials.bot.staffchat;

import fr.maxlego08.essentials.bot.DiscordBot;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class StaffChatManager extends ListenerAdapter {

    private final DiscordBot instance;

    // prevents loops (discord -> minecraft -> discord spam loop)
    private final Set<Long> recentDiscordMessages = new HashSet<>();

    public StaffChatManager(DiscordBot instance) {
        this.instance = instance;
    }

    // =========================
    // DISCORD → MINECRAFT
    // =========================
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {

        if (event.getAuthor().isBot()) return;

        var config = instance.getConfiguration().getStaffChat();

        if (!config.enabled()) return;
        if (!config.syncDiscordToMinecraft()) return;

        if (event.getChannel().getIdLong() != config.discordChannelId()) return;

        String message = config.format().discord()
                .replace("%player%", event.getAuthor().getName())
                .replace("%message%", event.getMessage().getContentDisplay());

        // SEND TO MINECRAFT (placeholder hook)
        sendToMinecraft(message);
    }

    // =========================
    // MINECRAFT → DISCORD (HOOK)
    // =========================
    public void sendMinecraftMessage(String player, String message) {

        var config = instance.getConfiguration().getStaffChat();

        if (!config.enabled()) return;
        if (!config.syncMinecraftToDiscord()) return;

        String formatted = config.format().minecraft()
                .replace("%player%", player)
                .replace("%message%", message);

        var channel = instance.getJda()
                .getTextChannelById(config.discordChannelId());

        if (channel == null) return;

        channel.sendMessage(formatted).queue();
    }

    // =========================
    // MINECRAFT HOOK PLACEHOLDER
    // =========================
    private void sendToMinecraft(String message) {
        // This will later connect to your Spigot plugin via socket/plugin channel
        System.out.println("[STAFFCHAT → MC] " + message);
    }
}