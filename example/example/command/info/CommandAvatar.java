package example.command.info;

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
		User effectiveUser = user.orElse(event.getAuthor());
		
		return new EmbedBuilder()
			.setAuthor(effectiveUser.getAsTag(), null, effectiveUser.getEffectiveAvatarUrl())
			.setImage(effectiveUser.getEffectiveAvatarUrl())
			.build();
	}
}
