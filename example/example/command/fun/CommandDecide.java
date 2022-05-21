package example.command.fun;

import java.util.concurrent.ThreadLocalRandom;

import com.jockie.bot.core.argument.Argument;
import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.impl.CommandImpl;

public class CommandDecide extends CommandImpl {
	
	public CommandDecide() {
		super("decide");
		
		super.setDescription("Give me two sentences and I will choose one of them");
	}
	
	public String onCommand(CommandEvent event, @Argument("statement") String firstStatement, @Argument("statement 2") String secondStatement) {
		return String.format("**%s** seems more reasonable to me!", ThreadLocalRandom.current().nextBoolean() ? firstStatement : secondStatement);
	}
}