package example.command.info.emote;

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
	}
	
	public MessageEmbed onCommand(CommandEvent event, @Argument("emote") Emote emote) {
		EmbedBuilder builder = new EmbedBuilder();
		
		builder.setThumbnail(emote.getImageUrl());
		builder.addField("Name", emote.getName(), true);
		builder.addField("Id", emote.getId(), true);
		builder.addField("Emote", emote.getAsMention(), true);
		builder.addField("Animated", String.valueOf(emote.isAnimated()), true);
		builder.addField("Created", Main.FORMATTER.format(emote.getTimeCreated()), true);
		
		return builder.build();
	}
}