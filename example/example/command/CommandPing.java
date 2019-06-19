package example.command;

import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.impl.CommandImpl;

public class CommandPing extends CommandImpl {
	
	public CommandPing() {
		super("ping");
		
		super.setDescription("Simple ping command");
	}
	
	public String onCommand(CommandEvent event) {
		return event.getJDA().getGatewayPing() + " ms";
	}
}