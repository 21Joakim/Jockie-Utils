package example.command.info;

import com.jockie.bot.core.argument.Argument;
import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.impl.CommandImpl;

import example.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;

public class CommandRoleInfo extends CommandImpl {
	
	public CommandRoleInfo() {
		super("role info");
		
		super.setAliases("roleinfo", "rolei", "ri");
		super.setDescription("Get information about a role");
	}
	
	public MessageEmbed onCommand(CommandEvent event, @Argument("Role") Role role) {
		return new EmbedBuilder()
			.setColor(role.getColor())
			.addField("Name", role.getName(), true)
			.addField("Id", role.getName(), true)
			.addBlankField(true)
			.addField("Mention", role.getAsMention(), true)
			.addField("Raw Permissions", String.valueOf(role.getPermissionsRaw()), true)
			.addBlankField(true)
			.addField("Created", Main.FORMATTER.format(role.getTimeCreated()), true)
			.addBlankField(true)
			.addBlankField(true)
			.build();
	}
}