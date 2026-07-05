package fr.maxlego08.essentials.bot.link;

import fr.maxlego08.essentials.api.discord.DiscordAction;
import fr.maxlego08.essentials.api.dto.DiscordCodeDTO;
import fr.maxlego08.essentials.bot.DiscordBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.internal.interactions.component.ButtonImpl;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class LinkManager extends ListenerAdapter {

    public static final String BUTTON_LINK_NAME = "zessentials:link";

    private final DiscordBot instance;

    // =========================
    // ANTI-SPAM (NEW)
    // =========================
    private final Map<Long, Long> cooldown = new HashMap<>();

    public LinkManager(DiscordBot instance) {
        this.instance = instance;
    }

    public void sendLinkMessage(MessageChannelUnion textChannel) {

        EmbedBuilder builder = instance.getConfiguration().getLink().embed().toEmbed();

        var config = instance.getConfiguration().getLink().button();

        Button action = new ButtonImpl(
                BUTTON_LINK_NAME,
                config.name(),
                config.style(),
                config.disabled(),
                config.emoji() == null ? null : Emoji.fromUnicode(config.emoji())
        );

        textChannel.sendMessageEmbeds(builder.build())
                .addActionRow(action)
                .queue();
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {

        // FEATURE TOGGLE
        if (!instance.getFeatureManager().isEnabled("link")) {
            event.reply("Link system is disabled.").setEphemeral(true).queue();
            return;
        }

        if (!event.getComponentId().equals(BUTTON_LINK_NAME)) return;

        // =========================
        // ANTI-SPAM CHECK
        // =========================
        long userId = event.getUser().getIdLong();
        long now = System.currentTimeMillis();

        int cooldownSeconds = instance.getConfiguration()
                .getFeatures()
                .antiSpam()
                .cooldownSeconds();

        if (cooldown.containsKey(userId)) {
            long last = cooldown.get(userId);

            if ((now - last) < cooldownSeconds * 1000L) {
                event.reply("Slow down. Please wait before using this again.")
                        .setEphemeral(true)
                        .queue();
                return;
            }
        }

        cooldown.put(userId, now);

        createCode(event, event.getGuild(), event.getUser());
    }

    private void createCode(ButtonInteractionEvent event, Guild guild, User user) {

        var config = instance.getConfiguration().getLink();
        var storage = instance.getStorageManager();
        long userId = user.getIdLong();

        storage.isAccountLinked(userId, isLinked -> {

            if (isLinked) {
                event.reply(config.messages().already()).setEphemeral(true).queue();
                return;
            }

            handleCode(event, guild, user, storage, config);
        });
    }

    private void handleCode(ButtonInteractionEvent event,
                            Guild guild,
                            User user,
                            var storage,
                            var config) {

        long userId = user.getIdLong();
        Optional<DiscordCodeDTO> optional = getCode(userId);

        if (optional.isPresent()) {
            sendExistingCode(event, guild, user, config, optional.get());
            return;
        }

        createNewCode(event, guild, user, storage, config);
    }

    private void sendExistingCode(ButtonInteractionEvent event,
                                  Guild guild,
                                  User user,
                                  var config,
                                  DiscordCodeDTO code) {

        replyCode(code.code(), event);

        instance.getStorageManager().insertLog(
                DiscordAction.ASK_CODE,
                null,
                null,
                user.getEffectiveName(),
                user.getIdLong(),
                code.code()
        );

        log(guild, config.log().channel(), config.log().ask()
                .replace("%name%", user.getName())
                .replace("%code%", code.code())
                .replace("%id%", String.valueOf(user.getIdLong())));
    }

    private void createNewCode(ButtonInteractionEvent event,
                               Guild guild,
                               User user,
                               var storage,
                               var config) {

        String generatedCode = UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 16);

        DiscordCodeDTO newCode = new DiscordCodeDTO(
                generatedCode,
                user.getIdLong(),
                user.getName()
        );

        storage.saveCode(newCode);

        replyCode(generatedCode, event);

        instance.getStorageManager().insertLog(
                DiscordAction.CREATE_CODE,
                null,
                null,
                user.getEffectiveName(),
                user.getIdLong(),
                generatedCode
        );

        log(guild, config.log().channel(), config.log().create()
                .replace("%name%", user.getName())
                .replace("%code%", generatedCode)
                .replace("%id%", String.valueOf(user.getIdLong())));

        // =========================
        // ROLE SYNC HOOK (READY FOR NEXT STEP)
        // =========================
        Member member = event.getMember();
        if (member != null) {
            instance.getRoleSyncService().syncRoles(member, String.valueOf(user.getIdLong()));
        }
    }

    private void log(Guild guild, long channelId, String message) {

        var channel = guild.getTextChannelById(channelId);

        if (channel == null) {
            System.err.println("Channel " + channelId + " not found");
            return;
        }

        channel.sendMessage(message).queue();
    }

    private void replyCode(String code, ButtonInteractionEvent event) {

        var config = instance.getConfiguration().getLink();

        event.reply(config.messages().code().replace("%code%", code))
                .setEphemeral(true)
                .queue();
    }

    private Optional<DiscordCodeDTO> getCode(long userId) {
        return instance.getStorageManager().getCode(userId);
    }
}