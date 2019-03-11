package example.command.moderation.role;

import com.jockie.bot.core.argument.Argument;
import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.impl.CommandImpl;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Role;

public class CommandDeleteRole extends CommandImpl {

	public CommandDeleteRole() {
		super("delete role");
		
		super.setDescription("Delete a role");
		super.setBotDiscordPermissions(Permission.MANAGE_ROLES);
		super.setAuthorDiscordPermissions(Permission.MANAGE_ROLES);
	}
	
	public void onCommand(CommandEvent event, @Argument(value="role", endless=true) Role role) {
		if(event.getMember().canInteract(role)) {
			if(event.getGuild().getSelfMember().canInteract(role)) {
				role.delete().queue(success -> {
					event.reply(role.getName() + " has been deleted").queue();
				});
			}else{
				event.reply("I can not interact with that role").queue();
			}
		}else{
			event.reply("You can not interact with that role").queue();
		}
	}
}