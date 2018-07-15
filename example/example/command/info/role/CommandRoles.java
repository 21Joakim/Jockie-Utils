package example.command.info.role;

import java.util.List;

import com.jockie.bot.core.command.argument.Argument;
import com.jockie.bot.core.command.impl.CommandImpl;
import com.jockie.bot.core.paged.impl.PagedManager;
import com.jockie.bot.core.paged.impl.PagedResult;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandRoles extends CommandImpl {

	public CommandRoles() {
		super("roles");
		
		super.setDescription("Get a list of all roles or roles by member");
	}
	
	public void onCommand(MessageReceivedEvent event, @Argument(name="member", nullDefault=true) Member member) {
		List<Role> roles = (member != null) ? member.getRoles() : event.getGuild().getRoles();
		
		if(roles.size() > 0) {
			PagedResult<Role> paged = new PagedResult<Role>(roles, Role::getAsMention /* Alternatively: role -> role.getAsMention() */);
			paged.setListIndexesContinuously(true);
			
			PagedManager.addPagedResult(event, paged);
		}else{
			if(member != null) {
				User user = member.getUser();
				
				event.getChannel().sendMessage(user.getName() + "#" + user.getDiscriminator() + " does not have any roles").queue();
			}else{
				event.getChannel().sendMessage("There are no roles on this server").queue();
			}
		}
	}
}