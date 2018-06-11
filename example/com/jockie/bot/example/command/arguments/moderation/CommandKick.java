package com.jockie.bot.example.command.arguments.moderation;

import com.jockie.bot.core.command.argument.Argument;
import com.jockie.bot.core.command.impl.CommandImpl;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandKick extends CommandImpl {

	public CommandKick() {
		super("kick");
		
		super.setDescription("Kick a member");
		super.setAuthorDiscordPermissionsNeeded(Permission.KICK_MEMBERS);
		super.setBotDiscordPermissionsNeeded(Permission.KICK_MEMBERS);
	}
	
	public void onCommand(MessageReceivedEvent event, @Argument(description="Member") Member member, @Argument(description="Reason", nullDefault=true, endless=true) String reason) {
		if(event.getMember().canInteract(member)) {
			if(event.getGuild().getSelfMember().canInteract(member)) {
				event.getGuild().getController().kick(member, reason).queue(success -> {
					User user = member.getUser();
					
					event.getChannel().sendMessage("**" + user.getName() + "#" + user.getDiscriminator() + "** has been kicked").queue();
				});
			}else{
				event.getChannel().sendMessage("I can not interact with that member").queue();
			}
		}else{
			event.getChannel().sendMessage("You can not interact with that member").queue();
		}
	}
}