package example.command.info.emote;

import com.jockie.bot.core.argument.Argument;
import com.jockie.bot.core.command.impl.CommandImpl;

import example.Main;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandEmoteInfo extends CommandImpl {

	public CommandEmoteInfo() {
		super("emote info");
	}
	
	public MessageEmbed onCommand(MessageReceivedEvent event, @Argument(name="emote") Emote emote) {
		EmbedBuilder builder = new EmbedBuilder();
		
		builder.setThumbnail(emote.getImageUrl());
		builder.addField("Name", emote.getName(), true);
		builder.addField("Id", emote.getId(), true);
		builder.addField("Emote", emote.getAsMention(), true);
		builder.addField("Animated", String.valueOf(emote.isAnimated()), true);
		builder.addField("Created", Main.FORMATTER.format(emote.getCreationTime()), true);
		
		return builder.build();
	}
}