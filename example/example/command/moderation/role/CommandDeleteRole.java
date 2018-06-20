package example.command.moderation.role;

import com.jockie.bot.core.command.argument.Argument;
import com.jockie.bot.core.command.impl.CommandImpl;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandDeleteRole extends CommandImpl {

	public CommandDeleteRole() {
		super("delete role");
		
		super.setDescription("Delete a role");
		super.setBotDiscordPermissionsNeeded(Permission.MANAGE_ROLES);
		super.setAuthorDiscordPermissionsNeeded(Permission.MANAGE_ROLES);
	}
	
	public void onCommand(MessageReceivedEvent event, @Argument(name="role", endless=true) Role role) {
		if(event.getMember().canInteract(role)) {
			if(event.getGuild().getSelfMember().canInteract(role)) {
				role.delete().queue(success -> {
					event.getChannel().sendMessage(role.getName() + " has been deleted").queue();
				});
			}else{
				event.getChannel().sendMessage("I can not interact with that role").queue();
			}
		}else{
			event.getChannel().sendMessage("You can not interact with that role").queue();
		}
	}
}