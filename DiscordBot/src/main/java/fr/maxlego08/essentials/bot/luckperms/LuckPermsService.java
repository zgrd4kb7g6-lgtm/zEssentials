package fr.maxlego08.essentials.bot.luckperms;

import fr.maxlego08.essentials.bot.DiscordBot;

import java.util.concurrent.CompletableFuture;

public class LuckPermsService {

    private final DiscordBot instance;

    public LuckPermsService(DiscordBot instance) {
        this.instance = instance;
    }

    // =========================
    // ADD GROUP
    // =========================
    public void addGroup(String minecraftUUID, String group) {

        CompletableFuture.runAsync(() -> {

            // TODO: replace with real LuckPerms API hook on Minecraft server
            System.out.println("[LuckPerms] ADD " + group + " -> " + minecraftUUID);

            // Example real implementation (on MC side):
            // luckPerms.getUserManager().modifyUser(UUID.fromString(minecraftUUID), user -> {
            //     user.data().add(Node.builder("group." + group).build());
            // });
        });
    }

    // =========================
    // REMOVE GROUP
    // =========================
    public void removeGroup(String minecraftUUID, String group) {

        CompletableFuture.runAsync(() -> {

            System.out.println("[LuckPerms] REMOVE " + group + " -> " + minecraftUUID);

            // Example real implementation:
            // luckPerms.getUserManager().modifyUser(UUID.fromString(minecraftUUID), user -> {
            //     user.data().remove(Node.builder("group." + group).build());
            // });
        });
    }
}