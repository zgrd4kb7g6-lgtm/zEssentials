package fr.maxlego08.essentials.bot.sync;

import fr.maxlego08.essentials.bot.DiscordBot;
import net.dv8tion.jda.api.entities.Member;

import java.util.Map;

public class RoleSyncService {

    private final DiscordBot instance;

    public RoleSyncService(DiscordBot instance) {
        this.instance = instance;
    }

    public void syncRoles(Member member, String minecraftUUID) {

        var config = instance.getConfiguration().getFeatures().roleSync();

        if (!config.enabled()) return;

        var roles = config.roles();

        // VIP
        if (member.getRoles().stream()
                .anyMatch(r -> r.getId().equals(roles.vip()))) {

            instance.getLuckPermsService().addGroup(
                    minecraftUUID,
                    "vip"
            );
        }

        // ADMIN
        if (member.getRoles().stream()
                .anyMatch(r -> r.getId().equals(roles.admin()))) {

            instance.getLuckPermsService().addGroup(
                    minecraftUUID,
                    "admin"
            );
        }

        // DEFAULT fallback
        instance.getLuckPermsService().addGroup(
                minecraftUUID,
                "default"
        );
    }

    public void removeAllRoles(String minecraftUUID) {

        var config = instance.getConfiguration().getFeatures().roleSync();

        if (!config.enabled()) return;

        var roles = config.roles();

        instance.getLuckPermsService().removeGroup(minecraftUUID, "vip");
        instance.getLuckPermsService().removeGroup(minecraftUUID, "admin");

        // optional default reset
        instance.getLuckPermsService().addGroup(minecraftUUID, "default");
    }
}