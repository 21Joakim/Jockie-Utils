package com.jockie.bot.example.command.arguments.moderation;

import com.jockie.bot.core.command.argument.Argument;
import com.jockie.bot.core.command.impl.CommandImpl;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandBan extends CommandImpl {
	
	public CommandBan() {
		super("ban");
		
		super.setDescription("Ban a user");
		super.setAuthorDiscordPermissionsNeeded(Permission.BAN_MEMBERS);
		super.setBotDiscordPermissionsNeeded(Permission.BAN_MEMBERS);
	}
	
	public void onCommand(MessageReceivedEvent event, @Argument(description="User") User user, @Argument(description="Reason", nullDefault=true, endless=true) String reason) {
		if(event.getGuild().isMember(user)) {
			Member member = event.getGuild().getMember(user);
			
			if(event.getMember().canInteract(member)) {
				if(event.getGuild().getSelfMember().canInteract(member)) {
					event.getGuild().getController().ban(member, 0, reason).queue(success -> {
						event.getChannel().sendMessage("**" + user.getName() + "#" + user.getDiscriminator() + "** has been banned").queue();
					});
				}else{
					event.getChannel().sendMessage("I can not interact with that member").queue();
				}
			}else{
				event.getChannel().sendMessage("You can not interact with that member").queue();
			}
		}else{
			event.getGuild().getController().ban(user, 0, reason).queue(success -> {
				event.getChannel().sendMessage("**" + user.getName() + "#" + user.getDiscriminator() + "** has been banned").queue();
			});
		}
	}
}