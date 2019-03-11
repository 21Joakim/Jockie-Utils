package example.command.moderation;

import java.util.Optional;

import com.jockie.bot.core.argument.Argument;
import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.impl.CommandImpl;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;

public class CommandKick extends CommandImpl {

	public CommandKick() {
		super("kick");
		
		super.setDescription("Kick a member");
		super.setAuthorDiscordPermissions(Permission.KICK_MEMBERS);
		super.setBotDiscordPermissions(Permission.KICK_MEMBERS);
	}
	
	public void onCommand(CommandEvent event, @Argument("Member") Member member, @Argument(value="Reason", endless=true) Optional<String> optionalReason) {
		String reason = optionalReason.orElse(null);
		
		if(event.getMember().canInteract(member)) {
			if(event.getGuild().getSelfMember().canInteract(member)) {
				event.getGuild().getController().kick(member, reason).queue(success -> {
					User user = member.getUser();
					
					event.reply("**" + user.getName() + "#" + user.getDiscriminator() + "** has been kicked").queue();
				});
			}else{
				event.reply("I can not interact with that member").queue();
			}
		}else{
			event.reply("You can not interact with that member").queue();
		}
	}
}