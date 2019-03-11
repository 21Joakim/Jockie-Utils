package example.command.moderation.collection;

import java.awt.Color;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import com.jockie.bot.core.argument.Argument;
import com.jockie.bot.core.command.Command;
import com.jockie.bot.core.command.annotation.Cooldown;
import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.impl.CommandImpl;
import com.jockie.bot.core.cooldown.ICooldown.Scope;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.entities.Message.Attachment;

public class CommandCreate extends CommandImpl {
	
	public CommandCreate() {
		super("create");
	}
	
	@Command(botPermissions=Permission.MANAGE_CHANNEL, authorPermissions=Permission.MANAGE_CHANNEL)
	public void text(CommandEvent event, @Argument("name") String name) {
		event.getGuild().getController().createTextChannel(name).queue(channel -> {
			event.reply("Created text channel with the name " + channel.getName()).queue();
		});
	}
	
	@Command(botPermissions=Permission.MANAGE_CHANNEL, authorPermissions=Permission.MANAGE_CHANNEL)
	public void voice(CommandEvent event, @Argument("name") String name) {
		event.getGuild().getController().createTextChannel(name).queue(channel -> {
			event.reply("Created voice channel with the name " + channel.getName()).queue();
		});
	}
	
	@Command(botPermissions=Permission.MANAGE_CHANNEL, authorPermissions=Permission.MANAGE_CHANNEL)
	public void category(CommandEvent event, @Argument("name") String name) {
		event.getGuild().getController().createCategory(name).queue(category -> {
			event.reply("Created category with the name " + category.getName()).queue();
		});
	}
	
	@Command(botPermissions=Permission.MANAGE_ROLES, authorPermissions=Permission.MANAGE_ROLES)
	public void role(CommandEvent event, @Argument("name") String name,
		@Argument(value="color", nullDefault=true) Color color,
		@Argument(value="permissions", nullDefault=true) Long permissions) {
		
		event.getGuild().getController().createRole().setName(name).setColor(color).setPermissions(permissions).queue(role -> {
			event.reply(role.getAsMention() + " has been created").queue();
		});
	}
	
	@Cooldown(cooldown=30, cooldownScope=Scope.GUILD)
	@Command(botPermissions=Permission.MANAGE_EMOTES, authorPermissions=Permission.MANAGE_EMOTES)
	public void emote(CommandEvent event, String name, @Argument(value="emoteUrl", nullDefault=true) URL emoteUrl) {
		if(emoteUrl == null) {
			List<Attachment> attachments = event.getMessage().getAttachments();
			if(attachments.size() > 0) {
				try {
					emoteUrl = new URL(attachments.get(0).getUrl());
				}catch(MalformedURLException unlikely) {
					return;
				}
			}else{
				event.reply("No url nor attachment was provided").queue();
				
				return;
			}
		}
		
		try {
			event.getGuild().getController().createEmote(name, Icon.from(emoteUrl.openStream())).queue(emote -> {
				event.reply(emote.getAsMention() + " has been created").queue();
			}, failure -> {
				event.reply("Ops, that might not be an image or there are too many emotes on this server already!").queue();
			});
		}catch(IOException e) {
			event.reply("Something went wrong when accessing the url").queue();
		}
	}
}