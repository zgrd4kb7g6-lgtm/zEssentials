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