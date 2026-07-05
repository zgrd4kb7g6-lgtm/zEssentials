package fr.maxlego08.essentials.bot.staffchat;

import fr.maxlego08.essentials.bot.DiscordBot;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class StaffChatManager extends ListenerAdapter {

    private final DiscordBot instance;

    public StaffChatManager(DiscordBot instance) {
        this.instance = instance;
    }

    // =====================================================
    // DISCORD → MINECRAFT
    // =====================================================
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {

        if (event.getAuthor().isBot()) return;

        var config = instance.getConfiguration().getFeatures().staffChat();

        if (!config.enabled()) return;

        if (event.getChannel().getIdLong() != config.discordChannelId()) return;

        String message =
                "§8[§9DISCORD§8] §f"
                        + event.getAuthor().getName()
                        + " §7» §f"
                        + event.getMessage().getContentDisplay();

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("zessentials.staffchat")) {
                p.sendMessage(message);
            }
        }
    }

    // =====================================================
    // MINECRAFT → DISCORD
    // =====================================================
    public void sendToDiscord(String player, String message) {

        var config = instance.getConfiguration().getFeatures().staffChat();

        if (!config.enabled()) return;

        var channel = instance.getJda().getTextChannelById(config.discordChannelId());

        if (channel == null) return;

        channel.sendMessage("**[MC STAFF]** " + player + " » " + message).queue();
    }
}