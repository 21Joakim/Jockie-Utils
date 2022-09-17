package example.command.moderation.emoji;

import com.jockie.bot.core.argument.Argument;
import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.impl.CommandImpl;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;

public class CommandDeleteEmoji extends CommandImpl {

	public CommandDeleteEmoji() {
		super("delete emoji");
		
		super.setDescription("delete an emoji in this server");
		super.setBotDiscordPermissions(Permission.MANAGE_EMOJIS_AND_STICKERS);
		super.setAuthorDiscordPermissions(Permission.MANAGE_EMOJIS_AND_STICKERS);
	}
	
	public void onCommand(CommandEvent event, @Argument(value="emoji", endless=true) RichCustomEmoji emoji) {
		emoji.delete().flatMap((success) -> event.replyFormat("%s has been deleted", emoji.getName())).queue();
	}
}