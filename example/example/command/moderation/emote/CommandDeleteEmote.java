package example.command.moderation.emote;

import com.jockie.bot.core.command.argument.Argument;
import com.jockie.bot.core.command.impl.CommandImpl;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandDeleteEmote extends CommandImpl {

	public CommandDeleteEmote() {
		super("delete emote");
		
		super.setDescription("delete emote");
		super.setBotDiscordPermissionsNeeded(Permission.MANAGE_EMOTES);
		super.setAuthorDiscordPermissionsNeeded(Permission.MANAGE_EMOTES);
	}
	
	public void onCommand(MessageReceivedEvent event, @Argument(name="emote", endless=true) Emote emote) {
		emote.delete().queue(success -> {
			event.getChannel().sendMessage(emote.getName() + " has been deleted").queue();
		});
	}
}