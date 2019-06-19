package example.command.moderation.role;

import java.util.Optional;

import com.jockie.bot.core.argument.Argument;
import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.impl.CommandImpl;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

public class CommandRemoveRole extends CommandImpl {
	
	public CommandRemoveRole() {
		super("remove role");
			
		super.setDescription("Remove a role from a user");
		super.setBotDiscordPermissions(Permission.MANAGE_ROLES);
		super.setAuthorDiscordPermissions(Permission.MANAGE_ROLES);
	}
	
	public void onCommand(CommandEvent event, @Argument("member") Optional<Member> optionalMember, @Argument(value="role", endless=true) Role role) {
		Member member = optionalMember.orElse(event.getMember());
		
		if(event.getMember().canInteract(role)) {
			if(event.getGuild().getSelfMember().canInteract(role)) {
				if(member.getRoles().contains(role)) {
					event.getGuild().removeRoleFromMember(member, role).queue(success -> {
						User user = member.getUser();
						
						event.getChannel().sendMessage("Removed " + role.getName() + " from " + user.getName() + "#" + user.getDiscriminator()).queue();
					});
				}else{
					event.getChannel().sendMessage("Member does not have that role").queue();
				}
			}else{
				event.getChannel().sendMessage("I can not interact with that role").queue();
			}
		}else{
			event.getChannel().sendMessage("You can not interact with that role").queue();
		}
	}
}