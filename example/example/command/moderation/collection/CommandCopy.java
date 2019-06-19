package example.command.moderation.collection;

import java.io.IOException;
import java.net.URL;

import com.jockie.bot.core.argument.Argument;
import com.jockie.bot.core.command.Command;
import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.impl.CommandImpl;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Role;

public class CommandCopy extends CommandImpl {
	
	public CommandCopy() {
		super("copy");
	}
	
	@Command(value="role", authorPermissions=Permission.MANAGE_ROLES, botPermissions=Permission.MANAGE_ROLES)
	public void onCommand(CommandEvent event, @Argument("role") Role role) {
		role.createCopy().queue(copy -> {
			event.reply("Created copy of role " + role.getName()).queue();
		});
	}
	
	@Command(value="channel", authorPermissions=Permission.MANAGE_CHANNEL, botPermissions=Permission.MANAGE_CHANNEL)
	public void onCommand(CommandEvent event, @Argument("channel") GuildChannel channel) {
		channel.createCopy().queue(copy -> {
			event.reply("Created copy of channel " + channel.getName()).queue();
		});
	}
	
	@Command(value="emote", authorPermissions=Permission.MANAGE_EMOTES, botPermissions=Permission.MANAGE_EMOTES)
	public void onCommand(CommandEvent event, @Argument("emote") Emote emote) {
		try {
			event.getGuild().createEmote(emote.getName(), Icon.from(new URL(emote.getImageUrl()).openStream())).queue(copy -> {
				event.reply("Created copy of " + emote.getName() + " " + copy.getAsMention()).queue();
			}, failure -> {
				event.reply("Ops, that might not be an image or there are too many emotes on this server already!").queue();
			});
		}catch(IOException e) {
			event.reply("Something went wrong when accessing the url").queue();
		}
	}
}