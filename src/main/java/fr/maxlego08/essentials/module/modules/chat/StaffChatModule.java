package fr.maxlego08.essentials.chat.staff;

import fr.maxlego08.essentials.chat.ChatManager;
import fr.maxlego08.essentials.chat.ChatMessage;
import fr.maxlego08.essentials.chat.ChatType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class StaffChatModule {

    private final ChatManager chatManager;

    public StaffChatModule(ChatManager chatManager) {
        this.chatManager = chatManager;
    }

    // =========================
    // MAIN ENTRY POINT (/sc)
    // =========================
    public void send(Player player, String message) {

        if (message == null || message.isEmpty()) return;

        ChatMessage chatMessage = new ChatMessage(
                player,
                message,
                ChatType.STAFF
        );

        handle(chatMessage);
    }

    // =========================
    // CORE HANDLER
    // =========================
    private void handle(ChatMessage message) {

        if (message.getType() != ChatType.STAFF) return;

        Player sender = message.getPlayer();

        String formatted = format(sender.getName(), message.getMessage());

        // Send to all staff online
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("zessentials.staffchat")) {
                player.sendMessage(formatted);
            }
        }

        // Send into main chat system (optional logging hook)
        chatManager.handle(message);

        // Discord bridge hook (YOU already have bot for this)
        chatManager.getDiscordBridge().sendStaffMessage(
                sender.getName(),
                formatted
        );
    }

    // =========================
    // FORMAT
    // =========================
    private String format(String player, String message) {
        return "[STAFF] " + player + " » " + message;
    }
}