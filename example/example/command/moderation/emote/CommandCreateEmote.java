package example.command.moderation.emote;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import com.jockie.bot.core.command.argument.Argument;
import com.jockie.bot.core.command.impl.CommandImpl;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.entities.Message.Attachment;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandCreateEmote extends CommandImpl {
	
	public CommandCreateEmote() {
		super("create emote");
		
		super.setDescription("create emote");
		super.setBotDiscordPermissionsNeeded(Permission.MANAGE_EMOTES);
		super.setAuthorDiscordPermissionsNeeded(Permission.MANAGE_EMOTES);
	}
	
	public void onCommand(MessageReceivedEvent event, @Argument(name="name") String name, @Argument(name="emoteUrl", nullDefault=true) URL emoteUrl) {
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