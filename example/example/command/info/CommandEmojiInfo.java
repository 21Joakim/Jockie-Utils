package example.command.info;

import com.jockie.bot.core.argument.Argument;
import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.impl.CommandImpl;

import example.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;

public class CommandEmojiInfo extends CommandImpl {

	public CommandEmojiInfo() {
		super("emoji info");
		
		super.setShortDescription("Get information about a custom emoji");
	}
	
	public MessageEmbed onCommand(CommandEvent event, @Argument("emoji") CustomEmoji emoji) {
		return new EmbedBuilder()
			.setThumbnail(emoji.getImageUrl())
			.addField("Name", emoji.getName(), true)
			.addField("Id", emoji.getId(), true)
			.addField("Emoji", emoji.getAsMention(), true)
			.addField("Animated", String.valueOf(emoji.isAnimated()), true)
			.addField("Created", Main.FORMATTER.format(emoji.getTimeCreated()), true)
			.build();
	}
}