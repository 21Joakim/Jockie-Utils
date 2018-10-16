package example.command.moderation.collection;

import java.awt.Color;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import com.jockie.bot.core.argument.Argument;
import com.jockie.bot.core.command.Command;
import com.jockie.bot.core.command.impl.CommandImpl;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.entities.Message.Attachment;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandCreate extends CommandImpl {
	
	public CommandCreate() {
		super("create");
	}
	
	@Command(botPermissionsNeeded=Permission.MANAGE_CHANNEL, authorPermissionsNeeded=Permission.MANAGE_CHANNEL)
	public void text(MessageReceivedEvent event, String name) {
		event.getGuild().getController().createTextChannel(name).queue(channel -> {
			event.getChannel().sendMessage("Created text channel with the name " + channel.getName()).queue();
		});
	}
	
	@Command(botPermissionsNeeded=Permission.MANAGE_CHANNEL, authorPermissionsNeeded=Permission.MANAGE_CHANNEL)
	public void voice(MessageReceivedEvent event, String name) {
		event.getGuild().getController().createTextChannel(name).queue(channel -> {
			event.getChannel().sendMessage("Created voice channel with the name " + channel.getName()).queue();
		});
	}
	
	@Command(botPermissionsNeeded=Permission.MANAGE_CHANNEL, authorPermissionsNeeded=Permission.MANAGE_CHANNEL)
	public void category(MessageReceivedEvent event, String name) {
		event.getGuild().getController().createCategory(name).queue(category -> {
			event.getChannel().sendMessage("Created category with the name " + category.getName()).queue();
		});
	}
	
	@Command(botPermissionsNeeded=Permission.MANAGE_ROLES, authorPermissionsNeeded=Permission.MANAGE_ROLES)
	public void role(MessageReceivedEvent event, @Argument(name="name") String name,
		@Argument(name="color", nullDefault=true) Color color,
		@Argument(name="permissions", nullDefault=true) Long permissions) {
		
		event.getGuild().getController().createRole().setName(name).setColor(color).setPermissions(permissions).queue(role -> {
			event.getChannel().sendMessage(role.getAsMention() + " has been created").queue();
		});
	}
	
	@Command(botPermissionsNeeded=Permission.MANAGE_EMOTES, authorPermissionsNeeded=Permission.MANAGE_EMOTES)
	public void emote(MessageReceivedEvent event, String name, @Argument(name="emoteUrl", nullDefault=true) URL emoteUrl) {
		if(emoteUrl == null) {
			List<Attachment> attachments = event.getMessage().getAttachments();
			if(attachments.size() > 0) {
				try {
					emoteUrl = new URL(attachments.get(0).getUrl());
				}catch(MalformedURLException unlikely) {
					return;
				}
			}else{
				event.getChannel().sendMessage("No url nor attachment was provided").queue();
				
				return;
			}
		}
		
		try {
			event.getGuild().getController().createEmote(name, Icon.from(emoteUrl.openStream())).queue(emote -> {
				event.getChannel().sendMessage(emote.getAsMention() + " has been created").queue();
			}, failure -> {
				event.getChannel().sendMessage("Ops, that might not be an image or there are too many emotes on this server already!").queue();
			});
		}catch(IOException e) {
			event.getChannel().sendMessage("Something went wrong when accessing the url").queue();
		}
	}
}