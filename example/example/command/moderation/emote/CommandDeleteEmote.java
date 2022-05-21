package example.command.moderation.emote;

import com.jockie.bot.core.argument.Argument;
import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.impl.CommandImpl;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emote;

public class CommandDeleteEmote extends CommandImpl {

	public CommandDeleteEmote() {
		super("delete emote");
		
		super.setDescription("delete emote");
		super.setBotDiscordPermissions(Permission.MANAGE_EMOTES_AND_STICKERS);
		super.setAuthorDiscordPermissions(Permission.MANAGE_EMOTES_AND_STICKERS);
	}
	
	public void onCommand(CommandEvent event, @Argument(value="emote", endless=true) Emote emote) {
		emote.delete().flatMap((success) -> event.replyFormat("%s has been deleted", emote.getName())).queue();
	}
}