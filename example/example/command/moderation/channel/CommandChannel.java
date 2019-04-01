package example.command.moderation.channel;

import com.jockie.bot.core.argument.Argument;
import com.jockie.bot.core.command.Command;
import com.jockie.bot.core.command.Command.AuthorPermissions;
import com.jockie.bot.core.command.Command.BotPermissions;
import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.impl.CommandImpl;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.TextChannel;

public class CommandChannel extends CommandImpl {

	public CommandChannel() {
		super("channel");
	}
	
	@Command
	@AuthorPermissions(Permission.MANAGE_CHANNEL)
	@BotPermissions(Permission.MANAGE_CHANNEL)
	public void delete(CommandEvent event, @Argument("channel") TextChannel channel) {
		channel.delete().queue($ -> {
			event.reply(String.format("**%s** has been deleted", channel.getName())).queue();
		});
	}
	
	@Command
	@AuthorPermissions(Permission.MANAGE_CHANNEL)
	@BotPermissions(Permission.MANAGE_CHANNEL)
	public void create(CommandEvent event, @Argument("name") String name) {
		event.getGuild().getController().createTextChannel(name).queue(channel -> {
			event.reply(String.format("**%s** has been created", channel.getName())).queue();
		});
	}
}