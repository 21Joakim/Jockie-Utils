package example.command.fun;

import com.jockie.bot.core.argument.Argument;
import com.jockie.bot.core.argument.Endless;
import com.jockie.bot.core.command.impl.CommandImpl;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandAddAll extends CommandImpl {

	public CommandAddAll() {
		super("add all");
		
		super.setDescription("Add all provided double numbers and get the result");
		super.setAliases("addall", "aa");
	}
													/* Primitive arrays are not supported yet */
	public void onCommand(MessageReceivedEvent event, @Argument(name="decimal number") @Endless(minArguments=2, maxArguments=10) Double[] nums) {
		double sum = 0;
		for(int i = 0; i < nums.length; i++) {
			sum += nums[i];
		}
		
		event.getChannel().sendMessage(sum + "").queue();
	}
}
