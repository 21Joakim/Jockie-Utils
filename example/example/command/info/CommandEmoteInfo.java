package example.command.info;

import com.jockie.bot.core.argument.Argument;
import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.impl.CommandImpl;

import example.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class CommandEmoteInfo extends CommandImpl {

	public CommandEmoteInfo() {
		super("emote info");
		
		super.setShortDescription("Get information about an emote");
	}
	
	public MessageEmbed onCommand(CommandEvent event, @Argument("emote") Emote emote) {
		return new EmbedBuilder()
			.setThumbnail(emote.getImageUrl())
			.addField("Name", emote.getName(), true)
			.addField("Id", emote.getId(), true)
			.addField("Emote", emote.getAsMention(), true)
			.addField("Animated", String.valueOf(emote.isAnimated()), true)
			.addField("Created", Main.FORMATTER.format(emote.getTimeCreated()), true)
			.build();
	}
}