package example.command.info.invite;

import java.util.stream.Collectors;

import com.jockie.bot.core.argument.Argument;
import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.impl.CommandImpl;

import example.Main;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Invite;

public class CommandInviteInfo extends CommandImpl {

	public CommandInviteInfo() {
		super("invite info");
	}
	
	public void onCommand(CommandEvent event, @Argument("code") String code) {
		event.getGuild().getInvites().queue(invites -> {
			invites = invites.stream().filter(invite -> invite.getCode().equalsIgnoreCase(code)).collect(Collectors.toList());
			
			Invite invite = null;
			if(invites.size() > 1) {
				for(Invite i : invites) {
					if(i.getCode().equals(code)) {
						invite = i;
						
						break;
					}
				}
				
				if(invite == null) {
					invite = invites.get(0);
				}
			}else if(invites.size() == 1) {
				invite = invites.get(0);
			}else{
				event.getChannel().sendMessage("No invite by that code").queue();
				
				return;
			}
			
			EmbedBuilder builder = new EmbedBuilder();
			builder.addField("Code", invite.getCode(), true);
			builder.addField("Channel", invite.getChannel().getName(), true);
			builder.addField("Max age", invite.getMaxAge() == 0 ? "Infinite" : invite.getMaxAge() + " seconds", true);
			builder.addField("Max Uses", invite.getMaxUses() == 0 ? "Infinite" : invite.getMaxUses() + "", true);
			builder.addField("Uses", invite.getUses() + "", true);
			builder.addBlankField(true);
			builder.addField("Creator", invite.getInviter().getName() + "#" + invite.getInviter().getDiscriminator(), true);
			builder.addField("Created", Main.FORMATTER.format(invite.getCreationTime()), true);
			builder.addBlankField(true);
			
			event.reply(builder.build()).queue();
		});
	}
}