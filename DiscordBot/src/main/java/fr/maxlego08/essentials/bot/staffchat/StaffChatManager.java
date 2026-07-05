package fr.maxlego08.essentials.bot.staffchat;

import fr.maxlego08.essentials.bot.DiscordBot;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class StaffChatManager extends ListenerAdapter {

    private final DiscordBot instance;

    public StaffChatManager(DiscordBot instance) {
        this.instance = instance;
    }

    // =========================
    // DISCORD -> MINECRAFT (VIA STORAGE RELAY)
    // =========================
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {

        if (event.getAuthor().isBot()) return;

        var config = instance.getConfiguration().getFeatures().staffChat();

        if (!config.enabled()) return;

        if (event.getChannel().getIdLong() != config.discordChannelId()) return;

        String formatted = config.format().discord()
                .replace("%player%", event.getAuthor().getName())
                .replace("%message%", event.getMessage().getContentDisplay());

        // ✅ STORE MESSAGE (Minecraft plugin will pick it up)
        instance.getStorageManager().insertStaffChatMessage(
                event.getAuthor().getName(),
                formatted,
                System.currentTimeMillis()
        );
    }

    // =========================
    // MINECRAFT -> DISCORD (CALLED BY PLUGIN)
    // =========================
    public void sendToDiscord(String player, String message) {

        var config = instance.getConfiguration().getFeatures().staffChat();

        if (!config.enabled()) return;

        String formatted = config.format().minecraft()
                .replace("%player%", player)
                .replace("%message%", message);

        var channel = instance.getJda()
                .getTextChannelById(config.discordChannelId());

        if (channel == null) {
            System.err.println("StaffChat channel not found: " + config.discordChannelId());
            return;
        }

        channel.sendMessage(formatted).queue();
    }
}