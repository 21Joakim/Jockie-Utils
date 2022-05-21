package example.command.info;

import java.util.List;
import java.util.stream.Collectors;

import com.jockie.bot.core.argument.Argument;
import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.impl.CommandImpl;

import example.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.TextChannel;

public class CommandTextChannelInfo extends CommandImpl {

	public CommandTextChannelInfo() {
		super("info");
		
		super.setShortDescription("Get information about a text channel");
	}
	
	public void onCommand(CommandEvent event, @Argument("channel") TextChannel channel) {
		channel.retrieveInvites().queue((invites) -> {
			List<Invite> sortedInvites = invites.stream().sorted((a, b) -> b.getUses() - a.getUses()).collect(Collectors.toList());
			
			channel.retrievePinnedMessages().queue((pins) -> {
				EmbedBuilder builder = new EmbedBuilder();
				
				builder.addField("Name", channel.getName(), true);
				builder.addField("Id", channel.getId(), true);
				
				Category category = channel.getParentCategory();
				builder.addField("Category", category != null ? category.getName() : "None", true);
				
				if(!sortedInvites.isEmpty()) {
					Invite invite = sortedInvites.get(0);
					builder.addField("Most popular invite", "`[" + invite.getUses() + "]` " + invite.getCode(), true);
				}else{
					builder.addField("Most popular invite", "None", true);
				}
				
				builder.addField("Invites", String.valueOf(sortedInvites.size()), true);
				builder.addField("Pins", String.valueOf(pins.size()), true);
				builder.addField("Position", String.valueOf(channel.getPositionRaw() + 1), true);
				builder.addField("Created", Main.FORMATTER.format(channel.getTimeCreated()), true);
				
				builder.addBlankField(true);
				
				String topic = channel.getTopic();
				builder.addField("Topic", topic != null && topic.length() > 0 ? topic : "None", false);
				
				event.reply(builder.build()).queue();
			});
		});
	}
}