package fr.maxlego08.essentials.bot.sync;

import fr.maxlego08.essentials.bot.DiscordBot;
import net.dv8tion.jda.api.entities.Member;

public class RoleSyncService {

    private final DiscordBot instance;

    public RoleSyncService(DiscordBot instance) {
        this.instance = instance;
    }

    public void syncRoles(Member member, String minecraftUUID) {

        var config = instance.getConfiguration().getFeatures().roleSync();

        if (!config.enabled()) return;

        var roles = config.roles();

        var luckPerms = instance.getLuckPermsService();

        // =========================
        // CLEAN EXISTING ROLES FIRST
        // =========================
        luckPerms.removeGroup(minecraftUUID, "vip");
        luckPerms.removeGroup(minecraftUUID, "admin");

        // =========================
        // APPLY NEW ROLES
        // =========================

        boolean hasVip = member.getRoles().stream()
                .anyMatch(r -> r.getId().equals(roles.vip()));

        boolean hasAdmin = member.getRoles().stream()
                .anyMatch(r -> r.getId().equals(roles.admin()));

        if (hasAdmin) {
            luckPerms.addGroup(minecraftUUID, "admin");
        } else if (hasVip) {
            luckPerms.addGroup(minecraftUUID, "vip");
        } else {
            luckPerms.addGroup(minecraftUUID, "default");
        }
    }

    public void removeAllRoles(String minecraftUUID) {

        var config = instance.getConfiguration().getFeatures().roleSync();

        if (!config.enabled()) return;

        var luckPerms = instance.getLuckPermsService();

        luckPerms.removeGroup(minecraftUUID, "vip");
        luckPerms.removeGroup(minecraftUUID, "admin");

        // safe reset only if needed
        luckPerms.addGroup(minecraftUUID, "default");
    }
}