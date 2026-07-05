package fr.maxlego08.essentials.bot.sync;

import fr.maxlego08.essentials.bot.DiscordBot;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateBoostTimeEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class BoostListener extends ListenerAdapter {

    private final DiscordBot instance;

    public BoostListener(DiscordBot instance) {
        this.instance = instance;
    }

    @Override
    public void onGuildMemberUpdateBoostTime(@NotNull GuildMemberUpdateBoostTimeEvent event) {

        if (!instance.getFeatureManager().isEnabled("role-sync")) return;

        var config = instance.getConfiguration().getFeatures().roleSync().boost();

        if (!config.enabled()) return;

        long userId = event.getUser().getIdLong();

        // =========================
        // BOOST STARTED
        // =========================
        if (event.getNewValue() != null && event.getOldValue() == null) {

            giveBoostRank(userId);
        }

        // =========================
        // BOOST ENDED
        // =========================
        if (event.getNewValue() == null && event.getOldValue() != null) {

            if (config.removeOnLoss()) {
                removeBoostRank(userId);
            }
        }
    }

    private void giveBoostRank(long userId) {

        String group = instance.getConfiguration()
                .getFeatures()
                .roleSync()
                .boost()
                .minecraftGroup();

        // hook to Minecraft (LuckPerms integration later)
        instance.getRoleSyncService().addGroup(userId, group);
    }

    private void removeBoostRank(long userId) {

        String group = instance.getConfiguration()
                .getFeatures()
                .roleSync()
                .boost()
                .minecraftGroup();

        instance.getRoleSyncService().removeGroup(userId, group);
    }
}