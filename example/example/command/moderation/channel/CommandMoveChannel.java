package example.command.moderation.channel;

import java.util.Optional;

import com.jockie.bot.core.argument.Argument;
import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.impl.CommandImpl;

import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.entities.Channel;

public class CommandMoveChannel extends CommandImpl {

	public CommandMoveChannel() {
		super("move");
	}
	
	public void onCommand(CommandEvent event, Channel channel, @Argument(value="category") Optional<Category> optionalCategory) {
		Category category = optionalCategory.orElse(null);
		Category parent = channel.getParent();
		
		if(parent == null || !parent.equals(category)) {
			channel.getManager().setParent(category).queue(success -> {
				String message = channel.getName() + " was moved";
				
				if(parent != null) {
					message += " from " + parent.getName();
				}
				
				if(category != null) {
					message += " to " + category.getName();
				}
				
				event.getChannel().sendMessage(message).queue();
			});
		}else{
			event.getChannel().sendMessage(channel.getName() + " is already under " + category.getName()).queue();
		}
	}
}