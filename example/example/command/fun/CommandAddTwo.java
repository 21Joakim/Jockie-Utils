package example.command.fun;

import com.jockie.bot.core.argument.Argument;
import com.jockie.bot.core.command.impl.CommandImpl;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandAddTwo extends CommandImpl {

	public CommandAddTwo() {
		super("add two");
		
		super.setDescription("Add two integer numbers together and get the result");
		super.setAliases("addtwo", "at");
	}
	
	public void onCommand(MessageReceivedEvent event, @Argument(name="number 1") int num1, @Argument(name="number 2") int num2) {
		event.getChannel().sendMessage((num1 + num2) + "").queue();
	}
}