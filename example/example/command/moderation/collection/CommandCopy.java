package example.command.moderation.collection;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import com.jockie.bot.core.argument.Argument;
import com.jockie.bot.core.command.Command;
import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.impl.CommandImpl;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.attribute.ICopyableChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;

public class CommandCopy extends CommandImpl {
	
	public CommandCopy() {
		super("copy");
	}
	
	@Command(value="role", authorPermissions=Permission.MANAGE_ROLES, botPermissions=Permission.MANAGE_ROLES)
	public void onCommand(CommandEvent event, @Argument("role") Role role) {
		role.createCopy().flatMap((copy) -> event.replyFormat("Created copy of role %s", role.getName())).queue();
	}
	
	@Command(value="channel", authorPermissions=Permission.MANAGE_CHANNEL, botPermissions=Permission.MANAGE_CHANNEL)
	public <T extends GuildChannel & ICopyableChannel> void onCommand(CommandEvent event, @Argument("channel") T channel) {
		channel.createCopy().flatMap((copy) -> event.replyFormat("Created copy of channel %s", channel.getName())).queue();
	}
	
	@Command(value="emoji", authorPermissions=Permission.MANAGE_EMOJIS_AND_STICKERS, botPermissions=Permission.MANAGE_EMOJIS_AND_STICKERS)
	public void onCommand(CommandEvent event, @Argument("emoji") CustomEmoji emoji) {
		Icon icon;
		try {
			try(InputStream stream = new URL(emoji.getImageUrl()).openStream()) {
				icon = Icon.from(stream);
			}
		}catch(IOException e) {
			event.reply("Something went wrong when accessing the url").queue();
			
			return;
		}
		
		event.getGuild().createEmoji(emoji.getName(), icon)
			.flatMap((copy) -> event.replyFormat("Created copy of %s (%s)", emoji.getName(), copy.getAsMention()))
			.onErrorFlatMap((failure) -> event.reply("Ops, that might not be an image or there are too many emojis in this server already!"))
			.queue();
	}
}