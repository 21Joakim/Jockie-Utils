package example.command.info.channel;

import java.util.List;
import java.util.stream.Collectors;

import com.jockie.bot.core.argument.Argument;
import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.impl.CommandImpl;

import example.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.TextChannel;

public class CommandTextChannelInfo extends CommandImpl {

	public CommandTextChannelInfo() {
		super("info");
	}
	
	public void onCommand(CommandEvent event, @Argument("channel") TextChannel channel) {
		channel.retrieveInvites().queue(i -> {
			List<Invite> invites = i.stream().sorted((a, b) -> b.getUses() - a.getUses()).collect(Collectors.toList());
			
			channel.retrievePinnedMessages().queue(pins -> {	
				EmbedBuilder builder = new EmbedBuilder();
				
				builder.addField("Name", channel.getName(), true);
				builder.addField("Id", channel.getId(), true);
				
				builder.addField("Category", channel.getParent() != null ? channel.getParent().getName() : "None", true);
				
				if(invites.size() > 0) {
					Invite invite = invites.get(0);
					builder.addField("Most popular invite", "`[" + invite.getUses() + "]` " + invite.getCode(), true);
				}else{
					builder.addField("Most popular invite", "None", true);
				}
				
				builder.addField("Invites", invites.size() + "", true);
				
				builder.addField("Pins", pins.size() + "", true);
				
				builder.addField("Position", (channel.getPositionRaw() + 1) +  "", true);
				
				builder.addField("Created", Main.FORMATTER.format(channel.getTimeCreated()), true);
				
				builder.addBlankField(true);
				
				builder.addField("Topic", channel.getTopic().length() > 0 ? channel.getTopic() : "None", false);
				
				event.reply(builder.build()).queue();
			});
		});
	}
}