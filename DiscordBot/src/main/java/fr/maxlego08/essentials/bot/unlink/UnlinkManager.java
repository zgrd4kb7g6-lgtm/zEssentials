package fr.maxlego08.essentials.bot.unlink;

import fr.maxlego08.essentials.bot.DiscordBot;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class UnlinkManager extends ListenerAdapter {

    private final DiscordBot instance;

    public UnlinkManager(DiscordBot instance) {
        this.instance = instance;
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {

        if (!instance.getFeatureManager().isEnabled("unlink")) return;

        if (!event.getComponentId().equals("zessentials:unlink")) return;

        long userId = event.getUser().getIdLong();

        event.deferReply(true).queue();

        CompletableFuture.runAsync(() -> handleUnlink(event, userId));
    }

    private void handleUnlink(ButtonInteractionEvent event, long userId) {

        var storage = instance.getStorageManager();
        var config = instance.getConfiguration().getFeatures().unlink();

        storage.isAccountLinked(userId, isLinked -> {

            if (!isLinked) {
                event.getHook().sendMessage("You are not linked.").queue();
                return;
            }

            // 1. REMOVE LINK FROM DB
            storage.deleteLink(userId);

            // 2. REMOVE STORED CODE
            storage.deleteCode(userId);

            // 3. ROLE CLEANUP (if enabled)
            if (config.removeRoles()) {
                instance.getRoleSyncService()
                        .removeAllRoles(String.valueOf(userId));
            }

            // 4. BOOST CLEANUP (optional hook)
            if (config.removeBoost()) {
                instance.getBoostService()
                        .removeBoost(userId);
            }

            // 5. RESPONSE
            event.getHook().sendMessage("Successfully unlinked your account.").queue();
        });
    }
}