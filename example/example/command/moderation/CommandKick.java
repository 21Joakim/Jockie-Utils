package example.command.moderation;

import com.jockie.bot.core.argument.Argument;
import com.jockie.bot.core.command.impl.CommandImpl;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Invite;
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
	
	public void onCommand(MessageReceivedEvent event, @Argument(name="Member") Member member, @Argument(name="Reason", nullDefault=true, endless=true) String reason) {
		if(event.getMember().canInteract(member)) {
			if(event.getGuild().getSelfMember().canInteract(member)) {
				event.getGuild().getController().kick(member, reason).queue(success -> {
					User user = member.getUser();
					
					event.getChannel().sendMessage("**" + user.getName() + "#" + user.getDiscriminator() + "** has been kicked").queue();
					
					user.openPrivateChannel().queue(privateChannel -> {
						StringBuilder builder = new StringBuilder();
						
						builder.append("You have been kicked from " + event.getGuild().getName());
						
						if(reason != null) {
							builder.append(" for the reason **" + reason + "**");
						}
						
						/* We don't want to create a new invite every time */
						event.getGuild().getInvites().queue(invites -> {
							Invite invite = null;
							if(invites.size() > 0) {
								/* We don't want to accidentally remove an invite which only has one invite, if possible */
								for(Invite i : invites) {
									if(invite == null) {
										invite = i;
										
										continue;
									}
									
									if(i.getMaxUses() == 0) {
										invite = i;
										
										break;
									}
									
									if(i.getMaxUses() > invite.getMaxUses()) {
										invite = i;
									}
								}
							}else{
								invite = event.getTextChannel().createInvite().setMaxAge(0).setMaxUses(0).complete();
							}
							
							builder.append(", if you want to join back here's an invite https://discord.gg/" + invite.getCode());
							
							privateChannel.sendMessage(builder).queue();
						});
					});
				});
			}else{
				event.getChannel().sendMessage("I can not interact with that member").queue();
			}
		}else{
			event.getChannel().sendMessage("You can not interact with that member").queue();
		}
	}
}