package example.command.fun;

import com.jockie.bot.core.command.argument.impl.ArgumentFactory;
import com.jockie.bot.core.command.impl.CommandImpl;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandAvatar extends CommandImpl {

	public CommandAvatar() {
		/* Need to figure out a way of making this more convenient (When using optional arguments) (Setting a default value will make the argument optional) */
		super("avatar", ArgumentFactory.of(User.class).setName("user").setDefaultValue(event -> event.getAuthor()).build());
		
		super.setDescription("Get the avatar of a user");
	}
	
	public void onCommand(MessageReceivedEvent event, User user) {
		event.getChannel().sendMessage(new EmbedBuilder().setImage(user.getAvatarUrl()).build()).queue();
	}
}
