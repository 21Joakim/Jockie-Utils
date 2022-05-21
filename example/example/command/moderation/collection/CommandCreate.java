package example.command.moderation.collection;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import com.jockie.bot.core.argument.Argument;
import com.jockie.bot.core.command.Command;
import com.jockie.bot.core.command.Command.Cooldown;
import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.impl.CommandImpl;
import com.jockie.bot.core.cooldown.ICooldown.Scope;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Message.Attachment;

public class CommandCreate extends CommandImpl {
	
	public CommandCreate() {
		super("create");
	}
	
	@Command(botPermissions=Permission.MANAGE_CHANNEL, authorPermissions=Permission.MANAGE_CHANNEL)
	public void text(CommandEvent event, @Argument("name") String name) {
		event.getGuild().createTextChannel(name).flatMap((channel) -> event.replyFormat("Created text channel with the name %s", channel.getName())).queue();
	}
	
	@Command(botPermissions=Permission.MANAGE_CHANNEL, authorPermissions=Permission.MANAGE_CHANNEL)
	public void voice(CommandEvent event, @Argument("name") String name) {
		event.getGuild().createVoiceChannel(name).flatMap((channel) -> event.replyFormat("Created voice channel with the name %s", channel.getName())).queue();
	}
	
	@Command(botPermissions=Permission.MANAGE_CHANNEL, authorPermissions=Permission.MANAGE_CHANNEL)
	public void category(CommandEvent event, @Argument("name") String name) {
		event.getGuild().createCategory(name).flatMap((category) -> event.replyFormat("Created category with the name %s", category.getName())).queue();
	}
	
	@Command(botPermissions=Permission.MANAGE_ROLES, authorPermissions=Permission.MANAGE_ROLES)
	public void role(CommandEvent event, @Argument("name") String name,
		@Argument(value="color", nullDefault=true) Color color,
		@Argument(value="permissions", nullDefault=true) Long permissions) {
		
		event.getGuild().createRole()
			.setName(name)
			.setColor(color)
			.setPermissions(permissions)
			.flatMap((role) -> event.replyFormat("%s has been created", role.getAsMention()))
			.queue();
	}
	
	@Cooldown(value=30, cooldownScope=Scope.GUILD)
	@Command(botPermissions=Permission.MANAGE_EMOTES_AND_STICKERS, authorPermissions=Permission.MANAGE_EMOTES_AND_STICKERS)
	public void emote(CommandEvent event, String name, @Argument(value="emoteUrl", nullDefault=true) URL emoteUrl) {
		if(emoteUrl == null) {
			List<Attachment> attachments = event.getMessage().getAttachments();
			if(attachments.isEmpty()) {
				event.reply("No url or attachment was provided").queue();
				
				return;
			}
			
			try {
				emoteUrl = new URL(attachments.get(0).getUrl());
			}catch(MalformedURLException unlikely) {
				event.reply("Something went wrong").queue();
				
				return;
			}
		}
		
		Icon icon;
		try {
			try(InputStream stream = emoteUrl.openStream()) {
				icon = Icon.from(stream);
			}
		}catch(IOException e) {
			event.reply("Something went wrong when accessing the url").queue();
			
			return;
		}
		
		event.getGuild().createEmote(name, icon)
			.flatMap((emote) -> event.replyFormat("%s has been created", emote.getAsMention()))
			.onErrorFlatMap((failure) -> event.reply("Ops, that might not be an image or there are too many emotes on this server already!"))
			.queue();
	}
}