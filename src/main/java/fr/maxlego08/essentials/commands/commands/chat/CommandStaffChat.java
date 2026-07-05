package fr.maxlego08.essentials.commands.commands.chat;

import fr.maxlego08.essentials.api.EssentialsPlugin;
import fr.maxlego08.essentials.api.commands.CommandResultType;
import fr.maxlego08.essentials.api.commands.Permission;
import fr.maxlego08.essentials.api.messages.Message;
import fr.maxlego08.essentials.api.user.Option;
import fr.maxlego08.essentials.api.user.User;
import fr.maxlego08.essentials.module.modules.chat.ChatModule;
import fr.maxlego08.essentials.zutils.utils.commands.VCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandStaffChat extends VCommand {

    public CommandStaffChat(EssentialsPlugin plugin) {
        super(plugin);

        this.setModule(ChatModule.class);
        this.setPermission(Permission.ESSENTIALS_STAFFCHAT);
        this.setDescription(Message.DESCRIPTION_STAFFCHAT);
        this.addAlias("sc");
        this.addOptionalArg("player");
    }

    @Override
    protected CommandResultType perform(EssentialsPlugin plugin) {

        Player player = this.argAsPlayer(0, this.player);

        if (player == null) {
            return CommandResultType.SYNTAX_ERROR;
        }

        if (player.equals(this.player) || !hasPermission(sender, Permission.ESSENTIALS_STAFFCHAT_OTHER)) {
            toggleStaffChat(player, this.user, sender);
        } else {
            User otherUser = getUser(player);
            if (otherUser == null) {
                return CommandResultType.SYNTAX_ERROR;
            }

            toggleStaffChat(player, otherUser, sender);
        }

        return CommandResultType.SUCCESS;
    }

    private void toggleStaffChat(Player player, User user, CommandSender sender) {

        user.setOption(Option.STAFF_CHAT, !user.getOption(Option.STAFF_CHAT));

        boolean enabled = user.getOption(Option.STAFF_CHAT);

        Message message = enabled ?
                Message.COMMAND_STAFFCHAT_ENABLE :
                Message.COMMAND_STAFFCHAT_DISABLE;

        message(sender, message,
                "%player%",
                user == this.user ? Message.YOU.getMessageAsString() : player.getName());
    }
}