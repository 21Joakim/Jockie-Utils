package example.command.info.user;

import java.util.Optional;

import com.jockie.bot.core.argument.Argument;
import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.impl.CommandImpl;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

public class CommandAvatar extends CommandImpl {

	public CommandAvatar() {
		super("avatar");
		
		super.setDescription("Get the avatar of a user");
	}
	
	public MessageEmbed onCommand(CommandEvent event, @Argument("user") Optional<User> user) {
		return new EmbedBuilder().setImage(user.orElse(event.getAuthor()).getAvatarUrl()).build();
	}
}
