package example.command.info.user;

import java.awt.Color;

import com.jockie.bot.core.command.argument.Argument;
import com.jockie.bot.core.command.impl.CommandImpl;
import com.jockie.bot.core.utility.ArgumentUtility;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandUserInfo extends CommandImpl {
	
	public CommandUserInfo() {
		super("user info");
		
		super.setDescription("Get user information");
	}
	
	public void onCommand(MessageReceivedEvent event, @Argument(name="user", nullDefault=true) String string) {
		if(string == null) {
			event.getChannel().sendMessage(getUserInfo(event.getAuthor())).queue();
		}else{
			ArgumentUtility.retrieveUser(event.getJDA(), string).queue(user -> {
				if(user != null) {
					event.getChannel().sendMessage(getUserInfo(user)).queue();
				}else{
					event.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Invalid user").build()).queue();
				}
			});
		}
	}
	
	private static MessageEmbed getUserInfo(User user) {
		EmbedBuilder builder = new EmbedBuilder();
		builder.setThumbnail(user.getAvatarUrl());
		
		return builder.build();
	}
}