package example.command.info.emote;

import com.jockie.bot.core.command.argument.Argument;
import com.jockie.bot.core.command.impl.CommandImpl;
import com.jockie.bot.core.paged.impl.PagedManager;
import com.jockie.bot.core.paged.impl.PagedResult;

import example.Main;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandEmoteInfo extends CommandImpl {

	public CommandEmoteInfo() {
		super("emote info");
	}
	
	public void onCommand(MessageReceivedEvent event, @Argument(name="emote", nullDefault=true) Emote emote) {
		if(emote != null) {
			event.getChannel().sendMessage(getEmoteInfo(emote)).queue();
		}else{
			PagedResult<Emote> paged = new PagedResult<Emote>(event.getGuild().getEmotes(), e -> e.getAsMention() + " - " + e.getName(), selectEvent -> {
				event.getChannel().sendMessage(getEmoteInfo(selectEvent.entry)).queue();
			});
			
			paged.setListIndexesContinuously(true);
			paged.setDeleteOnTimeout(true);
			
			PagedManager.addPagedResult(event, paged);
		}
	}
	
	private static MessageEmbed getEmoteInfo(Emote emote) {
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