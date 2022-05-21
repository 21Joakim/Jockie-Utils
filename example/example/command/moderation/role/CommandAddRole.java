package example.command.moderation.role;

import java.util.Optional;

import com.jockie.bot.core.argument.Argument;
import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.impl.CommandImpl;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

public class CommandAddRole extends CommandImpl {

	public CommandAddRole() {
		super("add role");
		
		super.setDescription("Add a role to a user");
		super.setBotDiscordPermissions(Permission.MANAGE_ROLES);
		super.setAuthorDiscordPermissions(Permission.MANAGE_ROLES);
	}
	
	public void onCommand(CommandEvent event, @Argument(value="member") Optional<Member> optionalMember, @Argument(value="role", endless=true) Role role) {
		Member member = optionalMember.orElse(event.getMember());
		if(!event.getMember().canInteract(role)) {
			event.reply("You can not interact with that role").queue();
			
			return;
		}
		
		if(!event.getGuild().getSelfMember().canInteract(role)) {
			event.reply("I can not interact with that role").queue();
			
			return;
		}
		
		if(member.getRoles().contains(role)) {
			event.reply("Member already has that role").queue();
			
			return;
		}
		
		event.getGuild().addRoleToMember(member, role)
			.flatMap((success) -> event.replyFormat("Added %s to %s", role.getName(), member.getUser().getAsTag()))
			.queue();
	}
}