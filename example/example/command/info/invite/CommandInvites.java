package example.command.info.invite;

import java.util.stream.Collectors;

import com.jockie.bot.core.command.impl.CommandImpl;
import com.jockie.bot.core.paged.impl.PagedManager;
import com.jockie.bot.core.paged.impl.PagedResult;

import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Invite;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandInvites extends CommandImpl {

	public CommandInvites() {
		super("invites");
	}
	
	public void onCommand(MessageReceivedEvent event, Channel channel) {
		channel.getInvites().queue(invites -> {
			if(invites.size() > 0) {
				invites = invites.stream().sorted((a, b) -> b.getUses() - a.getUses()).collect(Collectors.toList());
				
				PagedResult<Invite> paged = new PagedResult<>(invites, invite -> "`[" + invite.getUses() + "]`" + invite.getCode());
				paged.setListIndexes(false);
				
				PagedManager.addPagedResult(event, paged);
			}else{
				event.getChannel().sendMessage("There are no invites for " + channel.getName()).queue();
			}
		});
	}
	
	public void onCommand(MessageReceivedEvent event, User user) {
		event.getGuild().getInvites().queue(invites -> {
			if(invites.size() > 0) {
				invites = invites.stream().filter(invite -> invite.getInviter().getIdLong() == user.getIdLong()).sorted((a, b) -> b.getUses() - a.getUses()).collect(Collectors.toList());
				
				if(invites.size() > 0) {
					PagedResult<Invite> paged = new PagedResult<>(invites, invite -> "`[" + invite.getUses() + "]`" + invite.getCode());
					paged.setListIndexes(false);
					
					PagedManager.addPagedResult(event, paged);
				}else{
					event.getChannel().sendMessage("There are no invites for that user").queue();
				}
			}else{
				event.getChannel().sendMessage("There are no invites for this server").queue();
			}
		});
	}
	
	public void onCommand(MessageReceivedEvent event) {
		event.getGuild().getInvites().queue(invites -> {
			if(invites.size() > 0) {
				invites = invites.stream().sorted((a, b) -> b.getUses() - a.getUses()).collect(Collectors.toList());
				
				PagedResult<Invite> paged = new PagedResult<>(invites, invite -> "`[" + invite.getUses() + "]`" + invite.getCode());
				paged.setListIndexes(false);
				
				PagedManager.addPagedResult(event, paged);
			}else{
				event.getChannel().sendMessage("There are no invites for this server").queue();
			}
		});
	}
}