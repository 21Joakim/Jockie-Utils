package example.command.moderation.channel;

import com.jockie.bot.core.argument.Argument;
import com.jockie.bot.core.command.impl.CommandImpl;

import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandMoveChannel extends CommandImpl {

	public CommandMoveChannel() {
		super("move");
	}
	
	public void onCommand(MessageReceivedEvent event, Channel channel, @Argument(name="category", nullDefault=true) Category category) {
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