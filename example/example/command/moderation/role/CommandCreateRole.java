package example.command.moderation.role;

import java.awt.Color;

import com.jockie.bot.core.command.argument.Argument;
import com.jockie.bot.core.command.impl.CommandImpl;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandCreateRole extends CommandImpl {
	
	public CommandCreateRole() {
		super("create role");
		
		super.setDescription("Create a role");
		super.setBotDiscordPermissionsNeeded(Permission.MANAGE_ROLES);
		super.setAuthorDiscordPermissionsNeeded(Permission.MANAGE_ROLES);
	}
	
	public void onCommand(MessageReceivedEvent event, @Argument(name="name") String name,
			@Argument(name="color", nullDefault=true) Color color,
			@Argument(name="permissions", nullDefault=true) Long permissions) {
		
		event.getGuild().getController().createRole().setName(name).setColor(color).setPermissions(permissions).queue(role -> {
			event.getChannel().sendMessage(role.getAsMention() + " has been created").queue();
		}, exception -> {
			event.getChannel().sendMessage("Something went wrong: " + exception.getMessage()).queue();
		});
	}
}