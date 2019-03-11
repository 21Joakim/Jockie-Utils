package example.command.moderation.role;

import java.util.Optional;

import com.jockie.bot.core.argument.Argument;
import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.impl.CommandImpl;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;

public class CommandAddRole extends CommandImpl {

	public CommandAddRole() {
		super("add role");
		
		super.setDescription("Add a role to a user");
		super.setBotDiscordPermissions(Permission.MANAGE_ROLES);
		super.setAuthorDiscordPermissions(Permission.MANAGE_ROLES);
	}
	
	public void onCommand(CommandEvent event, @Argument(value="member") Optional<Member> optionalMember, @Argument(value="role", endless=true) Role role) {
		Member member = optionalMember.orElse(event.getMember());
		
		if(event.getMember().canInteract(role)) {
			if(event.getGuild().getSelfMember().canInteract(role)) {
				if(!member.getRoles().contains(role)) {
					event.getGuild().getController().addSingleRoleToMember(member, role).queue(success -> {
						User user = member.getUser();
						
						event.reply("Added " + role.getName() + " to " + user.getName() + "#" + user.getDiscriminator()).queue();
					});
				}else{
					event.reply("Member already has that role").queue();
				}
			}else{
				event.reply("I can not interact with that role").queue();
			}
		}else{
			event.reply("You can not interact with that role").queue();
		}
	}
}