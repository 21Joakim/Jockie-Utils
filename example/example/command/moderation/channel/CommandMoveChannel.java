package example.command.moderation.channel;

import java.util.Optional;

import com.jockie.bot.core.argument.Argument;
import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.impl.CommandImpl;

import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.ICategorizableChannel;

public class CommandMoveChannel extends CommandImpl {

	public CommandMoveChannel() {
		super("move");
	}
	
	public <T extends GuildChannel & ICategorizableChannel> void onCommand(CommandEvent event, T channel, @Argument(value="category") Optional<Category> optionalCategory) {
		Category category = optionalCategory.orElse(null);
		Category parent = channel.getParentCategory();
		
		if(parent == category) {
			if(category == null) {
				event.replyFormat("%s is already not under any category").queue();
			}else{
				event.replyFormat("%s is already under %s", channel.getName(), category.getName()).queue();
			}
			
			return;
		}
		
		channel.getManager().setParent(category).queue((success) -> {
			String message = channel.getName() + " was moved";
			
			if(parent != null) {
				message += " from " + parent.getName();
			}
			
			if(category != null) {
				message += " to " + category.getName();
			}
			
			event.reply(message).queue();
		});
	}
}