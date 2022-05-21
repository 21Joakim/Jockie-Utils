package example.command.moderation;

import java.util.Optional;

import com.jockie.bot.core.argument.Argument;
import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.impl.CommandImpl;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;

public class CommandKick extends CommandImpl {

	public CommandKick() {
		super("kick");
		
		super.setDescription("Kick a member");
		super.setAuthorDiscordPermissions(Permission.KICK_MEMBERS);
		super.setBotDiscordPermissions(Permission.KICK_MEMBERS);
	}
	
	public void onCommand(CommandEvent event, @Argument("Member") Member member, @Argument(value="Reason", endless=true) Optional<String> optionalReason) {
		String reason = optionalReason.orElse(null);
		
		if(!event.getMember().canInteract(member)) {
			event.reply("You can not interact with that member").queue();
			
			return;
		}
		
		if(!event.getGuild().getSelfMember().canInteract(member)) {
			event.reply("I can not interact with that member").queue();
			
			return;
		}
			
		member.kick(reason)
			.flatMap((success) -> event.replyFormat("**%s** has been kicked", member.getUser().getAsTag()))
			.queue();
	}
}