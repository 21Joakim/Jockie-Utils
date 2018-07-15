package example.command.info.role;

import com.jockie.bot.core.command.impl.CommandImpl;
import com.jockie.bot.core.paged.impl.PagedManager;
import com.jockie.bot.core.paged.impl.PagedResult;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandWithRole extends CommandImpl {

	public CommandWithRole() {
		super("with role");
		
		super.setDescription("Get a list of all the users in a role");
	}
	
	public void onCommand(MessageReceivedEvent event, Role role) {
		PagedResult<Member> paged = new PagedResult<Member>(event.getGuild().getMembersWithRoles(role), member -> {
			User user = member.getUser();
			
			/* I would do mentions but since lazy loading it wouldn't show all their names if the server is big */
			return user.getName() + "#" + user.getDiscriminator();
		});
		
		paged.setTimeout(false);
		
		PagedManager.addPagedResult(event, paged);
	}
}