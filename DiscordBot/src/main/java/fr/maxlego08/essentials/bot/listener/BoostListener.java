package fr.maxlego08.essentials.bot.sync;

import fr.maxlego08.essentials.bot.DiscordBot;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateRoleRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class BoostListener extends ListenerAdapter {

    private final DiscordBot instance;

    public BoostListener(DiscordBot instance) {
        this.instance = instance;
    }

    @Override
    public void onGuildMemberUpdateRoleAdd(GuildMemberUpdateRoleAddEvent event) {

        var config = instance.getConfiguration().getFeatures().roleSync().boost();

        if (!config.enabled()) return;

        String boosterRoleId = config.discordRoleId();

        boolean hasBoostRole = event.getRoles().stream()
                .anyMatch(role -> role.getId().equals(boosterRoleId));

        if (!hasBoostRole) return;

        long discordId = event.getUser().getIdLong();

        instance.getStorageManager().getMinecraftUUID(discordId, uuid -> {

            if (uuid == null) return;

            instance.getLuckPermsService().addGroup(uuid, config.minecraftGroup());
        });
    }

    @Override
    public void onGuildMemberUpdateRoleRemove(GuildMemberUpdateRoleRemoveEvent event) {

        var config = instance.getConfiguration().getFeatures().roleSync().boost();

        if (!config.enabled()) return;

        String boosterRoleId = config.discordRoleId();

        boolean lostBoostRole = event.getRoles().stream()
                .anyMatch(role -> role.getId().equals(boosterRoleId));

        if (!lostBoostRole) return;

        if (!config.removeOnLoss()) return;

        long discordId = event.getUser().getIdLong();

        instance.getStorageManager().getMinecraftUUID(discordId, uuid -> {

            if (uuid == null) return;

            instance.getLuckPermsService().removeGroup(uuid, config.minecraftGroup());
        });
    }
}