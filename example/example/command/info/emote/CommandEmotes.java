package example.command.info.emote;

import java.util.List;

import com.jockie.bot.core.command.impl.CommandImpl;
import com.jockie.bot.core.paged.impl.PagedManager;
import com.jockie.bot.core.paged.impl.PagedResult;

import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandEmotes extends CommandImpl {

	public CommandEmotes() {
		super("emotes");
	}
	
	public void onCommand(MessageReceivedEvent event) {
		List<Emote> emotes = event.getGuild().getEmotes();
		
		if(emotes.size() > 0) {
			PagedResult<Emote> paged = new PagedResult<Emote>(emotes, emote -> emote.getAsMention() + " - " + emote.getName());
			
			PagedManager.addPagedResult(event, paged);
		}else{
			event.getChannel().sendMessage("There are no emotes on this server").queue();
		}
	}
}