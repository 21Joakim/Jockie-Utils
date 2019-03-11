package example.command.fun;

import java.util.Random;

import com.jockie.bot.core.argument.Argument;
import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.impl.CommandImpl;

public class CommandDecide extends CommandImpl {
	
	/* No need to create a new one each time someone uses it */
	private Random random = new Random();

	public CommandDecide() {
		super("decide");
		
		super.setDescription("Give me two sentences and I will choose one of them");
	}
	
	public String onCommand(CommandEvent event, @Argument("statement") String firstStatement, @Argument("statement 2") String secondStatement) {
		return "**" + (this.random.nextBoolean() ? firstStatement : secondStatement) + "**" + " seems more reasonable to me!";
	}
}