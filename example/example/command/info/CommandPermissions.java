package example.command.info;

import com.jockie.bot.core.command.impl.CommandImpl;
import com.jockie.bot.core.paged.impl.PagedManager;
import com.jockie.bot.core.paged.impl.PagedResult;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandPermissions extends CommandImpl {

	public CommandPermissions() {
		super("permissions");
	}
	
	public void onCommand(MessageReceivedEvent event, Member member) {
		PagedResult<Permission> paged = new PagedResult<Permission>(member.getPermissions(), Permission::getName);
		paged.setListIndexes(false);
		
		PagedManager.addPagedResult(event, paged);
	}
	
	public void onCommand(MessageReceivedEvent event, Role role) {
		PagedResult<Permission> paged = new PagedResult<Permission>(role.getPermissions(), Permission::getName);
		paged.setListIndexes(false);
		
		PagedManager.addPagedResult(event, paged);
	}
}