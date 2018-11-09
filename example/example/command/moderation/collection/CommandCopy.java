package example.command.moderation.collection;

import java.io.IOException;
import java.net.URL;

import com.jockie.bot.core.argument.Argument;
import com.jockie.bot.core.command.Command;
import com.jockie.bot.core.command.impl.CommandImpl;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandCopy extends CommandImpl {
	
	public CommandCopy() {
		super("copy");
	}
	
	@Command(command="role", authorPermissionsNeeded=Permission.MANAGE_ROLES, botPermissionsNeeded=Permission.MANAGE_ROLES)
	public void onCommand(MessageReceivedEvent event, @Argument(name="role") Role role) {
		role.createCopy().queue(copy -> {
			event.getChannel().sendMessage("Created copy of role " + role.getName()).queue();
		});
	}
	
	@Command(command="channel", authorPermissionsNeeded=Permission.MANAGE_CHANNEL, botPermissionsNeeded=Permission.MANAGE_CHANNEL)
	public void onCommand(MessageReceivedEvent event, @Argument(name="channel") Channel channel) {
		channel.createCopy().queue(copy -> {
			event.getChannel().sendMessage("Created copy of channel " + channel.getName()).queue();
		});
	}
	
	@Command(command="emote", authorPermissionsNeeded=Permission.MANAGE_EMOTES, botPermissionsNeeded=Permission.MANAGE_EMOTES)
	public void onCommand(MessageReceivedEvent event, @Argument(name="emote") Emote emote) {
		try {
			event.getGuild().getController().createEmote(emote.getName(), Icon.from(new URL(emote.getImageUrl()).openStream())).queue(copy -> {
				event.getChannel().sendMessage("Created copy of " + emote.getName() + " " + copy.getAsMention()).queue();
			}, failure -> {
				event.getChannel().sendMessage("Ops, that might not be an image or there are too many emotes on this server already!").queue();
			});
		}catch(IOException e) {
			event.getChannel().sendMessage("Something went wrong when accessing the url").queue();
		}
	}
}