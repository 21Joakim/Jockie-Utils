package example.command.moderation.role;

import com.jockie.bot.core.argument.Argument;
import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.impl.CommandImpl;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;

public class CommandDeleteRole extends CommandImpl {

	public CommandDeleteRole() {
		super("delete role");
		
		super.setDescription("Delete a role");
		super.setBotDiscordPermissions(Permission.MANAGE_ROLES);
		super.setAuthorDiscordPermissions(Permission.MANAGE_ROLES);
	}
	
	public void onCommand(CommandEvent event, @Argument(value="role", endless=true) Role role) {
		if(!event.getMember().canInteract(role)) {
			event.reply("You can not interact with that role").queue();
			
			return;
		}
		
		if(!event.getGuild().getSelfMember().canInteract(role)) {
			event.reply("I can not interact with that role").queue();
			
			return;
		}
		
		role.delete().flatMap((success) -> event.replyFormat("%s has been deleted", role.getName())).queue();
	}
}