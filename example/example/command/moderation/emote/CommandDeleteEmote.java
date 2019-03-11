package example.command.moderation.emote;

import com.jockie.bot.core.argument.Argument;
import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.impl.CommandImpl;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Emote;

public class CommandDeleteEmote extends CommandImpl {

	public CommandDeleteEmote() {
		super("delete emote");
		
		super.setDescription("delete emote");
		super.setBotDiscordPermissions(Permission.MANAGE_EMOTES);
		super.setAuthorDiscordPermissions(Permission.MANAGE_EMOTES);
	}
	
	public void onCommand(CommandEvent event, @Argument(value="emote", endless=true) Emote emote) {
		emote.delete().queue(success -> {
			event.reply(emote.getName() + " has been deleted").queue();
		});
	}
}