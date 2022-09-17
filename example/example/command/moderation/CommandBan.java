package example.command.moderation;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.jockie.bot.core.argument.Argument;
import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.impl.CommandImpl;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public class CommandBan extends CommandImpl {
	
	public CommandBan() {
		super("ban");
		
		super.setDescription("Ban a user");
		super.setAuthorDiscordPermissions(Permission.BAN_MEMBERS);
		super.setBotDiscordPermissions(Permission.BAN_MEMBERS);
	}
	
	public void onCommand(CommandEvent event, @Argument("User") User user, @Argument(value="Reason", endless=true) Optional<String> optionalReason) {
		String reason = optionalReason.orElse(null);
		
		Member member = event.getGuild().getMember(user);
		if(member == null) {
			/* This is for the so called "hackban" command */
			event.getGuild().ban(user, 0, TimeUnit.SECONDS)
				.reason(reason)
				.flatMap((success) -> event.replyFormat("**%s** has been banned", user.getAsTag()))
				.queue();
			
			return;
		}
		
		if(!event.getMember().canInteract(member)) {
			event.reply("You can not interact with that member").queue();
			
			return;
		}
		
		if(!event.getGuild().getSelfMember().canInteract(member)) {
			event.reply("I can not interact with that member").queue();
			
			return;
		}
		
		member.ban(0, TimeUnit.SECONDS)
			.reason(reason)
			.flatMap((success) -> event.replyFormat("**%s** has been banned", user.getAsTag()))
			.queue();
	}
}