package example.command.info;

import java.util.stream.Collectors;

import com.jockie.bot.core.argument.Argument;
import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.impl.CommandImpl;

import example.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.Invite.Channel;
import net.dv8tion.jda.api.entities.User;

public class CommandInviteInfo extends CommandImpl {

	public CommandInviteInfo() {
		super("invite info");
		
		super.setShortDescription("Get information about an invite");
	}
	
	public void onCommand(CommandEvent event, @Argument("code") String code) {
		event.getGuild().retrieveInvites().queue((invites) -> {
			invites = invites.stream()
				.filter((invite) -> invite.getCode().equalsIgnoreCase(code))
				.collect(Collectors.toList());
			
			if(invites.isEmpty()) {
				event.getChannel().sendMessage("No invite by that code").queue();
				
				return;
			}
			
			Invite invite = invites.stream()
				.filter((i) -> i.getCode().equals(code))
				.findFirst()
				.orElse(invites.get(0));
			
			Channel channel = invite.getChannel();
			User inviter = invite.getInviter();
			
			EmbedBuilder embed = new EmbedBuilder()
				.addField("Code", invite.getCode(), true)
				.addField("Channel", channel != null ? channel.getName() : "None", true)
				.addField("Max age", invite.getMaxAge() == 0 ? "Infinite" : invite.getMaxAge() + " seconds", true)
				.addField("Max Uses", invite.getMaxUses() == 0 ? "Infinite" : invite.getMaxUses() + "", true)
				.addField("Uses", invite.getUses() + "", true)
				.addBlankField(true)
				.addField("Creator", inviter != null ? inviter.getAsTag() : "None", true)
				.addField("Created", Main.FORMATTER.format(invite.getTimeCreated()), true)
				.addBlankField(true);
			
			event.reply(embed.build()).queue();
		});
	}
}