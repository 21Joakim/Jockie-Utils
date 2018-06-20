package example.command.moderation.role;

import com.jockie.bot.core.command.argument.Argument;
import com.jockie.bot.core.command.argument.impl.ArgumentFactory;
import com.jockie.bot.core.command.impl.CommandImpl;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandAddRole extends CommandImpl {

	public CommandAddRole() {
		super("add role",
			ArgumentFactory.of(Member.class).setName("member").setAcceptQuote(true).setDefaultValue((event) -> event.getMember()).build(),
			ArgumentFactory.of(Role.class).setName("role").setEndless(true).build()
		);
		
		super.setDescription("Add a role to a user");
		super.setBotDiscordPermissionsNeeded(Permission.MANAGE_ROLES);
		super.setAuthorDiscordPermissionsNeeded(Permission.MANAGE_ROLES);
	}
	
	public void onCommand(MessageReceivedEvent event, @Argument(name="member") Member member, @Argument(name="role", endless=true) Role role) {
		if(event.getMember().canInteract(role)) {
			if(event.getGuild().getSelfMember().canInteract(role)) {
				if(!member.getRoles().contains(role)) {
					event.getGuild().getController().addSingleRoleToMember(member, role).queue(success -> {
						User user = member.getUser();
						
						event.getChannel().sendMessage("Added " + role.getName() + " to " + user.getName() + "#" + user.getDiscriminator()).queue();
					});
				}else{
					event.getChannel().sendMessage("Member already has that role").queue();
				}
			}else{
				event.getChannel().sendMessage("I can not interact with that role").queue();
			}
		}else{
			event.getChannel().sendMessage("You can not interact with that role").queue();
		}
	}
}